# Betclic technical assessment - Harold Fritsch

## Tools used

| Tool           | Usage                                             |
|----------------|---------------------------------------------------|
| Ktor           | Framework                                         |
| Netty          | Network engine                                    |
| PostgreSQL     | Database                                          |
| ORM            | Exposed                                           |
| Authentication | **NONE**                                          |
| Tests          | Unit tests in test package (+ Postman collection) |

## How to start
Set postgreSQL server and database infos in resources/application.conf.

```
chmod +x gradlew
./gradlew run
```

## Requirements for deployment
- **Remove hardcoded configuration from resources/application.conf**
- Authentication

## Improvements
- Dockerization
- Paging results
- Creating a tournament entity to handle simultaneous ranking + a global one
- End-to-end automated testing
- Caching/pooling database