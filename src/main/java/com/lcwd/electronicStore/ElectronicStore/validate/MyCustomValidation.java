package com.lcwd.electronicStore.ElectronicStore.validate;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
//  This means this custom annotation will be included in JavaDocs
// (useful for documentation purposes)

@Constraint(validatedBy = MyValidator.class)
//  This tells Spring/Java Validation which class will handle validation logic
//  MyValidator.class = custom validator where you write logic (isValid method)

@Target({ ElementType.FIELD, ElementType.PARAMETER })
// This defines where this annotation can be used
// FIELD → on variables (e.g., inside DTO class)
// PARAMETER → on method parameters (e.g., in controller)

@Retention(RetentionPolicy.RUNTIME)
//  This makes the annotation available at runtime
//  REQUIRED because validation happens at runtime (Spring reads it during execution)
public @interface MyCustomValidation {

    String message() default "Invalid value";// this will be default message

    Class<?>[] groups() default {}; //“Run this validation only for certain cases (groups), not always.”

    Class<? extends Payload>[] payload() default {};// additional information about annotation('metadata')

}
