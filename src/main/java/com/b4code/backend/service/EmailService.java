package com.b4code.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationOTPEmail(String toEmail, String guestName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Prime Stay Account");

            String htmlContent = buildVerificationOTPHtml(guestName, otp);
            helper.setText(htmlContent, true);

            log.info("[EMAIL] Attempting to send OTP email to {}...", toEmail);
            mailSender.send(message);
            log.info("[OTP DEBUG] Verification OTP for {} is: {}", toEmail, otp);
            log.info("[EMAIL] Verification OTP email successfully sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("[EMAIL ERROR] Failed to send verification OTP email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay");
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Prime Stay Password");

            String htmlContent = buildPasswordResetHtml(resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendInvitationEmail(String toEmail, String role, String inviteLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay");
            helper.setTo(toEmail);
            helper.setSubject("You've been invited to join Prime Stay!");

            String htmlContent = buildInvitationHtml(role, inviteLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Invitation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendBookingConfirmationEmail(String toEmail, String guestName, String confirmationNumber, 
                                            String propertyName, String checkIn, String checkOut, String totalAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay");
            helper.setTo(toEmail);
            helper.setSubject("Booking Confirmed! - " + propertyName);

            String htmlContent = buildBookingConfirmationHtml(guestName, confirmationNumber, propertyName, checkIn, checkOut, totalAmount);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    @Async
    public void sendBookingModificationEmail(String toEmail, String guestName, String confirmationNumber, 
                                            String propertyName, String oldRoomCategory, String newRoomCategory,
                                            String oldDates, String newDates, String differenceAmount, String newTotalAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay");
            helper.setTo(toEmail);
            helper.setSubject("Booking Updated! - " + propertyName);

            String htmlContent = buildBookingModificationHtml(guestName, confirmationNumber, propertyName, oldRoomCategory, newRoomCategory, oldDates, newDates, differenceAmount, newTotalAmount);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Booking modification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send booking modification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendUpcomingStayReminder(com.b4code.backend.models.Booking booking) {
        String toEmail = booking.getGuestEmail();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay");
            helper.setTo(toEmail);
            helper.setSubject("Upcoming Stay Reminder: " + booking.getProperty().getName());

            String htmlContent = buildUpcomingStayReminderHtml(booking);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Upcoming stay reminder email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send upcoming stay reminder email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildUpcomingStayReminderHtml(com.b4code.backend.models.Booking booking) {
        String guestName = booking.getGuestName() != null ? booking.getGuestName() : "Guest";
        String propertyName = booking.getProperty().getName();
        String address = booking.getProperty().getAddressLine1() != null ? booking.getProperty().getAddressLine1() : booking.getProperty().getAddress();
        if (booking.getProperty().getCity() != null) {
            address = address != null ? address + ", " + booking.getProperty().getCity() : booking.getProperty().getCity();
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#9a3300,#c44a00);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:13px;">Upcoming Stay Reminder</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:20px;font-weight:700;">Hello %s,</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            Your check-in date is tomorrow! We are excited to host you at <strong>%s</strong>.
                          </p>
                          
                          <!-- Booking Card -->
                          <div style="background:#fdfaf8;border:1px solid #f3e8e2;border-radius:12px;padding:24px;margin:0 0 28px;">
                            <table width="100%%" cellpadding="0" cellspacing="0">
                              <tr>
                                <td style="padding-bottom:12px;color:#828282;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Property Address</td>
                              </tr>
                              <tr>
                                <td style="padding-bottom:20px;color:#9a3300;font-size:18px;font-weight:700;">%s</td>
                              </tr>
                              <tr>
                                <td style="padding-bottom:8px;color:#1d1d1d;font-size:14px;"><strong>Check-In Date:</strong> %s</td>
                              </tr>
                              <tr>
                                <td style="color:#1d1d1d;font-size:14px;"><strong>Confirmation No:</strong> %s</td>
                              </tr>
                            </table>
                          </div>
                          
                          <p style="margin:0 0 28px;color:#555555;font-size:14px;line-height:1.6;">
                            Safe travels and we look forward to your arrival!
                          </p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;text-align:center;">
                            Thank you for choosing Prime Stay Sri Lanka.<br/>
                            &copy; 2025 All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(guestName, propertyName, address, booking.getCheckIn().toString(), booking.getConfirmationCode());
    }

    private String buildBookingConfirmationHtml(String guestName, String confirmationNumber, 
                                               String propertyName, String checkIn, String checkOut, String totalAmount) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#9a3300,#c44a00);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:13px;">Your Booking is Confirmed!</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:20px;font-weight:700;">Hello %s,</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            Pack your bags! Your stay at <strong>%s</strong> has been successfully booked. Here are your reservation details:
                          </p>
                          
                          <!-- Booking Card -->
                          <div style="background:#fdfaf8;border:1px solid #f3e8e2;border-radius:12px;padding:24px;margin:0 0 28px;">
                            <table width="100%%" cellpadding="0" cellspacing="0">
                              <tr>
                                <td style="padding-bottom:12px;color:#828282;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Confirmation Number</td>
                              </tr>
                              <tr>
                                <td style="padding-bottom:20px;color:#9a3300;font-size:20px;font-weight:800;">%s</td>
                              </tr>
                              <tr>
                                <td style="padding-bottom:8px;color:#1d1d1d;font-size:14px;"><strong>Dates:</strong> %s - %s</td>
                              </tr>
                              <tr>
                                <td style="color:#1d1d1d;font-size:14px;"><strong>Total Amount:</strong> LKR %s</td>
                              </tr>
                            </table>
                          </div>
                          
                          <p style="margin:0 0 28px;color:#555555;font-size:14px;line-height:1.6;">
                            We have notified the property owner of your upcoming visit. If you have any questions or need to make changes, please contact our support team.
                          </p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;text-align:center;">
                            Thank you for choosing Prime Stay Sri Lanka.<br/>
                            &copy; 2025 All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(guestName, propertyName, confirmationNumber, checkIn, checkOut, totalAmount);
    }

    private String buildVerificationOTPHtml(String guestName, String otp) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#9a3300,#c44a00);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:13px;">Sri Lanka's Premier Hospitality Platform</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:22px;font-weight:700;">Verify Your Email</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            Hello %s, thank you for joining Prime Stay. Please use the following One-Time Password (OTP) to verify your account. This code is valid for <strong>10 minutes</strong>.
                          </p>
                          
                          <!-- OTP Card -->
                          <div style="background:#fdfaf8;border:1px solid #f3e8e2;border-radius:12px;padding:32px;margin:0 0 28px;text-align:center;">
                            <span style="color:#9a3300;font-size:36px;font-weight:800;letter-spacing:8px;font-family:monospace;">%s</span>
                          </div>
                          
                          <p style="margin:0 0 28px;color:#555555;font-size:14px;line-height:1.6;">
                            If you did not create an account with us, you can safely ignore this email.
                          </p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;text-align:center;">
                            &copy; 2025 Prime Stay Sri Lanka. All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(guestName, otp);
    }

    private String buildPasswordResetHtml(String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Reset Your Password</title>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#9a3300,#c44a00);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:13px;">Sri Lanka's Premier Hospitality Platform</p>
                        </td>
                      </tr>
                      
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:22px;font-weight:700;">Reset Your Password</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            We received a request to reset your password. Click the button below to choose a new password. This link will expire in <strong>30 minutes</strong>.
                          </p>
                          
                          <!-- CTA Button -->
                          <table cellpadding="0" cellspacing="0" style="margin:0 0 28px;">
                            <tr>
                              <td style="background:#9a3300;border-radius:10px;">
                                <a href="%s" target="_blank"
                                   style="display:inline-block;padding:14px 32px;color:#ffffff;font-size:15px;font-weight:700;text-decoration:none;letter-spacing:0.3px;">
                                  Reset Password →
                                </a>
                              </td>
                            </tr>
                          </table>
                          
                          <p style="margin:0 0 8px;color:#828282;font-size:13px;">Or copy and paste this link in your browser:</p>
                          <p style="margin:0 0 28px;color:#9a3300;font-size:12px;word-break:break-all;">%s</p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;">
                            If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.<br/><br/>
                            &copy; 2025 Prime Stay Sri Lanka. All rights reserved.
                          </p>
                        </td>
                      </tr>
                      
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(resetLink, resetLink);
    }

    private String buildInvitationHtml(String role, String inviteLink) {
        String formattedRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
        
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Invitation to join Prime Stay</title>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#9a3300,#c44a00);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:13px;">Sri Lanka's Premier Hospitality Platform</p>
                        </td>
                      </tr>
                      
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:22px;font-weight:700;">You're Invited!</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            You have been invited to join the Prime Stay administration team as a <strong>%s</strong>. 
                            Click the button below to accept the invitation, set your name, and choose a secure password for your account.
                          </p>
                          
                          <!-- CTA Button -->
                          <table cellpadding="0" cellspacing="0" style="margin:0 0 28px;">
                            <tr>
                              <td style="background:#9a3300;border-radius:10px;">
                                <a href="%s" target="_blank"
                                   style="display:inline-block;padding:14px 32px;color:#ffffff;font-size:15px;font-weight:700;text-decoration:none;letter-spacing:0.3px;">
                                  Accept Invitation →
                                </a>
                              </td>
                            </tr>
                          </table>
                          
                          <p style="margin:0 0 8px;color:#828282;font-size:13px;">Or copy and paste this link in your browser:</p>
                          <p style="margin:0 0 28px;color:#9a3300;font-size:12px;word-break:break-all;">%s</p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;">
                            This invitation link will expire in 48 hours.<br/>
                            If you were not expecting this invitation, you can safely ignore this email.<br/><br/>
                            &copy; 2025 Prime Stay Sri Lanka. All rights reserved.
                          </p>
                        </td>
                      </tr>
                      
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(formattedRole, inviteLink, inviteLink);
    }

    private String buildBookingModificationHtml(String guestName, String confirmationNumber, 
                                               String propertyName, String oldRoomCategory, String newRoomCategory,
                                               String oldDates, String newDates, String differenceAmount, String newTotalAmount) {
        String changesDetailHtml = "";
        
        if (!oldRoomCategory.equalsIgnoreCase(newRoomCategory)) {
            changesDetailHtml += """
                <tr>
                  <td style="padding: 10px 0; border-bottom: 1px solid #eeeeee;">
                    <strong style="color: #555555; font-size: 13px;">Room Type:</strong><br/>
                    <span style="text-decoration: line-through; color: #888888; font-size: 13px;">%s</span> 
                    <span style="color: #9a3300; font-weight: bold; font-size: 13px;">&rarr; %s</span>
                  </td>
                </tr>
                """.formatted(oldRoomCategory, newRoomCategory);
        }
        
        if (!oldDates.equalsIgnoreCase(newDates)) {
            changesDetailHtml += """
                <tr>
                  <td style="padding: 10px 0; border-bottom: 1px solid #eeeeee;">
                    <strong style="color: #555555; font-size: 13px;">Stay Dates:</strong><br/>
                    <span style="text-decoration: line-through; color: #888888; font-size: 13px;">%s</span> 
                    <span style="color: #9a3300; font-weight: bold; font-size: 13px;">&rarr; %s</span>
                  </td>
                </tr>
                """.formatted(oldDates, newDates);
        }

        // Parse difference to see if it's refund, extra payment, or no change
        double diff = 0;
        try {
            diff = Double.parseDouble(differenceAmount.replaceAll("[^0-9.-]", ""));
        } catch (Exception e) {}

        String paymentInfoHtml = "";
        if (diff > 0) {
            paymentInfoHtml = """
                <tr>
                  <td style="padding: 10px 0; border-bottom: 1px solid #eeeeee; color: #d32f2f;">
                    <strong style="font-size: 13px;">Additional Payment Made:</strong> 
                    <span style="font-weight: bold; font-size: 13px;">LKR %s</span>
                  </td>
                </tr>
                """.formatted(differenceAmount);
        } else if (diff < 0) {
            paymentInfoHtml = """
                <tr>
                  <td style="padding: 10px 0; border-bottom: 1px solid #eeeeee; color: #388e3c;">
                    <strong style="font-size: 13px;">Refund Issued:</strong> 
                    <span style="font-weight: bold; font-size: 13px;">LKR %s</span>
                  </td>
                </tr>
                """.formatted(String.format("%.2f", Math.abs(diff)));
        } else {
            paymentInfoHtml = """
                <tr>
                  <td style="padding: 10px 0; border-bottom: 1px solid #eeeeee; color: #555555;">
                    <strong style="font-size: 13px;">No Price Difference</strong>
                  </td>
                </tr>
                """;
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#9a3300,#c44a00);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:13px;">Booking Modified Successfully</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:20px;font-weight:700;">Hello %s,</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            Your booking for <strong>%s</strong> (Confirmation Code: <strong>%s</strong>) has been updated as requested. Here are the modifications made:
                          </p>
                          
                          <!-- Changes List -->
                          <div style="background:#fdfaf8;border:1px solid #f3e8e2;border-radius:12px;padding:24px;margin:0 0 28px;">
                            <table width="100%%" cellpadding="0" cellspacing="0">
                              <tr>
                                <td style="padding-bottom:12px;color:#828282;font-size:12px;text-transform:uppercase;letter-spacing:1px;font-weight:bold;border-bottom:2px solid #f3e8e2;">Modifications Details</td>
                              </tr>
                              %s
                              %s
                              <tr>
                                <td style="padding: 12px 0 0; font-size: 15px; color: #1d1d1d;">
                                  <strong>New Total Amount:</strong> LKR %s
                                </td>
                              </tr>
                            </table>
                          </div>
                          
                          <p style="margin:0 0 28px;color:#555555;font-size:14px;line-height:1.6;">
                            If you did not request these modifications or have any concerns, please get in touch with our team immediately.
                          </p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;text-align:center;">
                            Thank you for staying with Prime Stay.<br/>
                            &copy; 2025 Prime Stay Sri Lanka. All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(guestName, propertyName, confirmationNumber, changesDetailHtml, paymentInfoHtml, newTotalAmount);
    }
    @Async
    public void sendPayoutProcessedEmail(String toEmail, com.b4code.backend.models.Payout payout) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay Finance");
            helper.setTo(toEmail);
            helper.setSubject("Payout Processed - " + payout.getPropertyName());

            String htmlContent = buildPayoutProcessedHtml(payout);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Payout processed email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send payout processed email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPayoutRejectedEmail(String toEmail, com.b4code.backend.models.Payout payout) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Prime Stay Finance");
            helper.setTo(toEmail);
            helper.setSubject("Payout Request Rejected - " + payout.getPropertyName());

            String htmlContent = buildPayoutRejectedHtml(payout);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Payout rejected email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send payout rejected email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildPayoutProcessedHtml(com.b4code.backend.models.Payout payout) {
        String netAmount = payout.getAmount() != null ? String.format("%.2f", payout.getAmount()) : "0.00";
        String bankRef = payout.getBankReference() != null ? payout.getBankReference() : "N/A";
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#16A34A,#15803D);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.9);font-size:13px;">Payout Processed Successfully</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:20px;font-weight:700;">Hello %s,</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            We have successfully processed your payout request for <strong>%s</strong>. The funds have been transferred to your registered bank account.
                          </p>
                          
                          <!-- Payout Card -->
                          <div style="background:#f0fdf4;border:1px solid #dcfce7;border-radius:12px;padding:24px;margin:0 0 28px;">
                            <table width="100%%" cellpadding="0" cellspacing="0">
                              <tr>
                                <td style="padding-bottom:12px;color:#166534;font-size:12px;text-transform:uppercase;letter-spacing:1px;font-weight:bold;">Transfer Details</td>
                              </tr>
                              <tr>
                                <td style="padding-bottom:12px;color:#15803D;font-size:20px;font-weight:800;">LKR %s</td>
                              </tr>
                              <tr>
                                <td style="padding-bottom:8px;color:#1d1d1d;font-size:14px;"><strong>Bank Reference:</strong> %s</td>
                              </tr>
                            </table>
                          </div>
                          
                          <p style="margin:0 0 28px;color:#555555;font-size:14px;line-height:1.6;">
                            Please allow 1-3 business days for the funds to reflect in your account, depending on your bank's processing times.
                          </p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;text-align:center;">
                            Thank you for partnering with Prime Stay.<br/>
                            &copy; 2025 All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(payout.getOwnerName() != null ? payout.getOwnerName() : "Owner", payout.getPropertyName(), netAmount, bankRef);
    }

    private String buildPayoutRejectedHtml(com.b4code.backend.models.Payout payout) {
        String note = payout.getAdminNote() != null && !payout.getAdminNote().isBlank() ? payout.getAdminNote() : "No specific reason provided.";
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#DC2626,#991B1B);padding:36px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">PRIME STAY</h1>
                          <p style="margin:6px 0 0;color:rgba(255,255,255,0.9);font-size:13px;">Payout Request Rejected</p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:40px;">
                          <h2 style="margin:0 0 12px;color:#1d1d1d;font-size:20px;font-weight:700;">Hello %s,</h2>
                          <p style="margin:0 0 24px;color:#555555;font-size:15px;line-height:1.6;">
                            Unfortunately, your recent payout request for <strong>%s</strong> could not be processed and has been rejected by our finance team.
                          </p>
                          
                          <!-- Rejection Reason Card -->
                          <div style="background:#fef2f2;border:1px solid #fee2e2;border-radius:12px;padding:24px;margin:0 0 28px;">
                            <table width="100%%" cellpadding="0" cellspacing="0">
                              <tr>
                                <td style="padding-bottom:12px;color:#991B1B;font-size:12px;text-transform:uppercase;letter-spacing:1px;font-weight:bold;">Reason for Rejection</td>
                              </tr>
                              <tr>
                                <td style="color:#7f1d1d;font-size:14px;line-height:1.5;">%s</td>
                              </tr>
                            </table>
                          </div>
                          
                          <p style="margin:0 0 28px;color:#555555;font-size:14px;line-height:1.6;">
                            Please review the reason above, update your bank details if necessary, and submit a new payout request through your owner dashboard.
                          </p>
                          
                          <hr style="border:none;border-top:1px solid #eeeeee;margin:0 0 24px;"/>
                          
                          <p style="margin:0;color:#aaaaaa;font-size:12px;line-height:1.6;text-align:center;">
                            If you have questions, please contact our support team.<br/>
                            &copy; 2025 All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(payout.getOwnerName() != null ? payout.getOwnerName() : "Owner", payout.getPropertyName(), note);
    }
}
