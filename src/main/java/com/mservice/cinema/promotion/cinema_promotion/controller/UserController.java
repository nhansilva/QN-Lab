package com.mservice.cinema.promotion.cinema_promotion.controller;

import com.mservice.cinema.promotion.cinema_promotion.model.User;
import com.mservice.cinema.promotion.cinema_promotion.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided information. Uses distributed locking to prevent concurrent creation of users with the same name."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = "{\"id\": 1, \"name\": \"John Doe\", \"address\": \"123 Main Street, City, Country\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"name\": \"Name is required\", \"address\": \"Address must not exceed 255 characters\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User with this name already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"User with name 'John Doe' already exists\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Service unavailable - Redis lock acquisition failed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Unable to acquire lock for user creation. Please try again.\"}"
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<User> createUser(
        @Parameter(description = "User object to create", required = true)
        @Valid @RequestBody User user
    ) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    
    @Operation(
        summary = "Save or update user (upsert)",
        description = "Creates a new user if ID is not provided, or updates an existing user if ID is provided. Uses distributed locking for creation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found (for updates)"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User with this name already exists"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Service unavailable - Redis lock acquisition failed"
        )
    })
    @PostMapping("/save-or-update")
    public ResponseEntity<User> saveOrUpdateUser(
        @Parameter(description = "User object to save or update", required = true)
        @Valid @RequestBody User user
    ) {
        try {
            User savedUser = userService.saveOrUpdateUser(user);
            HttpStatus status = user.getId() != null ? HttpStatus.OK : HttpStatus.CREATED;
            return new ResponseEntity<>(savedUser, status);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "Update an existing user",
        description = "Updates an existing user with the specified ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Updated user information", required = true)
        @Valid @RequestBody User userDetails
    ) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all users in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(
        summary = "Delete user by ID",
        description = "Deletes a user by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 