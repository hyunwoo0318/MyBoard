package Lim.boardApp.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EmailConstraintsValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD } )
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomEmail {

    public String message() default "올바른 형식의 이메일을 작성해주세요.";

    public Class<?>[] groups() default {};
    public Class<? extends Payload>[] payload() default {};


}
