package com.lcwd.electronicStore.ElectronicStore.dtos;

import com.lcwd.electronicStore.ElectronicStore.validate.MyCustomValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter // from lombok dependency..
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserDto {
    private String userId;
    @Size(min = 3, max = 30, message = "Invalid Name!!!")
    private String name;
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email Expression")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "password is blank")
    private String password;
    @Size(min = 4, max = 6, message = "Invalid gender input")
    private String gender;
    @NotBlank(message = "write Something")
    private String about;
    @MyCustomValidation
    private String imageName;
}
