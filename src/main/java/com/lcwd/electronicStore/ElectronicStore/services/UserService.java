package com.lcwd.electronicStore.ElectronicStore.services;

/*
Purpose:
Defines user profile, registration, lookup, and admin user operations.
*/
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto UpdateUser(UserDto userDto, String userid);

    void DeleteUser(String userid);

    UserDto getSingleUser(String userid);

    PageableResponse<UserDto> getAllUser(int pageNumber, int pageSize, String sortBy, String sortDirection);
    UserDto getUserByEmail(String email);
    List<UserDto> searchUser(String keyword);

}
