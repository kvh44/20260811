package com.example._0260811.model;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MysqlClientValidationTest {

    @Test
    void newClientCanHaveNullGeneratedId() {
        MysqlClient client = MysqlClient.builder()
                .username("new-client")
                .email("new-client@example.com")
                .telephone("1234567890")
                .build();

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(client).isEmpty());
        }
    }
}
