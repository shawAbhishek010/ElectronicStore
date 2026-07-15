package com.lcwd.electronicStore.ElectronicStore.controller;

import com.lcwd.electronicStore.ElectronicStore.dtos.ApiResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.UserDto;
import com.lcwd.electronicStore.ElectronicStore.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    // create
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto user = userService.createUser(userDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // update
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("userId") String userId, @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.UpdateUser(userDto, userId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // delete
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String userId) {
        userService.DeleteUser(userId);
        ApiResponse successfullyDeleted = ApiResponse.builder()
                .message("Successfully deleted")
                .status(HttpStatus.OK)
                .successs(true)
                .build();
        return new ResponseEntity<>(successfullyDeleted, HttpStatus.OK);
    }

    //get all
    @GetMapping("/getAll")
    public ResponseEntity<PageableResponse<UserDto>> getUser(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "name", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection
    ) {
        PageableResponse<UserDto> allUser = userService.getAllUser(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(allUser, HttpStatus.OK);
    }

    //get single
    @GetMapping("/getSingle/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable String userId) {
        UserDto singleUser = userService.getSingleUser(userId);
        return new ResponseEntity<>(singleUser, HttpStatus.OK);
    }

    //get by email
    @GetMapping("/getEmail/{emailId}")
    public ResponseEntity<UserDto> getEmailUser(@PathVariable String emailId) {
        UserDto userByEmail = userService.getUserByEmail(emailId);
        return new ResponseEntity<>(userByEmail, HttpStatus.OK);
    }

    //search user by keyword
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<UserDto>> SearchUser(@PathVariable String keyword) {
        List<UserDto> searchUser = userService.searchUser(keyword);
        return new ResponseEntity<>(searchUser, HttpStatus.OK);
    }

}
