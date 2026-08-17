package com.lcwd.electronicStore.ElectronicStore.services.impl;

import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.UserDto;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.helper.PageableHelper;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import com.lcwd.electronicStore.ElectronicStore.services.AdminAccountSyncService;
import com.lcwd.electronicStore.ElectronicStore.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/*
Purpose:
This service contains user business logic like create, update, delete, and search.
Explanation:
JWT authentication uses this same service for register so the project does not duplicate user creation logic.
Flow:
Controllers call this service, the service works with UserRepository, and DTO/entity conversion keeps API models separate.
*/
@Service
public class UserServicesImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PageableHelper pageableHelper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminAccountSyncService adminAccountSyncService;

    @Override
    public UserDto createUser(UserDto userDto) {

        // Step 1: Generate unique id in string format.
        String userId = UUID.randomUUID().toString();
        userDto.setUserId(userId);

        // Step 2: Encrypt password before saving because plain text passwords must never be stored.
        userDto.setPassword(getPasswordForStorage(userDto.getPassword()));
        userDto.setRole(normalizeRole(userDto.getRole()));

        // Step 3: Convert dto->entity, save user, then convert entity->dto for API response.
        User user = dtoToEntity(userDto);
        User save = userRepository.save(user);
        adminAccountSyncService.syncUser(save);
        UserDto newDto = entityToDto(save);
        return newDto;
    }
    @Override
    public UserDto UpdateUser(UserDto userDto, String userid) {
        User user = userRepository.findById(userid).orElseThrow(() -> new ResourceNotFoundException("invalid userId"));
        // Step 1: Update editable profile fields from request dto.
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setAbout(userDto.getAbout());
        user.setGender(userDto.getGender());

        // Step 2: Encrypt password when it is newly provided.
        // BCrypt hashes are skipped here to avoid double-encoding during profile updates.
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(getPasswordForStorage(userDto.getPassword()));
        }

        // Step 3: Save updated user and return dto.
        User save = userRepository.save(user);
        adminAccountSyncService.syncUser(save);
        UserDto userDto1 = entityToDto(save);
        return userDto1;
    }

    @Override
    public void DeleteUser(String userid) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("userId is not valid here"));
        adminAccountSyncService.removeUser(userid);
        userRepository.delete(user);
    }

    @Override
    public UserDto getSingleUser(String userid) {
        User user = userRepository.findById(userid).orElseThrow(()-> new  ResourceNotFoundException("userId is not valid here"));
        return entityToDto(user);
    }

    @Override
    public PageableResponse<UserDto> getAllUser(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        Sort sort = (sortDirection.equalsIgnoreCase("desc"))?(Sort.by(sortBy).descending()):(Sort.by(sortBy).ascending());// Sort is a class
        // Create pageable object for pagination and sorting
        // pageNumber-1 because Spring uses 0-based indexing
        Pageable pageable = PageRequest.of(Math.max(pageNumber, 0), pageSize,sort);// Pageable is an interface
        Page<User> page = userRepository.findAll(pageable); // fetch paginated users
        // Convert Page<Entity> to Page<DTO> using helper class
        // This ensures clean API response and avoids exposing entity directly
        PageableResponse<UserDto> pageableResponse = pageableHelper.getPageableResponse(page, UserDto.class);
        return pageableResponse;
    }
//    In Spring Boot, Pageable is used to request a page, Page object holds data and metadata

    @Override
    public UserDto getUserByEmail(String email) {
        User invalidEmail = userRepository.findByEmail(email).orElseThrow(() -> new  ResourceNotFoundException("Invalid Email"));
        return entityToDto(invalidEmail);
    }

    @Override
    public List<UserDto> searchUser(String keyword) {
        List<User> word = userRepository.findByNameContaining(keyword);
        List<UserDto> wordList = word.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return wordList;

    }

    private User dtoToEntity(UserDto userDto) {
        return modelMapper.map(userDto,User.class);// modelMapper method
    }
    private UserDto entityToDto(User save) {
        UserDto userDto = UserDto.builder()
                .userId(save.getUserId())
                .email(save.getEmail())
                .name(save.getName())             // using builder to avoid diff constructors for method overloading
                .password(null)
                .role(normalizeRole(save.getRole()))
                .about(save.getAbout())
                .gender(save.getGender())
                .build();
        return  userDto;
    }

    private String getPasswordForStorage(String password) {
        // Step 1: Keep empty password handling simple; validation normally prevents blank passwords.
        if (password == null || password.isBlank()) {
            return password;
        }

        // Step 2: Detect existing BCrypt hashes so image/profile updates do not hash the hash again.
        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
            return password;
        }

        // Step 3: Convert raw password into a BCrypt hash before database storage.
        return passwordEncoder.encode(password);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String cleanedRole = role.trim().toUpperCase();
        return cleanedRole.startsWith("ROLE_") ? cleanedRole : "ROLE_" + cleanedRole;
    }
}


