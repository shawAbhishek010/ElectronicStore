package com.lcwd.electronicStore.ElectronicStore.services;

import com.lcwd.electronicStore.ElectronicStore.dtos.CategoryDto;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;

import java.util.List;

public interface CategoryService
{

    //create
    CategoryDto create(CategoryDto categoryDto);

    //update
    CategoryDto update(CategoryDto categoryDto, String categoryId);

    //delete

    void delete(String categoryId);

    //get all
    PageableResponse<CategoryDto> getAll(int pageNumber, int pageSize, String sortBy, String sortDir);

    //get single category detail
    CategoryDto get(String categoryId);

    //search:
    List<CategoryDto> search(String keyword);
}
