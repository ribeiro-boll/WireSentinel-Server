package com.bolota.wiresentinelserver.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SystemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private UUID uuid;
    @Column(unique = true)
    private String mockName;
    public SystemEntity(UUID uuidGen, String mockName) {
        this.uuid = uuidGen;
        this.mockName = mockName;
    }
}
