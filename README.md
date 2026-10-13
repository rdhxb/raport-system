# raport-system

Monorepo (Maven multi-module): `krs-service` + `raport-service`.
Każdy serwis ma własną bazę PostgreSQL, komunikacja między serwisami przez RabbitMQ.

| Kontener         | Port na hoście | Opis                          |
|------------------|----------------|-------------------------------|
| krs-service      | 8081           | aplikacja                     |
| raport-service   | 8082           | aplikacja                     |
| krs-db           | 5433           | PostgreSQL dla krs-service    |
| raport-db        | 5434           | PostgreSQL dla raport-service |
| rabbitmq         | 5672 / 15672   | AMQP / panel zarządzania      |

Porty, nazwy baz, użytkownicy i hasła są w pliku `.env` (nie trafia do gita).

## Pierwsze uruchomienie

```bash
cp .env.example .env                # ustaw hasła
docker compose up --build -d        # zbuduj obrazy i odpal wszystko w tle
docker compose ps                   # status kontenerów
```

## Codzienne komendy

```bash
docker compose up -d                          # start (bez przebudowy)
docker compose up --build -d krs-service      # przebuduj i podmień jeden serwis po zmianach w kodzie
docker compose logs -f krs-service            # logi na żywo (Ctrl+C wychodzi)
docker compose stop                           # zatrzymaj (kontenery zostają)
docker compose down                           # zatrzymaj i usuń kontenery (dane w bazach zostają)
docker compose down -v                        # j.w. + usuń wolumeny = czyste bazy i kolejki
```

## Dostęp do usług

- RabbitMQ panel: http://localhost:15672 (login/hasło: `RABBITMQ_USER` / `RABBITMQ_PASSWORD` z `.env`)
- Baza krs: `docker exec -it krs-db psql -U krs_user -d krs_db`
- Baza raport: `docker exec -it raport-db psql -U raport_user -d raport_db`
- Z IDE (DataGrip/IntelliJ): `localhost:5433` (krs) i `localhost:5434` (raport)

## Uruchamianie serwisu z IDE zamiast w Dockerze

Odpal tylko infrastrukturę i zatrzymaj kontener serwisu, który chcesz debugować:

```bash
docker compose up -d krs-db raport-db rabbitmq
```

Następnie uruchom `KrsServiceApplication` / `RaportServiceApplication` z IDE.
Wartości domyślne w `application.properties` odpowiadają `.env` (localhost + porty z tabeli),
więc nic nie trzeba ustawiać.

## Build bez Dockera

```bash
./mvnw clean package -DskipTests              # oba moduły
./mvnw -pl krs-service -am package -DskipTests  # tylko jeden
```
