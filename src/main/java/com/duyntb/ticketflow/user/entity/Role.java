package com.duyntb.ticketflow.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String name;

    String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(
                    name = "role_id",
                    foreignKey = @ForeignKey(name = "fk_role_permissions_role")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "permission_id",
                    foreignKey = @ForeignKey(name = "fk_role_permissions_permission")
            )
    )
    private Set<Permission> permissions = new HashSet<>();
}
