package com.example.demowithtests.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_users")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SecurityUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "role")
    private String role;

    @Column(name = "password")
    private String password;

}