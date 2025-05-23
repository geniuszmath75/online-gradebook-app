package org.example.onlinegradebookapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {
    private String[] fieldNames;

    // Initialize validator
    @Override
    public void initialize(AtLeastOneField constraintAnnotation) {
        this.fieldNames = constraintAnnotation.fields();
    }

    // Check if at least one of given fields is not null
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if(value == null) return false;

        try {
            for (String fieldName : fieldNames) {
                Field field = value.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                if(fieldValue != null) {
                    return true;
                }
            }
        } catch(Exception e) {
            return false;
        }

        return false;
    }
}
