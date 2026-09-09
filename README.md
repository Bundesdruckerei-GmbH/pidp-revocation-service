# Revocation Service

## Description
The Revocation Service provides an API to revoke issued PIDs. It holds status information about issued PIDs and updates statuses on the Status List Service in case of revocation.

## Getting Started
### Prerequisites
In order to use the Revocation Service, it requires the following dependent services to be running and be configured.
- Postgres database (initialized with the Flyway scripts located in [migrations](db/migrations))
- Status-List Service

These services can be configured by setting the following application properties:
```yaml
spring:
  datasource:
    # datasource configuration

statuslistservice:
  restclient:
    # status-list service configuration
```

The API key to access the Revocation Service can be configured by setting the following application properties:
```yaml
revocation:
  api-key: # API key for accessing the revocation service
```

### Installation
The installation is performed via the Maven Wrapper:
```shell
./mvnw install
```

Without running the tests:
```shell
./mvnw install -DskipTests
```

### Start
The Revocation Service can be started via Maven and Spring:
```shell
./mvnw spring-boot:run
```

### Test
The service provides JUnit unit tests and integration tests using SpringBoot and RestAssured to ensure that the code works as expected. The tests are executed in the installation process and can also be executed directly via Maven:
```shell
./mvnw verify
```

## Usage
The revocation service offers the following APIs:
- [PID Master Token Lifecycle API](docs/api/revocation-service-pmt.openapi.yaml)
- [PID Credential Lifecycle API](docs/api/revocation-service-pid-credential-lifecycle.openapi.yaml)

