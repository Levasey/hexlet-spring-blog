-- Очищаем таблицы перед вставкой
DELETE FROM posts;
DELETE FROM users;

-- Вставляем тестовых пользователей
INSERT INTO users (first_name, last_name, email, birthday, created_at, updated_at) VALUES
                                                                                       ('John', 'Doe', 'john@example.com', '1990-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                       ('Jane', 'Smith', 'jane@example.com', '1992-05-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Вставляем тестовые посты
INSERT INTO posts (title, content, author, published) VALUES
                                                          ('First Published Post', 'This is the content of the first published post', 'John Doe', true),
                                                          ('Second Published Post', 'This is the content of the second published post', 'Jane Smith', true),
                                                          ('Draft Post', 'This is a draft post content', 'John Doe', false);