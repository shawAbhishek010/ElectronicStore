package com.lcwd.electronicStore.ElectronicStore.entities;

/*
Purpose:
Represents an application account with profile data, role, password, and orders.
*/
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter // from lombok dependency..
@NoArgsConstructor
@AllArgsConstructor
@Builder
//It automatically generates a builder pattern for your class, making it easier to create objects, especially when the class has many fields or optional parameters.

@Entity
@Table(name = "User")
public class User {
    @Id
    private String userId;
    @Column(name = "userName")
    private String name;
    @Column(name = " userEmail", unique = true)
    private String email;
    private String password;
    private String role = "ROLE_USER";
    private String gender;
    @Column(length = 2000)
    private String about;
    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    private List<Order> orders=new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void normalizeRole() {
        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        }
    }
}
