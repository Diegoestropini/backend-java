# FinancialClose Simulator

Backend de simulacion de cierre contable para cooperativa de credito.

## Entregables incluidos

- Codigo fuente completo (Spring Boot).
- Scripts SQL de base de datos:
  - `src/main/resources/db/migration/V1__init_schema.sql`
  - `scripts/schema.sql`
- CSV de prueba (10000+ lineas):
  - `data/sample-transactions-10000.csv`
- Coleccion Postman:
  - `postman_collection.json`

## Stack

- Java 21
- Spring Boot 3.3.8
- PostgreSQL (objetivo principal)
- Flyway
- OpenAPI/Swagger (`/swagger-ui.html`)

## Instrucciones para compilar y ejecutar

1. Crear la base en PostgreSQL:

```sql
CREATE DATABASE financial_close;
```

2. Ajustar credenciales en `src/main/resources/application.yml` si corresponde.

3. Compilar:

```bash
mvn clean package
```

4. Ejecutar:

```bash
mvn spring-boot:run
```

5. Swagger:

- `http://localhost:8080/swagger-ui.html`

## Arquitectura y patrones de diseno

Arquitectura por capas:

- `controller`: API REST.
- `service`: casos de uso y reglas de negocio.
- `repository`: acceso a datos JPA + SQL nativo de agregacion.
- `domain/entity`: modelo persistente.
- `dto`: contratos de entrada/salida.

Patrones usados:

- `Strategy`: validaciones por tipo de producto (`AHORRO`, `CREDITO`, `INVERSION`) en `service/validation`.
- `Factory`: `ValidationStrategyFactory` selecciona estrategia segun `ProductType`.
- `Builder`: `CloseSummaryBuilder` arma el resumen agregado de cierre.

## Justificacion del modelo de datos

- `BIGSERIAL/BIGINT` en claves: escalable para volumen alto de transacciones.
- `NUMERIC(18,2)` en montos: evita errores de precision de punto flotante en montos financieros.
- `DATE` en `tx_date` y `close_date`: alineado a logica de cierre diario.
- `UNIQUE (close_date, branch_id, product_id)` en `daily_close`: garantiza un unico resumen por combinacion.
- FKs entre transacciones/cierre y catalogos: integridad referencial.
- Indices:
  - `transaction_record(tx_date, status)` para cierre diario por fecha/estado.
  - `transaction_record(branch_id, tx_date)` y `transaction_record(product_id, tx_date)` para filtros operativos.
  - `daily_close(close_date)` para consulta de resumen diario.

## Endpoints

- `POST /api/upload` (multipart CSV)
- `GET /api/transactions` (filtros + paginacion)
- `GET /api/close-summary` (resumen por fecha)
- `POST /api/process-close` (ejecucion manual del cierre)

### Nota de idempotencia de cierre

El cierre diario es idempotente por estrategia `DELETE + INSERT` en una misma transaccion:

1. se eliminan los registros existentes de `daily_close` para la fecha solicitada,
2. se recalculan e insertan los agregados de esa fecha.

Ejecutar `POST /api/process-close` varias veces para el mismo dia no duplica totales.

## Pruebas de API (curl)

```bash
curl -X POST "http://localhost:8080/api/upload" -F "file=@data/sample-transactions-10000.csv"

curl "http://localhost:8080/api/transactions?date=2026-01-10&status=CONCILIADA&page=0&size=50"

curl -X POST "http://localhost:8080/api/process-close?date=2026-01-10"

curl "http://localhost:8080/api/close-summary?date=2026-01-10"
```

## Coleccion Postman

Importar `postman_collection.json` y usar variable:

- `baseUrl = http://localhost:8080`

## Testing de carga y tuning (1M registros)

Herramientas:

- API: `k6` o `JMeter` para `/api/upload` y `/api/process-close`.
- DB: `EXPLAIN ANALYZE`, `pg_stat_statements`.

Metricas a monitorear:

- Tiempo total de ingesta y cierre.
- Throughput (filas/seg).
- Latencia p95/p99.
- CPU, memoria, I/O, lock waits.
- Uso de pool JDBC.

Integridad financiera:

- Validar que `sum(net_total)` de `daily_close` coincida con la suma neta de `transaction_record` conciliadas del dia.
- Verificar que rerun de cierre no duplique montos (por estrategia `DELETE + INSERT`).

Ajustes si se degrada rendimiento:

- Incrementar batch size JDBC segun benchmark (ej. 1000 a 5000).
- Mantener agregacion en SQL set-based (sin loops por sucursal/producto).
- Evaluar particionado de `transaction_record` por fecha.
- Afinar indices compuestos segun patrones de consulta reales.
- Ajustar Hikari y parametros PostgreSQL (`work_mem`, `shared_buffers`, `effective_cache_size`).
