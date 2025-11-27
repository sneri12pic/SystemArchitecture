package com.example.soa.checkout;

import static org.junit.jupiter.api.Assertions.*;

import com.example.soa.catalog.InMemoryCatalogService;
import com.example.soa.catalog.Product;
import com.example.soa.orders.InMemoryOrdersService;
import com.example.soa.orders.Order;
import com.example.soa.orders.OrderStatus;
import com.example.soa.payments.InMemoryPaymentsService;
import com.example.soa.payments.PaymentFailedException;
import com.example.soa.users.InMemoryUsersService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckoutOrchestratorTest {
    private InMemoryUsersService usersService;
    private InMemoryCatalogService catalogService;
    private InMemoryPaymentsService paymentsService;
    private InMemoryOrdersService ordersService;
    private CheckoutOrchestrator orchestrator;
    private UUID userId;
    private Product product;

    @BeforeEach
    void setUp() {
        usersService = new InMemoryUsersService();
        catalogService = new InMemoryCatalogService();
        paymentsService = new InMemoryPaymentsService();
        ordersService = new InMemoryOrdersService();
        orchestrator = new CheckoutOrchestrator(usersService, catalogService, paymentsService, ordersService, "USD");

        userId = UUID.randomUUID();
        usersService.addUser(userId);

        product = new Product(UUID.randomUUID(), "SKU-TEST", "Test Product", 500, "USD");
        catalogService.addProduct(product, 5);
    }

    @Test
    void placeOrderConfirmsOnSuccessfulCharge() {
        CheckoutRequest request = new CheckoutRequest(
                userId,
                List.of(new CheckoutItem(product.id(), 2)),
                "tok_success",
                "idempo-1"
        );

        CheckoutResponse response = orchestrator.placeOrder(request);

        assertEquals(OrderStatus.CONFIRMED, response.status());
        Order order = ordersService.getOrder(response.orderId());
        assertEquals(OrderStatus.CONFIRMED, order.status());
        assertEquals(1000, response.totalCents());
        assertNotNull(response.chargeId());
        assertEquals(3, catalogService.stockLevel(product.id()));
    }

    @Test
    void placeOrderReleasesStockOnPaymentFailure() {
        CheckoutRequest request = new CheckoutRequest(
                userId,
                List.of(new CheckoutItem(product.id(), 1)),
                "fail-card",
                "idempo-2"
        );

        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () -> orchestrator.placeOrder(request));
        assertTrue(exception.getMessage().contains("Payment declined"));
        UUID failedOrderId = UUID.fromString(exception.getMessage().split(": ")[1]);
        Order order = ordersService.getOrder(failedOrderId);
        assertEquals(OrderStatus.FAILED_PAYMENT, order.status());
        assertEquals(5, catalogService.stockLevel(product.id()));
    }

    @Test
    void paymentsAreIdempotentPerKey() {
        CheckoutRequest request = new CheckoutRequest(
                userId,
                List.of(new CheckoutItem(product.id(), 1)),
                "tok_once",
                "idempo-3"
        );

        CheckoutResponse first = orchestrator.placeOrder(request);
        Order firstOrder = ordersService.getOrder(first.orderId());
        assertEquals(OrderStatus.CONFIRMED, firstOrder.status());

        CheckoutResponse second = orchestrator.placeOrder(request);
        assertEquals(first.chargeId(), second.chargeId());
        assertEquals(OrderStatus.CONFIRMED, ordersService.getOrder(second.orderId()).status());
    }
}
