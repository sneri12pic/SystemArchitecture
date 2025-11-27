# SystemArchitecture

This repository contains a service-based (SOA) web system blueprint delivered as a single deployable with a PostgreSQL database. The goal is to demonstrate a maintainable, testable, and low-ops architecture that keeps clear domain boundaries while using orchestration for cross-domain workflows.

- Coarse-grained domain services: Catalog, Orders, Payments, Users.
- Lightweight Orchestrator service coordinates workflows such as PlaceOrder with compensation and idempotency.
- Single database with schema-per-domain; optional API Gateway and Redis for idempotency and caching.
- Node.js + TypeScript, Fastify/NestJS, Prisma, PostgreSQL, Zod validation, Jest tests.

## Build Spec
See [SOA_BUILD_SPEC.md](SOA_BUILD_SPEC.md) for the full build specification, contracts, workflows, non-functional targets, and testing hooks.

## Java prototype (orchestration demo)
A lightweight Java prototype of the PlaceOrder orchestration is available under `src/main/java/com/example/soa`. The code is organized by domain (`catalog`, `orders`, `payments`, `users`) plus `checkout` for the orchestrator and `core` for shared exceptions. It wires in-memory implementations of each service and exposes a `PrototypeApplication` entry point. Run it with:

```bash
mvn compile exec:java -Dexec.mainClass=com.example.soa.PrototypeApplication
```

Automated tests cover the happy path, payment failure compensation, and idempotent payment handling:

```bash
mvn test
```
