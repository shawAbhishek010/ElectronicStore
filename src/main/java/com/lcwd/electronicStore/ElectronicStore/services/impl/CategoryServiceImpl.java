package com.lcwd.electronicStore.ElectronicStore.services.impl;
import com.lcwd.electronicStore.ElectronicStore.dtos.CategoryDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.entities.Category;
import com.lcwd.electronicStore.ElectronicStore.exceptions.ResourceNotFoundException;
import com.lcwd.electronicStore.ElectronicStore.helper.PageableHelper;
import com.lcwd.electronicStore.ElectronicStore.repositories.CategoryRepository;
import com.lcwd.electronicStore.ElectronicStore.services.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper mapper;
    @Autowired
    private PageableHelper pageableHelper;

    @Override
    public CategoryDto create(CategoryDto categoryDto) {
        //creating categoryId:randomly

        String categoryId = UUID.randomUUID().toString();
        categoryDto.setCategoryId(categoryId);
        Category category = mapper.map(categoryDto, Category.class);


        Category savedCategory = categoryRepository.save(category);
        return mapper.map(savedCategory, CategoryDto.class);
    }

    @Override
    public CategoryDto update(CategoryDto categoryDto, String categoryId) {

        //get category of given id
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found with given id !!"));
        //update category details
        category.setTitle(categoryDto.getTitle());
        category.setDescription(categoryDto.getDescription());
        category.setCoverImage(categoryDto.getCoverImage());
        Category updatedCategory = categoryRepository.save(category);
        return mapper.map(updatedCategory, CategoryDto.class);
    }

    @Override
    public void delete(String categoryId) {
        //get category of given id
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found with given id !!"));
        categoryRepository.delete(category);
    }

    @Override
    public PageableResponse<CategoryDto> getAll(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sort);
        Page<Category> page = categoryRepository.findAll(pageable);
        PageableResponse<CategoryDto> pageableResponse = pageableHelper.getPageableResponse(page, CategoryDto.class);
        return pageableResponse;
    }

    @Override
    public CategoryDto get(String categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found with given id !!"));
        return mapper.map(category, CategoryDto.class);
    }
    @Override
    public List<CategoryDto> search(String keyword) {
        List<Category> list = categoryRepository.findByTitleContaining(keyword);
        return list.stream()
                .map(cat -> mapper.map(cat, CategoryDto.class))
                .toList();
    }
}
