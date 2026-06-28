package com.b4code.backend;

import com.b4code.backend.service.StaffOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
public class StaffOrderIntegrationTest {

    @Autowired
    private StaffOrderService staffOrderService;

    @Test
    public void testGetOrdersByProperty() {
        try {
            System.out.println("Fetching orders...");
            staffOrderService.getOrdersByProperty(1L, null, null, null, PageRequest.of(0, 100));
            System.out.println("Success!");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
