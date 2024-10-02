package org.example.statelessspringsecurity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.statelessspringsecurity.enums.UserRole;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public User(String email, UserRole userRole) {
        this.email = email;
        this.userRole = userRole;
    }
}
