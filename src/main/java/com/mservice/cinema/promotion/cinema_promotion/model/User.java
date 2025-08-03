package com.mservice.cinema.promotion.cinema_promotion.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
@Schema(description = "User entity representing a user in the system")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the user", example = "1")
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "User's full name", example = "John Doe", minLength = 2, maxLength = 100)
    private String name;
    
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", nullable = false)
    @Schema(description = "User's address", example = "123 Main Street, City, Country", maxLength = 255)
    private String address;
    
    // Default constructor
    public User() {}
    
    // Constructor with fields
    public User(String name, String address) {
        this.name = name;
        this.address = address;
    }
    
    // Constructor with all fields
    public User(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
} 