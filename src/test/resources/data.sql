-- Удалите старый INSERT и замените на:
INSERT INTO posts (title, content, published, user_id) VALUES
                                                           ('First Published Post', 'This is the content of the first published post', true, 1),
                                                           ('Second Published Post', 'This is the content of the second published post', true, 2),
                                                           ('Draft Post', 'This is a draft post content', false, 1);