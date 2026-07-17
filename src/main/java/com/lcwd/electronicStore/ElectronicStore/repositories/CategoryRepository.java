package com.lcwd.electronicStore.ElectronicStore.repositories;

/*
Purpose:
Provides database access and search operations for product categories.
*/
import com.lcwd.electronicStore.ElectronicStore.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String>
{
    List<Category> findByTitleContaining(String keyword);
}
