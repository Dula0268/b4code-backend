package com.b4code.backend.service;

import com.b4code.backend.models.MenuItem;
import com.b4code.backend.models.MenuItemModifier;
import com.b4code.backend.models.MenuItemModifierOption;
import com.b4code.backend.models.MenuItemVariant;
import com.b4code.backend.models.Order;
import com.b4code.backend.models.OrderItem;
import com.b4code.backend.models.Property;
import com.b4code.backend.dto.OrderRequest;
import com.b4code.backend.dao.MenuItemRepository;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.dao.OrderStatusLogRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.exception.MenuItemNotFoundException;
import com.b4code.backend.exception.OrderNotFoundException;
import com.b4code.backend.exception.StatusTransitionException;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.OrderStatusLog;
import com.b4code.backend.models.enums.OrderActorType;
import com.b4code.backend.models.enums.OrderRefundStatus;
import com.b4code.backend.models.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fixed default tax rate applied to guest food orders, kept in sync with
 * {@code PublicPropertyController#getCharges} which currently exposes the same
 * fixed 5% figure to the frontend for display purposes.
 */
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.service.NotificationService;
import com.b4code.backend.models.User;
import java.util.List;
import com.b4code.backend.service.OrderSseService;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private static final BigDecimal DEFAULT_SERVICE_CHARGE_RATE_PERCENT = BigDecimal.valueOf(10.0);
    private static final BigDecimal FIXED_TAX_RATE_PERCENT = BigDecimal.valueOf(5.0);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OrderSseService orderSseService;
    private final PaymentService paymentService;

    @Transactional
    public Order placeOrder(OrderRequest request) {
        if (request.getGuestSessionId() == null || !request.getGuestSessionId().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new RuntimeException("Invalid guest session ID format");
        }

        String actor = resolveActor(request.getGuestName(), request.getGuestSessionId());

        Order order = new Order();
        order.setPropertyId(request.getPropertyId());
        order.setGuestId(request.getGuestId());
        order.setGuestSessionId(request.getGuestSessionId());
        order.setGuestName(request.getGuestName());
        order.setGuestPhone(request.getGuestPhone());
        order.setLocation(request.getLocation());
        order.setGuestInstructions(request.getGuestInstructions());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCreatedBy(actor);
        order.setUpdatedBy(actor);

        // Never trust the client-supplied status: always derive it server-side from the
        // payment method, mirroring the intent already encoded on the frontend (online/card
        // payments must wait for payment confirmation before becoming an active order).
        boolean requiresOnlinePayment = request.getPaymentMethod() != null
                && ("online".equalsIgnoreCase(request.getPaymentMethod()) || "card".equalsIgnoreCase(request.getPaymentMethod()));
        order.setStatus(requiresOnlinePayment ? OrderStatus.PAYMENT_PENDING : OrderStatus.PLACED);

        BigDecimal subtotal = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
                MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                        .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found with ID: " + itemReq.getMenuItemId()));

                if (menuItem.getPropertyId() == null || !menuItem.getPropertyId().equals(request.getPropertyId())) {
                    throw new CustomException("Menu item does not belong to this property", HttpStatus.BAD_REQUEST);
                }

                // Authoritative price: always resolved server-side from the menu item's real
                // price/variants/modifiers, never the client-supplied priceAtOrder (still
                // accepted on the DTO so old payloads don't fail deserialization, but it is
                // ignored here).
                BigDecimal authoritativePrice = resolveAuthoritativePrice(menuItem, itemReq);

                int quantity = itemReq.getQuantity() != null ? itemReq.getQuantity() : 0;
                BigDecimal lineTotal = authoritativePrice.multiply(BigDecimal.valueOf(quantity))
                        .setScale(2, RoundingMode.HALF_UP);

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setQuantity(itemReq.getQuantity());
                item.setPriceAtOrder(authoritativePrice.doubleValue());
                item.setLineTotal(lineTotal.doubleValue());
                item.setNote(itemReq.getNote());
                item.setMenuItem(menuItem);
                order.getItems().add(item);

                subtotal = subtotal.add(lineTotal);
            }
        }

        applyPricing(order, request.getPropertyId(), subtotal);

        Order savedOrder = orderRepository.save(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(savedOrder);
        log.setOldStatus(null);
        log.setNewStatus(savedOrder.getStatus());
        log.setChangedBy(actor);
        log.setChangedByRole(OrderActorType.GUEST);
        orderStatusLogRepository.save(log);
        
        // Broadcast SSE to staff
        orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "status-update", savedOrder);
        
        // Notify staff via Notifications table
        List<User> staff = userRepository.findByPropertyIdAndDeletedFalse(request.getPropertyId());
        String msg = String.format("New order #%d received from %s", savedOrder.getId(), savedOrder.getGuestName() != null ? savedOrder.getGuestName() : "Guest");
        for (User u : staff) {
            notificationService.createNotification(u, "New Order", msg);
        }

        return savedOrder;
    }

    /**
     * Resolves the real, authoritative unit price for an order line: the menu item's base
     * price, overridden by the selected variant's price (matched by label — {@link MenuItemVariant}
     * is an embeddable value with no DB id), then increased by any selected modifier options
     * (matched by the real {@link MenuItemModifier} id plus option label). Never trusts the
     * client-supplied price for any of these — only labels/ids used to look up real amounts.
     */
    private BigDecimal resolveAuthoritativePrice(MenuItem menuItem, OrderRequest.OrderItemRequest itemReq) {
        BigDecimal price = menuItem.getPrice() != null ? menuItem.getPrice() : BigDecimal.ZERO;

        if (itemReq.getSelectedVariantLabel() != null && menuItem.getVariants() != null) {
            for (MenuItemVariant variant : menuItem.getVariants()) {
                if (itemReq.getSelectedVariantLabel().equals(variant.getLabel())) {
                    price = variant.getPrice() != null ? variant.getPrice() : price;
                    break;
                }
            }
        }

        if (itemReq.getSelectedModifiers() != null && menuItem.getModifiers() != null) {
            for (OrderRequest.SelectedModifierRequest selected : itemReq.getSelectedModifiers()) {
                if (selected.getModifierId() == null) continue;
                for (MenuItemModifier modifier : menuItem.getModifiers()) {
                    if (!selected.getModifierId().equals(modifier.getId()) || modifier.getOptions() == null) continue;
                    for (MenuItemModifierOption option : modifier.getOptions()) {
                        if (selected.getOptionLabel() != null && selected.getOptionLabel().equals(option.getLabel())) {
                            price = price.add(option.getPrice() != null ? option.getPrice() : BigDecimal.ZERO);
                        }
                    }
                    break;
                }
            }
        }

        return price;
    }

    /**
     * Computes the full money breakdown server-side from the resolved (authoritative) line
     * prices and persists every component on the order: subtotal, service charge, tax,
     * discount and the grand total, plus the rates actually used. This is the single source
     * of truth for order money — the guest checkout screen and every staff view render these
     * persisted values rather than re-deriving anything client-side.
     *
     * <p>Each component is rounded to 2dp independently and the grand total is the sum of the
     * rounded components, so subtotal + serviceCharge + tax - discount always adds up exactly
     * to the displayed total (no "the parts don't sum to the total" artefacts).</p>
     */
    private void applyPricing(Order order, Long propertyId, BigDecimal rawSubtotal) {
        BigDecimal serviceChargeRate = DEFAULT_SERVICE_CHARGE_RATE_PERCENT;
        if (propertyId != null) {
            Property property = propertyRepository.findById(propertyId).orElse(null);
            if (property != null && property.getServiceChargeRate() != null) {
                serviceChargeRate = BigDecimal.valueOf(property.getServiceChargeRate());
            }
        }
        BigDecimal taxRate = FIXED_TAX_RATE_PERCENT;

        BigDecimal subtotal = rawSubtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceCharge = subtotal
                .multiply(serviceChargeRate.divide(HUNDRED, 6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal
                .multiply(taxRate.divide(HUNDRED, 6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        // No discount scheme exists yet; persisted explicitly so the breakdown is complete
        // and a future discount only has to change this one line.
        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(serviceCharge).add(tax).subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);

        order.setSubtotalAmount(subtotal.doubleValue());
        order.setServiceChargeAmount(serviceCharge.doubleValue());
        order.setTaxAmount(tax.doubleValue());
        order.setDiscountAmount(discount.doubleValue());
        order.setServiceChargeRate(serviceChargeRate.doubleValue());
        order.setTaxRate(taxRate.doubleValue());
        order.setTotalAmount(total.doubleValue());
    }

    /**
     * Reverses the payment behind a cancelled order and records the outcome on the order.
     * The refunded amount is the authoritative amount stored on the Payment row - never a
     * client-supplied figure. Orders that were never actually paid (cash / pay-at-property, or
     * an online order still awaiting settlement) are marked NOT_APPLICABLE.
     */
    private void applyRefund(Order order) {
        PaymentService.FoodOrderRefundResult result = paymentService.refundFoodOrderPayment(order.getId());
        if (result.refunded()) {
            order.setRefundStatus(OrderRefundStatus.REFUNDED);
            order.setRefundAmount(result.amount());
            order.setRefundReference(result.reference());
            order.setRefundedAt(java.time.LocalDateTime.now());
        } else {
            order.setRefundStatus(OrderRefundStatus.NOT_APPLICABLE);
            order.setRefundAmount(null);
            order.setRefundReference(null);
            order.setRefundedAt(null);
        }
    }

    private String resolveActor(String guestName, String guestSessionId) {
        if (guestName != null && !guestName.isBlank()) {
            return guestName;
        }
        return "guest:" + guestSessionId;
    }

    public Page<Order> getGuestOrderHistory(Long guestId, String guestSessionId, Pageable pageable) {
        if (guestSessionId == null || guestSessionId.isBlank()) {
            throw new CustomException("guestSessionId is required", HttpStatus.BAD_REQUEST);
        }
        return orderRepository.findByGuestIdAndGuestSessionIdOrderByCreatedAtDesc(guestId, guestSessionId, pageable);
    }

    public Page<Order> getGuestOrderHistoryBySession(String guestSessionId, Pageable pageable) {
        return orderRepository.findByGuestSessionIdOrderByCreatedAtDesc(guestSessionId, pageable);
    }

    /**
     * Internal lookup with no guest-session ownership check. Only for use by trusted,
     * non-guest-facing callers (e.g. the dev-only payment simulator) that have their own
     * gating. Guest-facing reads/mutations must go through {@link #getOrderById(Long, String)}.
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    /**
     * Guest-facing lookup: requires the caller to present the guestSessionId that was used to
     * place the order. On mismatch we throw a generic "not found" (rather than "forbidden") so
     * the endpoint doesn't leak whether an order with that ID exists at all (IDOR fix).
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, String guestSessionId) {
        Order order = getOrderById(orderId);
        assertOwnedBySession(order, guestSessionId);
        return order;
    }

    private void assertOwnedBySession(Order order, String guestSessionId) {
        if (guestSessionId == null || guestSessionId.isBlank()
                || order.getGuestSessionId() == null
                || !order.getGuestSessionId().equals(guestSessionId)) {
            throw new CustomException("Order not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Statuses a guest may still cancel from. Once the kitchen is actually cooking
     * (IN_PROGRESS) or the food is plated/handed over (READY, DELIVERED), only staff can
     * cancel - a guest self-cancel there would strand food the property already paid to make.
     */
    private static final java.util.Set<OrderStatus> GUEST_CANCELLABLE_STATUSES =
            java.util.EnumSet.of(OrderStatus.PAYMENT_PENDING, OrderStatus.PLACED, OrderStatus.ACCEPTED);

    /**
     * Guest-initiated cancellation.
     *
     * <p>Runs in a single transaction covering the status transition, the refund and the audit
     * log, so a failing refund rolls the cancellation back rather than leaving a
     * cancelled-but-unrefunded order. Idempotent on an already-CANCELLED order: it returns the
     * existing (already refunded) order instead of refunding a second time.</p>
     */
    @Transactional
    public Order cancelOrder(Long orderId, String guestSessionId) {
        // Ownership check first: getOrderById(id, session) throws a generic 404 on mismatch so a
        // guest can never cancel - or even probe for - somebody else's order.
        Order order = getOrderById(orderId, guestSessionId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            // Idempotency guard: a double-tap / retried request must not trigger a second refund.
            return order;
        }
        if (!GUEST_CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new StatusTransitionException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        String actor = resolveActor(order.getGuestName(), order.getGuestSessionId());
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedBy(actor);
        order.setCancelledBy(OrderActorType.GUEST);
        order.setCancelledAt(java.time.LocalDateTime.now());

        // Refund inside the same transaction. Any provider/persistence failure propagates and
        // rolls back the CANCELLED transition above.
        applyRefund(order);

        Order savedOrder = orderRepository.save(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(savedOrder);
        log.setOldStatus(oldStatus);
        log.setNewStatus(OrderStatus.CANCELLED);
        log.setChangedBy(actor);
        log.setChangedByRole(OrderActorType.GUEST);
        orderStatusLogRepository.save(log);

        orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "status-update", savedOrder);
        List<User> staff = userRepository.findByPropertyIdAndDeletedFalse(savedOrder.getPropertyId());
        String msg = String.format("Order #%d was cancelled by guest", savedOrder.getId());
        for (User u : staff) {
            notificationService.createNotification(u, "Order Cancelled", msg);
        }

        return savedOrder;
    }

    @Transactional
    public Order confirmPayment(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return order; // Already processed
        }

        String actor = resolveActor(order.getGuestName(), order.getGuestSessionId());
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.PLACED);
        order.setUpdatedBy(actor);
        Order savedOrder = orderRepository.save(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(savedOrder);
        log.setOldStatus(oldStatus);
        log.setNewStatus(OrderStatus.PLACED);
        log.setChangedBy(actor);
        log.setChangedByRole(OrderActorType.SYSTEM);
        orderStatusLogRepository.save(log);
        
        orderSseService.sendPropertyEvent(savedOrder.getPropertyId(), "status-update", savedOrder);
        List<User> staff = userRepository.findByPropertyIdAndDeletedFalse(savedOrder.getPropertyId());
        String msg = String.format("New order #%d payment completed", savedOrder.getId());
        for (User u : staff) {
            notificationService.createNotification(u, "Order Paid", msg);
        }

        return savedOrder;
    }
}
