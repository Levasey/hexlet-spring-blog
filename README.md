# Hexlet Spring Blog

Учебный блог на **Spring Boot**: REST API (посты, теги, комментарии, пользователи), JWT, JPA, Thymeleaf для статических страниц.

## Стек

- Java **21**, Spring Boot **3.5**
- Spring Data JPA, Validation, Security (OAuth2 Resource Server + JWT)
- MapStruct, Lombok, Thymeleaf
- [springdoc-openapi](https://springdoc.org/) — OpenAPI 3 и Swagger UI
- БД по умолчанию: **H2** (`application.yml`); **PostgreSQL** — для профиля `production`

## Требования

- JDK 21
- при необходимости: `chmod +x gradlew`

## Запуск

```bash
./gradlew bootRun
```

По умолчанию поднимается с `application.yml`: встроенная H2, RSA-ключи JWT из `src/main/resources/certs/`.

#### RSA-ключи для JWT (новый разработчик / прод без ключей в репозитории)

Приложение читает пути из `rsa.private-key` и `rsa.public-key` в `application.yml` (по умолчанию `classpath:certs/private.pem` и `public.pem`). В учебном репозитории файлы могут лежать в `src/main/resources/certs/`; в продакшене ключи обычно **не коммитят** — их кладут в образ/секреты и подключают через переменные или внешние файлы.

**Локально сгенерировать пару (OpenSSL):**

```bash
chmod +x scripts/generate-rsa-jwt-keys.sh
./scripts/generate-rsa-jwt-keys.sh
```

Скрипт создаёт PKCS#8 private key и публичный ключ в PEM — тот же формат, что ожидает Spring. Чтобы положить ключи в другой каталог: `./scripts/generate-rsa-jwt-keys.sh /path/to/certs`, затем укажите пути в конфиге, например `file:/absolute/path/private.pem` (см. [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)).

**Вручную (эквивалент скрипта):**

```bash
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
openssl pkey -in private.pem -pubout -out public.pem
```

В тестах активен профиль `test` (H2 в памяти); JWT по-прежнему подписывается/проверяется теми же RSA-ключами из `classpath:certs/` (как в основном `application.yml`). Для `bootRun` без ключей в classpath приложение не стартует.

### Профили

| Профиль       | Как включить | Назначение |
|---------------|--------------|------------|
| *(нет)*       | —            | H2, настройки из `application.yml` |
| `development` | `SPRING_PROFILES_ACTIVE=development ./gradlew bootRun` или `--args='--spring.profiles.active=development'` | H2 в файл `./data/devdb` — см. `application-development.yml` |
| `production`  | `SPRING_PROFILES_ACTIVE=production` + переменные `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL — см. `application-production.yml` |

#### Переменные для `application-production.yml`

В `application-production.yml` подключение к БД задаётся через плейсхолдеры Spring:

| Переменная | Обязательность | Описание |
|------------|----------------|----------|
| `DB_URL` | **Обязательна в продакшене** (технически есть дефолт `jdbc:postgresql://localhost:5432/myapp`) | Полный JDBC URL PostgreSQL. |
| `DB_USERNAME` | То же (дефолт `postgres`) | Имя пользователя БД. |
| `DB_PASSWORD` | То же (дефолт `postgres`) | Пароль; в боевом окружении дефолт переопределять обязательно. |

Профиль включается так: `SPRING_PROFILES_ACTIVE=production`. В этом профиле `spring.jpa.hibernate.ddl-auto` = **validate** — таблицы должны уже существовать (миграции или однократный `update`). Пример всех переменных для локальной копии: [.env.example](.env.example).

### Docker (PostgreSQL + приложение)

Требуется [Docker](https://docs.docker.com/get-docker/) и Docker Compose v2.

```bash
docker compose up --build
```

- приложение: http://localhost:8080 (если порт занят: `APP_PORT=8081 docker compose up`)  
- PostgreSQL с хоста: `localhost:5433` (переопределение: `POSTGRES_HOST_PORT=5434 docker compose up`)  
- БД `myapp`, пользователь/пароль `postgres`/`postgres`  
- в `docker-compose.yml` задано `SPRING_JPA_HIBERNATE_DDL_AUTO=update`, чтобы схема создалась без отдельных миграций (в `production` по умолчанию стоит `validate`).

## OpenAPI и Swagger UI

После запуска приложения (порт по умолчанию **8080**):

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **Спецификация JSON:** http://localhost:8080/v3/api-docs  

В спецификацию попадают только пути `/api/**`. Защищённые операции используют схему **Bearer JWT**; токен можно получить через `POST /api/login`, затем нажать **Authorize** в Swagger UI и вставить токен.

**Postman:** Import → Link → указать URL `http://localhost:8080/v3/api-docs`.

Документация и схема JWT задаются в `OpenApiConfig`; публичные эндпоинты помечены так же, как в `SecurityConfig`.

## Тесты

```bash
./gradlew test
```

Отчёт JaCoCo: `./gradlew jacocoTestReport` → `build/reports/jacoco/test/html/index.html`

В тестах активен профиль `test` (см. `build.gradle.kts` и `application-test.yml`). Защищённые сценарии опираются на `@WithMockUser` или на реальную цепочку JWT (например `PostsApiSecurityIntegrationTest`); устаревшее отключение Security через `spring.security.enabled` в Boot 3.x здесь не применяется.

## API (кратко)

| Область       | Базовый путь       | Примечание |
|---------------|--------------------|------------|
| Посты         | `/api/posts`       | **GET** списка и поста по id — без JWT; **POST/PUT/DELETE** — с JWT |
| Теги          | `/api/tags`        | **GET** — без JWT; создание/удаление — с JWT |
| Комментарии   | `/api/comments`    | все операции — с JWT |
| Пользователи  | `/api/users`       | `POST /api/users/register` — публично; остальное — с JWT |
| Вход          | `POST /api/login`  | выдача JWT (RSA в `certs/`) |

Список постов: query-параметры пагинации `page`, `size` и фильтры из `PostParamsDTO` (например `authorId`, `nameCont`, `createdAtGt`, `createdAtLt`, `published`).

Без JWT ответ **GET** `/api/posts` всегда содержит только опубликованные посты (параметр `published` не ослабляет фильтр). С JWT можно передать `published=true` / `published=false` или не указывать параметр, чтобы получить и черновики, и опубликованные. **GET** `/api/posts/{id}` для черновика без JWT возвращает 404.

Страницы вне API: `/`, `/about`, `/welcome`.

## Безопасность

- Stateless-сессии, **Bearer JWT** для защищённых эндпоинтов.
- Публичные маршруты и Swagger/OpenAPI — в `SecurityConfig`.

В продакшене имеет смысл ограничить доступ к `/swagger-ui.html` и `/v3/api-docs` (отдельный профиль, сеть, аутентификация).

## CI

GitHub Actions: `.github/workflows/build.yml` — сборка, тесты и покрытие (в т.ч. SonarCloud при настроенных секретах).
