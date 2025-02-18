package com.auth.wow.libre.infrastructure.entities.auth;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "server_publications")
public class ServerPublicationsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String img;
    private String title;
    private String description;

}
