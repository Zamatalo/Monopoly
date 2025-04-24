package com.example.application.entity;

import com.example.application.PlayerColors;
import com.example.application.PropertyNames;
import com.example.application.util.PropertyData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

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

    public void addProperty(PropertyNames propertyName) {
        PropertyData property = PropertyData.of(propertyName);
        if (property != null) {
            ownedProperties.add(property);
        } else {
            throw new IllegalArgumentException("Invalid property: " + propertyName);
        }
    }


}