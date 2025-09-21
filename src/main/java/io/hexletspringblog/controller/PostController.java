package io.hexletspringblog.controller;

import io.hexletspringblog.model.Post;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@RestController
public class PostController {
    // Хранилище добавленных постов, то есть обычный список
    private List<Post> posts = new ArrayList<>();

    @GetMapping("/posts") // Список постов
    public List<Post> index(@RequestParam(defaultValue = "10") Integer limit) {
        return posts.stream().limit(limit).toList();
    }

    @PostMapping("/posts") // Создание поста
    public Post create(@RequestBody Post post) {
        posts.add(post);
        return post;
    }

    @GetMapping("/posts/{id}") // Вывод поста
    public Optional<Post> show(@PathVariable String id) {
        var post = posts.stream()
                .filter(p -> p.getTitle().equals(id))
                .findFirst();
        return post;
    }

    @PutMapping("/posts/{id}") // Обновление поста
    public Post update(@PathVariable String id, @RequestBody Post data) {
        var maybePost = posts.stream()
                .filter(p -> p.getTitle().equals(id))
                .findFirst();
        if (maybePost.isPresent()) {
            var post = maybePost.get();
            post.setTitle(data.getTitle());
            post.setContent(data.getContent());
            return post;
        }
        return data;
    }

    @DeleteMapping("/pages/{id}") // Удаление поста
    public void destroy(@PathVariable String id) {
        posts.removeIf(p -> p.getTitle().equals(id));
    }
}
