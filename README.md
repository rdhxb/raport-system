# Raport System

> 🚧 **Work in progress.** The project is under active development. Data collection from KRS and the VAT White List already works; more data sources are being added before the data is combined into a report.

A microservice system that builds a report about a Polish company from official public registers. You provide a **KRS number**, and the system collects data from:

- **KRS (National Court Register)** – company details, address, share capital and representation, via the Ministry of Justice API
- **VAT White List (Ministry of Finance)** – VAT status and registered bank accounts, looked up by the company's NIP

Planned data sources:

- **CRBR** (Central Register of Beneficial Owners) – beneficial owners of the company
- **SUDOP** (Public Aid Data System) – public aid received by the company
- **Financial statements** from the Ministry of Justice – access to this data has been requested

Services communicate asynchronously through **RabbitMQ**, and each service has its own **PostgreSQL** database.

## Architecture

```
                 POST /krs/send/{krs}
                         │
                ┌────────▼────────┐
                │  raport-service │◄──────────────┐
                └────────┬────────┘               │
                         │ KRS number             │ replies
                         ▼                        │ (queue)
              [raport-requests exchange]          │
                         │                        │
                ┌────────▼────────┐               │
                │   krs-service   │───────────────┤
                │  KRS API + DB   │  company data │
                └─────────────────┘  incl. NIP    │
                                                  │
              raport-service extracts NIP         │
                         │                        │
                         ▼                        │
               [nip-exchanger exchange]           │
                         │                        │
                ┌────────▼────────┐               │
                │ whitelist-svc   │───────────────┘
                │  WL API + DB    │  VAT status, bank accounts
                └─────────────────┘
```

**Flow**

1. The client sends a KRS number to `raport-service`.
2. `raport-service` publishes a request to the `raport-requests` exchange.
3. `krs-service` fetches the current KRS extract, saves it to its database (or updates it if the register has a newer entry) and replies on the `replies` queue.
4. `raport-service` reads the NIP from the KRS data and publishes it to the `nip-exchanger` exchange.
5. `whitelist-service` queries the VAT White List API, saves the result and replies on the `replies` queue.
6. `raport-service` receives both responses, matched by `requestId` and `source` (`KRS` / `WL`).

Every reply has a common format (`RepliesModel`) with a status (`SUCCESS` / `FAILED`) and an error message, so failures in external APIs are reported back instead of being lost.

## Modules

| Module | Description | Port (host) |
|---|---|---|
| `raport-service` | REST entry point and orchestration of the flow | 8082 |
| `krs-service` | Client for the KRS API, stores company data | 8081 |
| `whiteList-service` | Client for the VAT White List API, stores VAT data | 8083 |
| `common` | Shared message models and DTOs (`RepliesModel`, `KrsData`, `WlData`, …) | – |

## Tech stack

- Java 21, Spring Boot 4
- Spring AMQP (RabbitMQ), Spring Data JPA / Hibernate
- PostgreSQL 17 (one database per service)
- Docker, Docker Compose (multi-stage builds, health checks)
- Maven multi-module project
- JUnit tests for the KRS and White List services

## Getting started

**Requirements:** Docker and Docker Compose.

```bash
git clone https://github.com/rdhxb/raport-system.git
cd raport-system
cp .env.example .env        # set your own passwords
docker compose up --build
```

Send a request with a KRS number:

```bash
curl -X POST http://localhost:8082/krs/send/0000012345
```

The result of each step is visible in the service logs. The RabbitMQ management panel is available at http://localhost:15672.

### Running tests

```bash
./mvnw test
```

## Roadmap

- [x] Multi-module project, Docker Compose setup with health checks
- [x] `krs-service`: KRS API client, mapping, storing and refreshing data
- [x] `whiteList-service`: VAT White List API client, mapping, storing data
- [x] Communication through RabbitMQ with a shared reply format and error handling
- [ ] `crbr-service`: beneficial owners from the CRBR register
- [ ] `sudop-service`: public aid data from SUDOP
- [ ] Combine data from all sources into a single company report in `raport-service`
- [ ] Display the report on a web page
- [ ] Financial statements from the Ministry of Justice (access requested)

## Data sources

- KRS API – Ministry of Justice: `https://api-krs.ms.gov.pl`
- VAT White List API – Ministry of Finance: `https://wl-api.mf.gov.pl`
