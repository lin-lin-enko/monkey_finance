package com.lin.monkey_finance.domain.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Generated;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "dev")
public class User {
    @Id
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

    @Generated
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    public User(){
    }

    public User(String username, String email, String password, String name, LocalDate dateOfBirth){
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
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
}
