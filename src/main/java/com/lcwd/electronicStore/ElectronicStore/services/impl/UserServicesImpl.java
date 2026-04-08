package com.lcwd.electronicStore.ElectronicStore.services.impl;

import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.UserDto;
import com.lcwd.electronicStore.ElectronicStore.entities.User;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.helper.PageableHelper;
import com.lcwd.electronicStore.ElectronicStore.repositories.UserRepository;
import com.lcwd.electronicStore.ElectronicStore.services.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServicesImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PageableHelper pageableHelper;

    @Value("${user.profile.image.path}")
    private String imagePath;

    private Logger logger = LoggerFactory.getLogger(UserServicesImpl.class);

    @Override
    public UserDto createUser(UserDto userDto) {

        // generate unique id in string format
        String userId = UUID.randomUUID().toString();
        userDto.setUserId(userId);
        // dto-> entity
        User user = dtoToEntity(userDto);
        User save = userRepository.save(user);
       // entity-> dto
        UserDto newDto = entityToDto(save);
        return newDto;
    }
    @Override
    public UserDto UpdateUser(UserDto userDto, String userid) {
        User user = userRepository.findById(userid).orElseThrow(() -> new ResourceNotFoundException("invalid userId"));
        user.setName(userDto.getName());
        user.setAbout(userDto.getAbout());
        user.setGender(userDto.getGender());
        user.setPassword(userDto.getPassword());
        user.setImageName(userDto.getImageName());
        User save = userRepository.save(user);
        UserDto userDto1 = entityToDto(save);
        return userDto1;
    }

    @Override
    public void DeleteUser(String userid) {
      User user = userRepository.findById(userid).orElseThrow(()-> new ResourceNotFoundException("userId is not valid here"));
        //delete user profile image
        //images/user/abc.jpeg
      String fullPath = imagePath+user.getImageName();
        try {
            Path path = Paths.get(imagePath, user.getImageName());
            Files.deleteIfExists(path);
        } catch (NoSuchFileException ex) {
            logger.info("User image not found in folder");
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize,sort);// Pageable is an interface
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

    // CONVERSION PURPOSES:
    private User dtoToEntity(UserDto userDto) {
//        User user = User.builder()
//                .userId(userDto.getUserId())
//                .email(userDto.getEmail())
//                .name(userDto.getName())
//                .password(userDto.getPassword())
//                .about(userDto.getAbout())
//                .gender(userDto.getGender())
//                .imageName(userDto.getImageName())
//                .build();
//        return user;
        return modelMapper.map(userDto,User.class);// modelMapper method
    }
    private UserDto entityToDto(User save) {
        UserDto userDto = UserDto.builder()
                .userId(save.getUserId())
                .email(save.getEmail())
                .name(save.getName())             // using builder to avoid diff constructors for method overloading
                .password(save.getPassword())
                .about(save.getAbout())
                .gender(save.getGender())
                .imageName(save.getImageName())
                .build();
        return  userDto;
    }
//    private UserDto entityToDto(User user) {
//        UserDto userDto = new UserDto();     // without  builder
//        userDto.setUserId(user.getUserId());
//        userDto.setEmail(user.getEmail());
//        userDto.setName(user.getName());
//        userDto.setPassword(user.getPassword());
//        userDto.setAbout(user.getAbout());
//        userDto.setGender(user.getGender());
//        userDto.setImageName(user.getImageName());
//
//        return userDto;
//    }
}


