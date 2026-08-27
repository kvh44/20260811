package com.example._0260811.model;

import jakarta.validation.constraints.NotNull;

/**
 * Simple User record used by UsersController.
 */
public record User(@NotNull Long id, @NotNull String name) {
}
