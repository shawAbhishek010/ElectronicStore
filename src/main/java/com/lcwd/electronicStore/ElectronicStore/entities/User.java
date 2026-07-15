package com.lcwd.electronicStore.ElectronicStore.entities;

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
    private String gender;
    @Column(length = 2000)
    private String about;
    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    private List<Order> orders=new ArrayList<>();
}
