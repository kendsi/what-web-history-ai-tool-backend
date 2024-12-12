package cap.team3.what.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CategoryUpdateRequestDto {

    @Schema(description = "기존 카테고리 이름", example = "게임")
    private String originalName;

    @Schema(description = "새로운 카테고리 이름", example = "테크")
    private String newName;

    public CategoryUpdateRequestDto(String originalName, String newName) {
        this.originalName = originalName;
        this.newName = newName;
    }
}
