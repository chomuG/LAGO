package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private String nickname;

    private String personality;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
