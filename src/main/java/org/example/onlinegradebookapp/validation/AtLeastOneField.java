package org.example.onlinegradebookapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {AtLeastOneFieldValidator.class})
public @interface AtLeastOneField {
    String message() default "At least one of the specified fields must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String[] fields();
}
