package Lim.boardApp.form;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TextCreateFormTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void init(){
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void clear(){
        validatorFactory.close();
    }
    
    @Test
    @DisplayName("정상적인 글 작성")
    public void normalTextCreate(){
        TextCreateForm form = new TextCreateForm("title1", "content1", "h1,h2,h3,h4");
        TextCreateForm formNoHashtags = new TextCreateForm("title1", "content1");
        Set<ConstraintViolation<TextCreateForm>> result1 = validator.validate(form);
        Set<ConstraintViolation<TextCreateForm>> result2 = validator.validate(formNoHashtags);

        assertThat(result1.size()).isEqualTo(0);
        assertThat(result2.size()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("제목이나 글 생략")
    public void missingTextCreate(){
        TextCreateForm formNoContent = new TextCreateForm("title1", "");
        TextCreateForm formNoTitle = new TextCreateForm("", "content1");

        Set<ConstraintViolation<TextCreateForm>> result1 = validator.validate(formNoContent);
        Set<ConstraintViolation<TextCreateForm>> result2 = validator.validate(formNoTitle);
        
        assertThat(result1.size()).isEqualTo(1);
        assertThat(result2.size()).isEqualTo(1);

        for (ConstraintViolation<TextCreateForm> c : result1) {
            assertThat(c.getMessageTemplate()).isEqualTo("내용을 입력해주세요.");            
        }
        for (ConstraintViolation<TextCreateForm> c : result2) {
            assertThat(c.getMessageTemplate()).isEqualTo("제목을 입력해주세요.");
        }
    }

}