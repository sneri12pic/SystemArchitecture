# SOA Build Spec — Orchestrated, Service-Based Web System

This spec guides a single-deploy, service-based (SOA) Node.js system that balances clear domain boundaries with simple operations. It is optimized for maintainability, testability, and low latency while keeping ops overhead low (single database, coarse-grained services, orchestration for cross-domain flows).

## 0) Executive intent
- Build a service-based web application with coarse-grained domain services (**Catalog, Orders, Payments, Users**) coordinated by a lightweight **Orchestrator** for multi-step workflows (e.g., `PlaceOrder`).
- Monorepo, single deployable (one container/process group), single PostgreSQL with schema-per-domain to preserve boundaries.
- Clear service contracts (OpenAPI), optional API Gateway for external clients.
- Technology: Node.js 20+, TypeScript 5+, Fastify or NestJS, Prisma 5+, PostgreSQL 14+, Zod validation, Jest tests, pino logging, Prometheus metrics.

## 1) High-level architecture
**Runtime topology**
- `apps/api-gateway/` (optional): Routes external requests to services; applies auth, rate limiting, and audit logging.
- `apps/orchestrator/`: Coordinates cross-domain workflows.
- `apps/services/`: Coarse-grained services: `catalog/`, `orders/`, `payments/`, `users/`.
- `packages/shared-*`: Common config, logger, types, database helpers, and error primitives.
- Single PostgreSQL instance with schemas per domain; optional Redis for idempotency, job retries, and outbox/event relay.

**Deployment**
- Single container (or PM2 multi-process) running gateway + orchestrator + services.
- Environment variables: `NODE_ENV`, `DATABASE_URL`, `REDIS_URL`, `JWT_SECRET`, `PAYMENT_PROVIDER=mock|stripe`.

## 2) Repository layout
```
repo/
  apps/
    api-gateway/
    orchestrator/
    services/
      catalog/
      orders/
      payments/
      users/
  packages/
    shared-config/
    shared-db/
    shared-logger/
    shared-errors/
    shared-types/
  prisma/            # root prisma and per-domain schema split
  scripts/
  .github/workflows/ci.yml
```

## 3) Domain services & contracts
Each service exposes an OpenAPI contract (3.0.3). Example endpoints:
- **Catalog**: `GET /products`, `POST /products`, `PATCH/DELETE /products/{id}`, `POST /stock/reservations` (reserve stock), `POST /stock/reservations/{id}/release`.
- **Orders**: `POST /orders` (create draft), `POST /orders/{id}/confirm`, `GET /orders/{id}`.
- **Payments**: `POST /payments/charges` (idempotent charge), `POST /payments/refunds`.
- **Users**: `POST /auth/register`, `POST /auth/login`, `GET /me` (JWT bearer auth).

## 4) Orchestration workflows
**PlaceOrder (synchronous orchestration with compensation)**
1. Validate user & basket (Users, Catalog).
2. Reserve stock per item (Catalog) → capture `reservationIds[]`.
3. Create order draft & compute total (Orders).
4. Charge payment (Payments) with `Idempotency-Key = orderDraftId`.
5. Confirm order with `chargeId` (Orders); release unused reservations; emit `OrderConfirmed` event.
6. Compensation: on payment failure → release reservations & mark order `FAILED_PAYMENT`; on order persist failure after charge → attempt refund, release reservations, mark `INCONSISTENT` + alert.

## 5) Database model (single DB, schema-per-domain)
- `users.accounts(id pk, email unique, password_hash, role, created_at)`
- `catalog.products(id pk, sku unique, name, price_cents, currency, stock_on_hand)`
- `catalog.stock_reservations(id pk, product_id fk, qty, status, expires_at)`
- `orders.orders(id pk, user_id fk, total_cents, currency, status, charge_id, created_at)`
- `orders.order_lines(id pk, order_id fk, product_id fk, qty, unit_price_cents)`
- `payments.charges(id pk, order_id fk, amount_cents, currency, status, provider_ref, idempotency_key unique)`

Constraints & indices: FK cascades; unique `(sku, idempotency_key)`; check `qty > 0`; partial index on `stock_reservations(status='ACTIVE')` for fast release.

## 6) Cross-cutting concerns
- Validation: Zod schemas per endpoint; standardized error JSON `{ code, message, details? }`.
- Authn/z: JWT bearer; role-based guards in gateway & services.
- Idempotency: `Idempotency-Key` header required for `POST /payments/charges`.
- Observability: pino logs with correlation ID; `/metrics` Prometheus; request timing.
- Resilience: retries with exponential backoff for remote-like calls; circuit breaker stub.
- Caching (optional): product list/details cached in Redis 60s.
- Rate limiting: 60 req/min/IP in gateway.
- OpenAPI docs served at `/docs` per service.

## 7) Non-functional targets
- p50 latency for `/checkout/place-order` < 300ms (local dev).
- Graceful shutdown & retry for catalog reservations and payments.
- Security: Argon2 password hashes; JWT signed with rotating secret.
- Quality: ≥80% coverage of service logic; TypeScript strict; ESLint clean.

## 8) Testing & acceptance (hooks)
- **Unit**: stock reserve/release semantics; idempotent payment charge; order total and status transitions.
- **Integration**: `PlaceOrder` happy path → 201 CONFIRMED; payment failure → 409 + reservations released; idempotent `PlaceOrder` retries → single charge.
- **Contract**: OpenAPI backward compatibility checks per service.

## 9) Scaffolding commands
- `pnpm init -w` then create workspaces under `apps/` and `packages/`.
- Dependencies: `pnpm add fastify zod zod-openapi pino @prisma/client` and dev deps `prisma typescript ts-node tsx jest ts-jest supertest eslint`.
- Prisma: `npx prisma init`; model schemas; `npx prisma migrate dev --name init`.
- Seed: `pnpm run seed` to create users, products, and stock.
- Dev: `pnpm -r --parallel dev` to run gateway, orchestrator, and services.

## 10) API Gateway (optional but recommended)
- Routes: `POST /api/checkout/place-order` → orchestrator; `GET /api/products` → catalog; `POST /api/auth/login` → users.
- Middleware: JWT verify (except login/register), rate limit, audit log (userId, route, status, duration).

## 11) Design tips (for codegen assistants)
- Prefer composition over inheritance; keep pure domain functions.
- Use service interfaces in orchestrator (`ICatalog`, `IPayments`, `IOrders`) to allow future process splits without changing orchestration logic.
- Implement retry/backoff wrappers around cross-service calls; stub circuit breaker.
- Optional outbox table in Orders for `OrderConfirmed` events.

## 12) SAAM/ATAM rationale snapshot
- **SAAM scenarios**: performance (p50 latency target), modifiability (clear service boundaries + contracts), security (JWT/argon2), reliability (compensation & retries), testability (contract + E2E hooks).
- **ATAM tradeoffs**: single-DB favors operational simplicity and consistency over strict physical isolation; coarse services reduce chattiness and latency; orchestration centralizes workflow logic, accepting a potential single coordination hotspot but enabling clearer compensation paths.
- Not microservices: avoids discovery/mesh/trace overhead at current scale while keeping boundaries ready for future extraction.
