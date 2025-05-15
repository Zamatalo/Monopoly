package com.example.application.entity;

import com.example.application.util.enums.PlayerColors;
import com.example.application.util.PropertyData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private UUID playerId;

    private String playerName;

    @Enumerated(EnumType.STRING)
    private PlayerColors color;

    private int balance = 1500;
    private int position = 0;
    private boolean inJail = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ownedProperties", joinColumns = @JoinColumn(name = "player_id"))
    private List<PropertyData> ownedProperties = new ArrayList<>();

    public void addProperty(PropertyData property) {
        if (property != null) {
            ownedProperties.add(property);
        } else {
            throw new IllegalArgumentException("Invalid property");
        }
    }
}