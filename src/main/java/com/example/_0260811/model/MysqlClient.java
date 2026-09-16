package com.example._0260811.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dockerclient")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MysqlClient {
    @Id @NotNull Long id;

    @NotNull
    @Column(unique = true)
    String username;

    @NotNull
    @Column(unique = true)
    String email;

    @NotNull
    @Column(unique = true)
    String telephone;
}
