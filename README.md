# Hexlet Spring Blog

Учебный блог на **Spring Boot**: REST API (посты, теги, комментарии, пользователи), JWT, JPA, Thymeleaf для страниц приветствия.

## Стек

- Java **21**, Spring Boot **3.5**
- Spring Data JPA, Validation, Security (OAuth2 Resource Server + JWT)
- MapStruct, Lombok, Thymeleaf
- БД по умолчанию: **H2** (см. `application.yml`); в зависимостях есть **PostgreSQL** для прод-профилей

## Требования

- JDK 21
- при необходимости: `chmod +x gradlew`

## Запуск

```bash
./gradlew bootRun
```

Приложение поднимается с настройками из `src/main/resources/application.yml` (H2, ключи RSA из `classpath:certs/`).

### Профили

- `development` — см. `application-development.yml`
- `test` — H2 in-memory, используется в автотестах

## Тесты

```bash
./gradlew test
```

Отчёт JaCoCo: `./gradlew jacocoTestReport` → `build/reports/jacoco/test/html/index.html`

## API (кратко)

| Область        | Базовый путь      | Примечание |
|----------------|-------------------|------------|
| Посты          | `/api/posts`      | **GET** списка и поста по id — без JWT; **POST/PUT/DELETE** — с JWT |
| Теги           | `/api/tags`       | **GET** — без JWT; создание/удаление — с JWT |
| Комментарии    | `/api/comments`   | все операции — с JWT                                        |
| Пользователи   | `/api/users`      | регистрация `POST /api/users/register` — публично |
| Вход           | `POST /api/login` | выдача JWT (RSA-ключи в `certs/`) |

Список постов поддерживает query-параметры пагинации (`page`, `size`) и фильтры из `PostParamsDTO` (например `authorId`, `nameCont`, `createdAtGt`, `createdAtLt`).

Страницы без API: `/`, `/about`, `/welcome`.

## Безопасность

- Stateless-сессии, **Bearer JWT** для защищённых эндпоинтов.
- Публичные маршруты настраиваются в `SecurityConfig`.
