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

### Профили

| Профиль       | Как включить | Назначение |
|---------------|--------------|------------|
| *(нет)*       | —            | H2, настройки из `application.yml` |
| `development` | `SPRING_PROFILES_ACTIVE=development ./gradlew bootRun` или `--args='--spring.profiles.active=development'` | H2 в файл `./data/devdb` — см. `application-development.yml` |
| `production`  | `SPRING_PROFILES_ACTIVE=production` + переменные `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL — см. `application-production.yml` |

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

В тестах активен профиль `test` (см. `build.gradle.kts` и `application-test.yml`).

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
