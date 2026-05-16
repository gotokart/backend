package com.gotokart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    // Never serialise the BCrypt hash back to clients. Accept it on inbound
    // requests (register / role updates) only.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String role; // ADMIN or USER

    // Soft-deactivation flag. The columnDefinition ensures MySQL adds the
    // column with a default of 1 (true) so existing rows backfill cleanly when
    // Hibernate's ddl-auto=update runs ALTER TABLE on first boot.
    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1 NOT NULL")
    private Boolean active = Boolean.TRUE;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (active == null) active = Boolean.TRUE;
        if (role == null || role.isBlank()) role = "USER";
    }
}
