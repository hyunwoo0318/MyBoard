package Lim.boardApp.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "emailParam", description = "이메일 인증 파라미터")
@Getter
@Setter
public class EmailParam {

    @Schema(description = "이메일 주소", example = "hyunwoo0318@naver.com")
    private String email;
}
