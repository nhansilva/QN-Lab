package com.mservice.cinema.promotion.cinema_promotion.service;

import com.mservice.cinema.promotion.cinema_promotion.exception.LockAcquisitionException;
import com.mservice.cinema.promotion.cinema_promotion.exception.UserAlreadyExistsException;
import com.mservice.cinema.promotion.cinema_promotion.model.User;
import com.mservice.cinema.promotion.cinema_promotion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final DistributedLockService distributedLockService;
    
    @Autowired
    public UserService(UserRepository userRepository, DistributedLockService distributedLockService) {
        this.userRepository = userRepository;
        this.distributedLockService = distributedLockService;
    }
    
    // Save or update user (upsert functionality) with concurrency protection
    @Transactional
    public User saveOrUpdateUser(User user) {
        if (user.getId() != null) {
            // Update existing user
            Optional<User> optionalUser = userRepository.findById(user.getId());
            if (optionalUser.isPresent()) {
                User existingUser = optionalUser.get();
                existingUser.setName(user.getName());
                existingUser.setAddress(user.getAddress());
                return userRepository.save(existingUser);
            } else {
                throw new RuntimeException("User not found with id: " + user.getId());
            }
        } else {
            // Create new user with concurrency protection
            return createUserWithConcurrencyProtection(user);
        }
    }
    
    // Create a new user with concurrency protection
    @Transactional
    public User createUser(User user) {
        return createUserWithConcurrencyProtection(user);
    }
    
    // Private method to handle concurrent user creation
    private User createUserWithConcurrencyProtection(User user) {
        // Use user name as the lock key to prevent concurrent creation of users with same name
        String lockKey = "user_creation:" + user.getName();
        DistributedLockService.LockResult lockResult = distributedLockService.acquireLock(lockKey);
        
        if (!lockResult.isAcquired()) {
            throw new LockAcquisitionException("Unable to acquire lock for user creation. Please try again.");
        }
        
        try {
            // Check if user with same name already exists (business rule)
            Optional<User> existingUser = userRepository.findByName(user.getName());
            if (existingUser.isPresent()) {
                throw new UserAlreadyExistsException("User with name '" + user.getName() + "' already exists");
            }
            
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User creation failed due to data integrity violation", e);
        } finally {
            // Always release the lock
            distributedLockService.releaseLock(lockResult.getLockKey(), lockResult.getLockValue());
        }
    }
    
    // Update an existing user
    public User updateUser(Long id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setName(userDetails.getName());
            existingUser.setAddress(userDetails.getAddress());
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }
    
    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Delete user by ID
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }
} 