package com.lcwd.electronicStore.ElectronicStore.repositories;

/*
Purpose:
Provides database access and catalog queries for products.
*/
import com.lcwd.electronicStore.ElectronicStore.entities.Category;
import com.lcwd.electronicStore.ElectronicStore.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    //search,custom methods
    Page<Product> findByTitleContaining(String subTitle, Pageable pageable);

    Page<Product> findByLiveTrue(Pageable pageable);

    Page<Product> findByCategory(Category category, Pageable pageable);

    List<Product> findByCategory(Category category);

    List<Product> findByTitleIgnoreCase(String title);

}
