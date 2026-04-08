package com.lcwd.electronicStore.ElectronicStore.validate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
public class MyValidator implements ConstraintValidator<MyCustomValidation, String> {
    private static final List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");

    @Override
    public boolean isValid(String imageName, ConstraintValidatorContext context) {
        // Null or blank check
        if (imageName == null || imageName.trim().isEmpty()) {
            return false;
        }

        // Lowercase for comparison
        String lowerName = imageName.toLowerCase();

        // Check if it ends with a valid image extension
        for (String ext : allowedExtensions) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }

        return false; // if none matched
    }
}
