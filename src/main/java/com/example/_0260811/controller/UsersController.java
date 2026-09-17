package com.example._0260811.controller;

import com.example._0260811.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User operations")
public class UsersController {

    @GetMapping
    @Operation(summary = "List users", description = "Returns all sample users.")
    public List<User> listUsers() {
        return List.of(
                new User(1L, "Alice"),
                new User(2L, "Bob"),
                new User(3L, "Charlie")
        );
    }
}
