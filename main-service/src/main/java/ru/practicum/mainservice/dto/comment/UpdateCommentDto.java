package ru.practicum.mainservice.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @NotNull
    private Long id;
    @NotBlank
    @Size(min = 1, max = 300)
    private String text;
}
