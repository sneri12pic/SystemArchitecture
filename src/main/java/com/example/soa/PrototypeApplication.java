package com.example.soa;

import com.example.soa.catalog.InMemoryCatalogService;
import com.example.soa.catalog.Product;
import com.example.soa.checkout.CheckoutItem;
import com.example.soa.checkout.CheckoutOrchestrator;
import com.example.soa.checkout.CheckoutRequest;
import com.example.soa.checkout.CheckoutResponse;
import com.example.soa.orders.InMemoryOrdersService;
import com.example.soa.payments.InMemoryPaymentsService;
import com.example.soa.users.InMemoryUsersService;
import java.util.List;
import java.util.UUID;

public class PrototypeApplication {
    public static void main(String[] args) {
        InMemoryUsersService usersService = new InMemoryUsersService();
        InMemoryCatalogService catalogService = new InMemoryCatalogService();
        InMemoryPaymentsService paymentsService = new InMemoryPaymentsService();
        InMemoryOrdersService ordersService = new InMemoryOrdersService();

        UUID userId = UUID.randomUUID();
        usersService.addUser(userId);

        Product product = new Product(UUID.randomUUID(), "SKU-1", "Demo Product", 1500, "USD");
        catalogService.addProduct(product, 10);

        CheckoutOrchestrator orchestrator = new CheckoutOrchestrator(usersService, catalogService, paymentsService, ordersService, "USD");

        CheckoutRequest request = new CheckoutRequest(
                userId,
                List.of(new CheckoutItem(product.id(), 2)),
                "tok_demo",
                "order-1"
        );

        CheckoutResponse response = orchestrator.placeOrder(request);
        System.out.printf("Order %s confirmed for %d %s (charge=%s)%n", response.orderId(), response.totalCents(), response.currency(), response.chargeId());
    }
}
