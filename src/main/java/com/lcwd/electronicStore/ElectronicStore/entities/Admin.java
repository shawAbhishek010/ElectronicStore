package com.lcwd.electronicStore.ElectronicStore.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Purpose:
Stores a dedicated copy of admin account data while authentication continues to use User.
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "admins")
public class Admin {
    @Id
    private String adminId;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String password;
    private String gender;

    @Column(length = 2000)
    private String about;
}
