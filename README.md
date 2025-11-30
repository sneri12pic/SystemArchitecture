DE-Store Prototype (Service-Oriented Modular Monolith)

This README blends the codebase overview with the coursework report outline. Default port is 8087.

## Summary
DE-Store is a distributed store management system (not an e-commerce site) designed to be expandable and adaptive as business needs change. It provides core store functions including price control (manager-defined pricing and promotions such as 3-for-2, BOGOF, and free delivery), inventory control (continuous stock monitoring, warehouse sync, automatic re-order requests to HQ, and low-stock warnings), loyalty offers for repeat customers, finance approval via an external “Enabling” buy-now-pay-later portal integration, and reports/analysis generated from accounting activity.

This prototype is a contract pitch implementation that demonstrates two candidate architectures and a working baseline design:

**Candidate A (Selected)**: Service-Oriented Modular Monolith + API Gateway
A single deployable application with clear bounded contexts (pricing, inventory, loyalty, finance, reporting, notifications) connected via REST/HTTP + JSON through a gateway (/api/**). Internally, modules communicate using in-process domain events, with an optional Kafka path for asynchronous messaging (e.g., low-stock alerts). This approach delivers fast iteration, strong modularity, and clear seams for future service extraction.

**Candidate B (Alternative)**: Full Microservices
Independent services per capability, DB-per-service, and a Kafka/event backbone behind an API gateway (with auth, rate limiting, and resilience). This is not fully implemented here, but the prototype is structured as a stepping stone toward it.

Why Candidate A was chosen: it provides the best balance for a prototype and early delivery—high modifiability, testability, and deployability in one unit, while keeping the architecture “service-shaped” so it can evolve into distributed microservices as requirements grow.

The prototype includes working endpoints and a simple UI to demonstrate: pricing rules and price calculation, inventory sync/adjust and low-stock alerts (in-app and optionally via Kafka), loyalty offers, finance portal stub calls, and a reporting snapshot. The evaluation highlights strong modular structure and a clear scalability path, while noting intentional prototype gaps such as full authentication/authorization, real email/SMS integration, advanced analytics, and production-grade observability/resilience.

## Run Modes
- Monolith + Gateway (default): `mvn spring-boot:run "-Dspring-boot.run.profiles=monolith,gateway"` (UI and APIs on 8087).
- With Kafka: add `,kafka` (Kafka must be running): `mvn spring-boot:run "-Dspring-boot.run.profiles=monolith,gateway,kafka"`.
- Split services (demo only): run each profile on its own port (pricing 8081, inventory 8082, loyalty 8083, finance 8084, notifications 8086) and point gateway targets (`destore.gateway.*`) to those ports.
- Override port: add `"-Dspring-boot.run.arguments=--server.port=XXXX --destore.gateway.*=http://localhost:XXXX"`.

## Key Endpoints (monolith or via gateway `/api/**`)
- Pricing: `GET/POST /pricing/rules`, `POST /pricing/price`
- Inventory: `GET /inventory`, `GET /inventory/{sku}`, `POST /inventory/adjust`, `POST /inventory/sync`
- Notifications: `GET /notifications`
- Loyalty: `GET/POST /loyalty/offers/{customerId}`
- Finance: `POST /finance/apply`
- Reporting: `GET /reports/snapshot`
- UI: `/` (static page using current origin)
- Actuator: `/actuator/health`, `/actuator/info`

## Architecture (Candidate A – Selected)
- Style: Service-oriented modular monolith with profiles; API gateway at `/api/**`.
- Bounded contexts (packages):
  - `gateway` (RestClient proxy)
  - `pricing` (rules, calculation, JPA persistence)
  - `inventory` (stock, HQ sync stub, low-stock events, JPA persistence)
  - `notification` (event/Kafka consumer)
  - `loyalty` (per-customer offers, JPA persistence)
  - `finance` (Enabling stub)
  - `reporting` (snapshot aggregator)
  - `common.events` (`StockLowEvent`)
- Connectors: REST/HTTP+JSON; Spring events in-process; optional Kafka topic `stock-low`.
- Persistence: Postgres via JPA for pricing rules, inventory items, loyalty offers. H2 for tests.
- Profiles/Ports: monolith (8087), pricing (8081), inventory (8082), loyalty (8083), finance (8084), reporting (8085), notifications (8086), kafka (sets bootstrap servers). Gateway targets default to loopback (same port).

## Alternate Candidate (B) – Microservices (Not Implemented)
Independent services per bounded context, DB-per-service, Kafka backbone, API Gateway with auth/rate limiting. Current prototype is the stepping stone.

## Design Mapping
- Entry: `DestoreApplication`
- Gateway: `GatewayController`
- Pricing: `PricingController`, `PricingService`, `PricingRuleEntity/Repository`
- Inventory: `InventoryController`, `InventoryService`, `InventoryItemEntity/Repository`, `StockLowEvent`
- Notifications: `NotificationService` (@EventListener, @KafkaListener), `NotificationController`
- Loyalty: `LoyaltyController`, `LoyaltyService`, `LoyaltyOfferEntity/Repository`
- Finance: `FinanceController`, `FinanceService`, `FinanceRequest/Decision`
- Reporting: `ReportingController`, `ReportingService`, `ReportSnapshot`
- Config: `application.yml` (profiles, datasource, gateway bases, Kafka toggle), `KafkaConfig` (conditional)
- UI: `src/main/resources/static/index.html`
- Build/Infra: `pom.xml`, `Dockerfile`, `docker-compose.yml`

## Evaluation Snapshot (Prototype Coverage)
- Implemented: pricing rules & price calc; inventory with low-stock alerts; loyalty offers; finance stub; notifications; reporting snapshot; gateway; UI; optional Kafka; Postgres persistence for core domains.
- Gaps (intentional for prototype): authn/z, real email/SMS, advanced reporting, observability hardening, resilience patterns.

## Tests
- `mvn test` (profiles `monolith,test`, H2): PricingServiceTest (promo math), InventoryServiceTest (low-stock event).

## Docker
- `Dockerfile`: multi-stage build (Maven + JRE).
- `docker-compose.yml`: Postgres (volume `pgdata`), Kafka/Zookeeper, optional `destore-app` (monolith,kafka) mapped to host 8087 (adjust as needed).

## Coursework Report Alignment (S1 i–iv)
- S1(i) Two candidates: A) Service-oriented modular monolith with profiles (selected), B) Distributed microservices with broker.
- S1(ii) Rationale: A meets prototype goals with clear seams, simpler ops, consistent data; profiles show deployability; Kafka toggle shows async path. Trade-off: shared DB and not independently scalable per module.
- S1(iii) Design: Context/logical/runtime/deployment/data views; scope mapping to endpoints; ADRs (monolith first, REST/JSON, Spring events + optional Kafka, Postgres via JPA, lightweight gateway); mapping of modules to code above.
- S1(iv) Evaluation: Functional coverage (pricing, inventory alerts, loyalty, finance stub, reporting snapshot); quality attributes (modifiability, deployability, testability, scalability path via Kafka); risks/tech debt (auth missing, shared DB coupling, no outbox, limited observability/resilience).
- Roadmap: OpenAPI contracts, per-service DBs, Kafka-first with outbox, real gateway/auth, observability, resilience patterns, CI/CD per service.

## Demo Plan
1) Start monolith+gateway (8087); UI at `/`.
2) Pricing: view rules, calc price with `R-342` (3-for-2).
3) Inventory: adjust SKU-200 below threshold; check `/notifications` for alert.
4) Loyalty: override offers for a customer; fetch them.
5) Finance: apply ≤2000 (approved) vs >2000 (pending).
6) Reporting: call `/reports/snapshot`.
7) (Optional) Kafka: run with `kafka` profile; repeat inventory adjust to see `STOCK_LOW_KAFKA`.
