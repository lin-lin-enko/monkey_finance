package com.lin.monkey_finance.domain.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "dev")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Size(min = 5, max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private  String username;

    @Size(min = 5, max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private  String email;

    @Size(min = 5, max = 100)
    @Column(nullable = false, length = 100)
    private  String password;

    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private  String name;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Generated(event = {EventType.UPDATE, EventType.INSERT})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Generated(event = {EventType.UPDATE, EventType.INSERT})
    @Column(name = "password_updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime passwordUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.PENDING;

    protected User(){}

    public User(String username, String email, String password, String name, LocalDate dateOfBirth, UserStatus status){
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }

    public UUID getId(){
        return id;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public LocalDate getDateOfBirth(){
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getPasswordUpdatedAt() {
        return passwordUpdatedAt;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
