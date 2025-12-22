# SAL (Storage Abstraction Layer)

Spring Boot + JdbcTemplate project implementing:
- SAL metadata model (Oracle): SAL_METADATA + SAL_METADATA_HIST + indexes
- Stream-based upload/download endpoints
- Storage handler abstraction (factory + handler)
- Default filesystem bytes storage
- External Entitlements hook (NOOP by default; HTTP hook in profile 'entitlements')

## Run
1. Ensure Oracle JDBC driver is on the classpath (ojdbc11 via internal repo).
2. Run DDL: `src/main/resources/db/schema-oracle.sql`
3. Configure datasource in `src/main/resources/application.yml`
4. Start: `mvn spring-boot:run`
