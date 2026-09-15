package com.example._0260811.model;

import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mongoClients")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MongoClient {
    @Id
    private String id;
    @NotNull
    private String name;
    @NotNull
    private String email;
}
