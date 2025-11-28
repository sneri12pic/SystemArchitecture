DE-Store prototype (architecture-visible, service-oriented). Please adapt, extend, and reference this in your own words for coursework use.

## What this shows
- SOA/microservice-aligned modular monolith: separate controllers/services per bounded context (pricing, inventory, loyalty, finance gateway, reporting, notifications).
- Connectors/protocols: REST/HTTP + JSON, Spring events for async stock alerts, stub external call for finance (Enabling).
- Quality hooks: modifiability (clear packages), integrability (stub adapter), observability (Actuator), basic validation.

## How to run
1) Prereqs: JDK 17+, Maven 3.8+. No DB needed (in-memory state).
2) Default modular-monolith: `mvn spring-boot:run` (uses local repo `.m2` in project to avoid home-folder permission issues).
3) Per-service ports (for stronger SOA evidence; run each in its own shell):
   - Pricing: `mvn spring-boot:run "-Dspring-boot.run.profiles=pricing"` (port 8081)
   - Inventory + notifications: `mvn spring-boot:run "-Dspring-boot.run.profiles=inventory"` (port 8082)
   - Loyalty: `mvn spring-boot:run "-Dspring-boot.run.profiles=loyalty"` (port 8083)
   - Finance gateway: `mvn spring-boot:run "-Dspring-boot.run.profiles=finance"` (port 8084)
   - Reporting aggregator (monolith-only in this prototype): stay on default run
4) API base default: `http://localhost:8080` (see key endpoints below).
5) Optional Kafka for stock-low alerts (improves SOA/eventing story):
   - Start Kafka via Docker Compose (required): `docker-compose up -d` from project root. If `docker-compose` / `docker compose` is not available, install/enable the Docker Compose plugin in Docker Desktop first.
   - Run with `mvn spring-boot:run "-Dspring-boot.run.profiles=inventory,kafka"` (inventory on 8082) and `mvn spring-boot:run "-Dspring-boot.run.profiles=notifications,kafka"` (notifications on 8086). Quoting the profiles avoids Maven mis-parsing on some shells.
   - Notifications will consume the `stock-low` topic; inventory publishes when stock is below threshold.
6) Simple UI: when running the monolith, open `http://localhost:8080` to use the static UI (see `src/main/resources/static/index.html`) to exercise endpoints without curl.
7) Persistence: PostgreSQL via Docker Compose (`postgres` service with volume `pgdata`). Inventory is persisted via Spring Data JPA; other bounded contexts remain in-memory.

## Key endpoints (happy-path smoke test)
- Pricing
  - `GET /pricing/rules` (seeded rules: BOGOF, 3-for-2, loyalty 10%, free delivery)
  - `POST /pricing/price` with body:
    ```json
    {"sku":"SKU-100","unitPrice":10,"quantity":3,"promotionRuleId":"R-342"}
    ```
- Inventory
  - `GET /inventory` (seeded SKU-100=12, SKU-200=3; SKU-200 will trigger low-stock)
  - `POST /inventory/adjust` with body `{"sku":"SKU-200","delta":-1}`
  - `POST /inventory/sync` with body `{"SKU-300":2,"SKU-400":8}` (simulates HQ push)
- Notifications
  - `GET /notifications` (shows `STOCK_LOW` events from inventory)
- Loyalty
  - `GET /loyalty/offers/{customerId}` (default offers seeded)
  - `POST /loyalty/offers/{customerId}` to override offers for a user
- Finance gateway
  - `POST /finance/apply` with body `{"customerId":"c1","amount":1500,"termMonths":12}`
- Reporting
  - `GET /reports/snapshot` (aggregate counts across services)

## Package map (C4 component-ish)
- `com.destore.pricing` (PricingController/Service, promotion rules)
- `com.destore.inventory` (InventoryController/Service, HQ sync stub, stock-low event)
- `com.destore.notification` (NotificationController/Service, event listener)
- `com.destore.loyalty` (LoyaltyController/Service)
- `com.destore.finance` (FinanceController/Service, Enabling stub)
- `com.destore.reporting` (ReportingController/Service)
- `com.destore.common.events` (shared domain events)

## Talking points for the report (rewrite yourself)
- Why this SOA-aligned cut: independent evolution of pricing/inventory/loyalty/finance; clean adapters to HQ + Enabling; events decouple alerts.
- Trade-offs: distributed concerns (consistency, tracing) even inside a modular monolith; real deployment would split services + broker.
- Per-service ports demonstrate deployability behind an API Gateway/front-end; the prototype keeps them co-located for simplicity.
- Prototype coverage vs backlog: shows connectors and data flow; persistence, authZ/authN, and production messaging are intentionally stubbed.
- Kafka profile shows how alerts/reporting could be decoupled asynchronously; document that a proper broker replaces the in-memory event bus.
