package com.example.soa.checkout;

import com.example.soa.catalog.CatalogService;
import com.example.soa.catalog.Product;
import com.example.soa.catalog.StockReservation;
import com.example.soa.orders.Order;
import com.example.soa.orders.OrderLine;
import com.example.soa.orders.OrderStatus;
import com.example.soa.orders.OrdersService;
import com.example.soa.payments.Charge;
import com.example.soa.payments.ChargeStatus;
import com.example.soa.payments.PaymentFailedException;
import com.example.soa.payments.PaymentsService;
import com.example.soa.users.UsersService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckoutOrchestrator {
    private final UsersService usersService;
    private final CatalogService catalogService;
    private final PaymentsService paymentsService;
    private final OrdersService ordersService;
    private final String currency;
    private final Map<String, CheckoutResponse> completedByKey = new HashMap<>();

    public CheckoutOrchestrator(UsersService usersService, CatalogService catalogService, PaymentsService paymentsService, OrdersService ordersService, String currency) {
        this.usersService = usersService;
        this.catalogService = catalogService;
        this.paymentsService = paymentsService;
        this.ordersService = ordersService;
        this.currency = currency;
    }

    public CheckoutResponse placeOrder(CheckoutRequest request) {
        if (request.idempotencyKey() != null && completedByKey.containsKey(request.idempotencyKey())) {
            return completedByKey.get(request.idempotencyKey());
        }
        usersService.assertUserExists(request.userId());
        Map<UUID, Product> productLookup = new HashMap<>();
        List<StockReservation> reservations = new ArrayList<>();
        for (CheckoutItem item : request.items()) {
            Product product = catalogService.getProduct(item.productId());
            productLookup.put(product.id(), product);
            reservations.add(catalogService.reserveStock(item.productId(), item.quantity()));
        }
        List<OrderLine> lines = request.items().stream()
                .map(item -> {
                    Product product = productLookup.get(item.productId());
                    return new OrderLine(item.productId(), item.quantity(), product.priceCents());
                })
                .toList();
        Order draft = ordersService.createDraft(request.userId(), lines, currency);
        Charge charge = paymentsService.charge(draft.id(), draft.totalCents(), draft.currency(), request.idempotencyKey(), request.cardToken());
        if (charge.status() == ChargeStatus.SUCCEEDED) {
            ordersService.confirmOrder(draft.id(), charge.id());
            CheckoutResponse response = new CheckoutResponse(draft.id(), OrderStatus.CONFIRMED, charge.id(), draft.totalCents(), draft.currency());
            if (request.idempotencyKey() != null) {
                completedByKey.put(request.idempotencyKey(), response);
            }
            return response;
        }
        reservations.forEach(reservation -> catalogService.releaseReservation(reservation.id()));
        ordersService.markPaymentFailed(draft.id());
        throw new PaymentFailedException("Payment declined for order: " + draft.id());
    }
}
