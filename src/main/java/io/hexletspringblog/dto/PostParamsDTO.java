package io.hexletspringblog.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PostParamsDTO {
    private String nameCont;
    private Long authorId;
    private LocalDate createdAtGt;
    private LocalDate createdAtLt;

    /**
     * Фильтр по признаку публикации: {@code true} — только опубликованные, {@code false} — только черновики.
     * Без JWT параметр игнорируется: список всегда только из опубликованных.
     */
    private Boolean published;
}
