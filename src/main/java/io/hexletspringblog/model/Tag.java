package io.hexletspringblog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener.class)
public class Tag implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    List<Post> posts = new ArrayList<>();

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    public void addPost(Post post) {
        posts.add(post);
        post.getTags().add(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.getTags().remove(this);
    }
}
