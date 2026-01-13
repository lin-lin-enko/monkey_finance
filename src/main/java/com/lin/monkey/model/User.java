package com.lin.monkey.model;

import jakarta.persistence.*;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLCITextType;
import org.hibernate.annotations.Type;

@Entity //means that it's a class that represents a table in the db
@Table(name = "users") //table to which i connect
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Type(PostgreSQLCITextType.class)
    @Column(nullable = false, unique = true, columnDefinition = "citext", length = 60)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
