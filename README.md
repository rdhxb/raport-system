# Raport System

> 🚧 **Work in progress.** The project is under active development. Data collection from KRS, the VAT White List and CRBR already works; more data sources are being added before the data is combined into a report.

A microservice system that builds a report about a Polish company from official public registers. You provide a **KRS number**, and the system collects data from:

- **KRS (National Court Register)** – company details, address, share capital and representation, via the Ministry of Justice API
- **VAT White List (Ministry of Finance)** – VAT status and registered bank accounts, looked up by the company's NIP
- **CRBR (Central Register of Beneficial Owners)** – beneficial owners of the company and the other companies they are linked to, via the Ministry of Finance SOAP API

Planned data sources:

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
                 (fanout)│                        │
           ┌─────────────┴──────────┐             │
  ┌────────▼────────┐      ┌────────▼────────┐    │
  │   krs-service   │      │  crbr-service   │    │
  │  KRS API + DB   │      │ CRBR SOAP + DB  │    │
  └────────┬────────┘      └────────┬────────┘    │
           │ company data           │ owners +    │
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
6. In parallel, `crbr-service` receives the same KRS number from the `raport-requests` (fanout) exchange. It fetches the company's beneficial owners from CRBR, then queries CRBR again by each owner's PESEL to find the other companies they are linked to. The result is saved as a request header (`crbr_data`) and a list of person–company links (`crbr_powiazanie`); data for a KRS number already in the database is replaced. PESEL numbers are masked in logs. After saving, `crbr-service` replies on the `replies` queue with a single `CrbrData` payload (request header + list of `CrbrPowiazanieData` links).
7. `raport-service` receives the responses, matched by `requestId` and `source` (`KRS` / `WL` / `CRBR`). The `requestId` is passed along with each message, so concurrent requests don't mix their data.

Every reply has a common format (`RepliesModel`) with a status (`SUCCESS` / `FAILED`) and an error message, so failures in external APIs are reported back instead of being lost.

## Modules

| Module | Description | Port (host) |
|---|---|---|
| `raport-service` | REST entry point and orchestration of the flow | 8082 |
| `krs-service` | Client for the KRS API, stores company data | 8081 |
| `whiteList-service` | Client for the VAT White List API, stores VAT data | 8083 |
| `crbr-service` | SOAP client for the CRBR API, stores beneficial owners and their company links | 8085 |
| `common` | Shared message models and DTOs (`RepliesModel`, `KrsData`, `WlData`, `CrbrData`, …) | – |

## Tech stack

- Java 21, Spring Boot 4
- Spring AMQP (RabbitMQ), Spring Data JPA / Hibernate
- Spring Web Services (SOAP client generated from WSDL) for CRBR
- PostgreSQL 17 (one database per service)
- Docker, Docker Compose (multi-stage builds, health checks)
- Maven multi-module project
- JUnit tests for the KRS, White List and CRBR services

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
- [x] `crbr-service`: beneficial owners from the CRBR register, linked companies, storing data and replying to `raport-service`
- [ ] `sudop-service`: public aid data from SUDOP
- [ ] Combine data from all sources into a single company report in `raport-service`
- [ ] Display the report on a web page
- [ ] Financial statements from the Ministry of Justice (access requested)

## Data sources

- KRS API – Ministry of Justice: `https://api-krs.ms.gov.pl`
- VAT White List API – Ministry of Finance: `https://wl-api.mf.gov.pl`
- CRBR API (SOAP) – Ministry of Finance: `https://bramka-crbr.mf.gov.pl`
