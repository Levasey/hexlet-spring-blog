package io.hexletspringblog.util;

import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;

import java.time.LocalDateTime;

public class TestDataFactory {

    public static Post createTestPost(User author) {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setContent("Test Content for the post");
        post.setSlug("test-post");
        post.setPublished(true);
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    public static Post createTestPost(User author, String title, String slug) {
        Post post = createTestPost(author);
        post.setTitle(title);
        post.setSlug(slug);
        return post;
    }
}
