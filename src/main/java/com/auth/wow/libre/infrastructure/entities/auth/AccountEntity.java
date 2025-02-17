package com.auth.wow.libre.infrastructure.entities.auth;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "account")
public class AccountEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", unique = true)
    private String username;
    @Column(name = "sha_pass_hash")
    private String sha;
    @Column(name = "s")
    private String salt;
    @Column(name = "v")
    private String verifier;
    @Column(name = "email")
    private String email;
    @Column(name = "joindate")
    private LocalDate joinDate;
    @Column(name = "last_ip")
    private String lastIp;
    @Column(name = "failed_logins")
    private String failedLogins;
    private boolean locked;
    @Column(name = "last_login")
    private LocalDate lastLogin;
    private boolean online;
    private String expansion;
    @Column(name = "mutetime")
    private Long muteTime;
    @Column(name = "mutereason")
    private String muteReason;
    @Column(name = "muteby")
    private String muteBy;
    private String os;
    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    public void prePersist() {
        this.joinDate = LocalDate.now();
        this.failedLogins = "0";
        this.lastIp = "";
        this.os = "";
        this.muteBy = "";
        this.muteReason = "";
        this.muteTime = 0L;
    }

    public AccountEntity() {
    }
}
