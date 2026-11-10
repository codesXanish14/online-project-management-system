package com.oms.core.entity;

import com.oms.core.constants.AppRole;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private AppRole name;

    public Role() {
    }

    public Role(AppRole name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppRole getName() {
        return name;
    }

    public void setName(AppRole name) {
        this.name = name;
    }
}