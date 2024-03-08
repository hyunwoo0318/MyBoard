package Lim.boardApp.common.annotation.emailvalidator;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailConstraintsValidator implements ConstraintValidator<CustomEmail, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value==null) return false;
        String regex = "^(.+)@(\\S+)$";
        return Pattern.compile(regex).matcher(value).matches();
    }
}
