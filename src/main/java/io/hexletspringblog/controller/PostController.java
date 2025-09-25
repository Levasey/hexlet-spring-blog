package io.hexletspringblog.controller;

import io.hexletspringblog.model.Post;
import io.hexletspringblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Post> index() {
        return postRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@RequestBody Post post) {
        postRepository.save(post);
        return post;
    }

    @GetMapping("/{id}") // Вывод поста
    @ResponseStatus(HttpStatus.OK)
    public Post show(@PathVariable Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}") // Обновление поста
    @ResponseStatus(HttpStatus.OK)
    public Post update(@PathVariable Long id, @RequestBody Post data) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null; // Или можно генерировать исключение и обрабатывать его с помощью @ExceptionHandler
        } else {
            post.setTitle(data.getTitle());
            post.setContent(data.getContent());
            post.setAuthor(data.getAuthor());
            postRepository.save(post);
            return post;
        }
    }

    @DeleteMapping("/{id}")
    public void destroy(@PathVariable Long id) {
        postRepository.deleteById(id);
    }
}
