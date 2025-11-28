package com.destore.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Profile({"monolith", "gateway"})
public class GatewayController {

    private final RestClient client;
    private final String pricingBase;
    private final String inventoryBase;
    private final String loyaltyBase;
    private final String financeBase;
    private final String notificationsBase;
    private final String reportingBase;

    public GatewayController(@Value("${destore.gateway.pricing:http://localhost:8080}") String pricingBase,
                             @Value("${destore.gateway.inventory:http://localhost:8080}") String inventoryBase,
                             @Value("${destore.gateway.loyalty:http://localhost:8080}") String loyaltyBase,
                             @Value("${destore.gateway.finance:http://localhost:8080}") String financeBase,
                             @Value("${destore.gateway.notifications:http://localhost:8080}") String notificationsBase,
                             @Value("${destore.gateway.reporting:http://localhost:8080}") String reportingBase) {
        this.client = RestClient.builder().build();
        this.pricingBase = pricingBase;
        this.inventoryBase = inventoryBase;
        this.loyaltyBase = loyaltyBase;
        this.financeBase = financeBase;
        this.notificationsBase = notificationsBase;
        this.reportingBase = reportingBase;
    }

    @PostMapping("/pricing/price")
    public ResponseEntity<String> price(@RequestBody String body) {
        return forward(pricingBase + "/pricing/price", body);
    }

    @PostMapping("/pricing/rules")
    public ResponseEntity<String> addRule(@RequestBody String body) {
        return forward(pricingBase + "/pricing/rules", body);
    }

    @GetMapping("/pricing/rules")
    public ResponseEntity<String> listRules() {
        return client.get().uri(pricingBase + "/pricing/rules").retrieve().toEntity(String.class);
    }

    @GetMapping("/inventory")
    public ResponseEntity<String> inventory() {
        return client.get().uri(inventoryBase + "/inventory").retrieve().toEntity(String.class);
    }

    @PostMapping("/inventory/adjust")
    public ResponseEntity<String> adjust(@RequestBody String body) {
        return forward(inventoryBase + "/inventory/adjust", body);
    }

    @PostMapping("/inventory/sync")
    public ResponseEntity<String> sync(@RequestBody String body) {
        return forward(inventoryBase + "/inventory/sync", body);
    }

    @GetMapping("/notifications")
    public ResponseEntity<String> notifications() {
        return client.get().uri(notificationsBase + "/notifications").retrieve().toEntity(String.class);
    }

    @GetMapping("/loyalty/offers/{customerId}")
    public ResponseEntity<String> loyalty(@PathVariable String customerId) {
        return client.get().uri(loyaltyBase + "/loyalty/offers/" + customerId).retrieve().toEntity(String.class);
    }

    @PostMapping("/loyalty/offers/{customerId}")
    public ResponseEntity<String> loyaltyOverride(@PathVariable String customerId, @RequestBody String body) {
        return forward(loyaltyBase + "/loyalty/offers/" + customerId, body);
    }

    @PostMapping("/finance/apply")
    public ResponseEntity<String> finance(@RequestBody String body) {
        return forward(financeBase + "/finance/apply", body);
    }

    @GetMapping("/reports/snapshot")
    public ResponseEntity<String> report() {
        return client.get().uri(reportingBase + "/reports/snapshot").retrieve().toEntity(String.class);
    }

    private ResponseEntity<String> forward(String url, String body) {
        return client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }
}
