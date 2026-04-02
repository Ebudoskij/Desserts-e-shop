package com.ebudoskij.dessert_shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "login_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfo implements Persistable<Long> {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This links the id of this entity to the id of the User entity
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Short failedAttempts = 0;

    @Column(nullable = false)
    private Boolean locked = false;

    private Instant lockDate;

    @Transient
    private boolean isNew = false;

    // Custom constructor for new users
    public LoginInfo(User user) {
        this.user = user;
        this.id = user.getId();
        this.failedAttempts = 0;
        this.locked = false;
        this.isNew = true;
    }

    @Override
    public boolean isNew() {
        return this.isNew || this.id == null;
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    private void trackNotNew() {
        this.isNew = false;
    }
}