# Raport System

> 🚧 **Work in progress.** The project is under active development. Data collection from KRS, the VAT White List, CRBR and CEIDG already works; more data sources are being added before the data is combined into a report.

A microservice system that builds a report about a Polish business from official public registers. You provide either a **KRS number** (company registered in the National Court Register) or a **NIP** (sole proprietorship), and the system collects data from:

- **KRS (National Court Register)** – company details, address, share capital and representation, via the Ministry of Justice API
- **VAT White List (Ministry of Finance)** – VAT status and registered bank accounts, looked up by NIP
- **CRBR (Central Register of Beneficial Owners)** – beneficial owners of the company and the other companies they are linked to, via the Ministry of Finance SOAP API
- **CEIDG (Central Register and Information on Economic Activity)** – sole proprietorships: owner, business status and dates, address, PKD codes and previous entries for the same NIP, via the `dane.biznes.gov.pl` API

| Report started from | Registers queried |
|---|---|
| KRS number | KRS, CRBR, VAT White List (NIP taken from the KRS data) |
| NIP | CEIDG, VAT White List |

Planned data sources:

- **SUDOP** (Public Aid Data System) – public aid received by the company
- **Financial statements** from the Ministry of Justice – access to this data has been requested

Services communicate asynchronously through **RabbitMQ**, and each service has its own **PostgreSQL** database.

## Architecture

```
           POST /raports/krs/{krs}     POST /raports/nip/{nip}
                  (company)             (sole proprietorship)
                      │                           │
                  ┌───▼───────────────────────────▼───┐
                  │          raport-service           │◄────────────────┐
                  └───┬───────────────────────────┬───┘                 │
                      │ KRS number                │ NIP                 │
                      ▼                           ▼                     │
              [raport-requests]            [nip-exchanger]              │
                  (fanout)                     (topic)                  │
               ┌──────┴──────┐             ┌──────┴──────┐              │ replies
               ▼             ▼             ▼ nip.*       ▼ nip.direct   │ (queue)
         ┌────────────┐┌────────────┐┌────────────┐┌────────────┐       │
         │krs-service ││crbr-service││ whitelist- ││   ceidg-   │       │
         │KRS API + DB││CRBR SOAP+DB││  service   ││  service   │       │
         │            ││            ││WL API + DB ││CEIDG API+DB│       │
         └─────┬──────┘└─────┬──────┘└─────┬──────┘└─────┬──────┘       │
               └─────────────┴─────────────┴─────────────┴──────────────┘
```

The `nip-exchanger` is a topic exchange. A NIP read from the KRS data is published with the routing key `nip.krs`, and a NIP sent directly by the client with `nip.direct`. `whitelist-service` listens on `nip.*` (both cases), `ceidg-service` only on `nip.direct`, so sole proprietorships are looked up in CEIDG and companies are not.

**Flow – report from a KRS number**

1. The client sends a KRS number to `raport-service` and gets back a `requestId`.
2. `raport-service` saves the report with the `KRS` and `CRBR` sources as `PENDING` and publishes a request to the `raport-requests` exchange.
3. `krs-service` fetches the current KRS extract, saves it to its database (or updates it if the register has a newer entry) and replies on the `replies` queue.
4. `raport-service` reads the NIP from the KRS data, adds the `WL` source and publishes the NIP to the `nip-exchanger` exchange (`nip.krs`).
5. `whitelist-service` queries the VAT White List API, saves the result and replies on the `replies` queue.
6. In parallel, `crbr-service` receives the same KRS number from the `raport-requests` (fanout) exchange. It fetches the company's beneficial owners from CRBR, then queries CRBR again by each owner's PESEL to find the other companies they are linked to. The result is saved as a request header (`crbr_data`) and a list of person–company links (`crbr_powiazanie`); data for a KRS number already in the database is replaced. PESEL numbers are masked in logs. After saving, `crbr-service` replies on the `replies` queue with a single `CrbrData` payload (request header + list of `CrbrPowiazanieData` links).

**Flow – report from a NIP (sole proprietorship)**

1. The client sends a NIP to `raport-service` and gets back a `requestId`.
2. `raport-service` saves the report with the `CEIDG` and `WL` sources as `PENDING` and publishes the NIP to the `nip-exchanger` exchange (`nip.direct`).
3. `ceidg-service` queries the CEIDG API by NIP. One NIP can have several CEIDG entries over time: the newest entry that is not deleted (`WYKRESLONY`) becomes the current business, the others are kept as its history. The data is saved to `ceidg-db` (data for a NIP already in the database is replaced) and sent back on the `replies` queue as a `CeidgData` payload. If CEIDG has no entry for the NIP, the reply has the status `NOT_FOUND`.
4. At the same time, `whitelist-service` receives the NIP (`nip.*`), queries the VAT White List and replies as in the KRS flow.

**Collecting replies**

`raport-service` receives the responses, matched by `requestId` and `source` (`KRS` / `WL` / `CRBR` / `CEIDG`). The `requestId` is passed along with each message, so concurrent requests don't mix their data. Each source of a report is stored with its own status and payload; when no source is `PENDING` any more, the report is marked `DONE`.

Every reply has a common format (`RepliesModel`) with a status (`SUCCESS` / `FAILED` / `NOT_FOUND`) and an error message, so failures in external APIs are reported back instead of being lost. In the report, a source is then `SUCCESS`, `FAILED` or `SKIPPED` (nothing found in the register).

## REST API (`raport-service`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/raports/krs/{krs}` | Starts a company report from a KRS number, returns `requestId` |
| `POST` | `/raports/nip/{nip}` | Starts a sole-proprietorship report from a NIP, returns `requestId` |
| `GET` | `/raports/{requestId}` | Report status (`IN_PROGRESS` / `DONE`), `generatedFrom` (`KRS` / `NIP`), the number it was started with, and every source with its status, error message and payload; `404` for an unknown `requestId` |

## Modules

| Module | Description | Port (host) |
|---|---|---|
| `raport-service` | REST entry point and orchestration of the flow | 8082 |
| `krs-service` | Client for the KRS API, stores company data | 8081 |
| `whiteList-service` | Client for the VAT White List API, stores VAT data | 8083 |
| `crbr-service` | SOAP client for the CRBR API, stores beneficial owners and their company links | 8085 |
| `ceidg-service` | Client for the CEIDG API, stores sole-proprietorship data and its history by NIP | 8087 |
| `common` | Shared message models and DTOs (`RepliesModel`, `KrsData`, `WlData`, `CrbrData`, `CeidgData`, …) | – |

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
cp .env.example .env        # set your own passwords and CEIDG_API_TOKEN
docker compose up --build
```

`ceidg-service` needs an API token for `dane.biznes.gov.pl` (`CEIDG_API_TOKEN` in `.env`). The token is read only from the environment – never commit it.

Start a report from a KRS number (company) or a NIP (sole proprietorship):

```bash
curl -X POST http://localhost:8082/raports/krs/0000012345
curl -X POST http://localhost:8082/raports/nip/1234563218
```

Both return a `requestId`. Check the report until its status is `DONE`:

```bash
curl http://localhost:8082/raports/{requestId}
```

The result of each step is also visible in the service logs. The RabbitMQ management panel is available at http://localhost:15672.

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
- [x] `ceidg-service`: sole proprietorships from CEIDG by NIP, current entry + history, storing data and replying to `raport-service`
- [x] Reports started from a NIP (sole proprietorship): CEIDG + VAT White List
- [x] Report status endpoint (`GET /raports/{requestId}`) with per-source status and payload
- [ ] `sudop-service`: public aid data from SUDOP
- [ ] Combine data from all sources into a single company report in `raport-service`
- [ ] Display the report on a web page
- [ ] Financial statements from the Ministry of Justice (access requested)

## Data sources

- KRS API – Ministry of Justice: `https://api-krs.ms.gov.pl`
- VAT White List API – Ministry of Finance: `https://wl-api.mf.gov.pl`
- CRBR API (SOAP) – Ministry of Finance: `https://bramka-crbr.mf.gov.pl`
- CEIDG API – Ministry of Development and Technology: `https://dane.biznes.gov.pl/api/ceidg/v3`
