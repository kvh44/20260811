package com.example._0260811.controller;

import com.example._0260811.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    @GetMapping
    public List<User> listUsers() {
        return List.of(
                new User(1L, "Alice"),
                new User(2L, "Bob")
        );
    }
}
