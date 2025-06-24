package com.example.application.entity;

import com.example.application.util.data.PropertyData;
import com.example.application.util.enums.PlayerColors;
import com.example.application.util.enums.PlayerState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    private UUID playerId;

    private String playerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Enumerated(EnumType.STRING)
    private PlayerColors color;

    private Integer balance = 1500;

    private Integer position = 0;

    @Column(nullable = false)
    private Integer inJail_Turns = 0;

    @Column(nullable = false)
    private Boolean isBot = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "owned_properties", joinColumns = @JoinColumn(name = "player_id"))
    private List<PropertyData> ownedProperties = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PlayerState playerState = PlayerState.IDLE;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdTime;

    public void addProperty(PropertyData property) {
        if (property == null) {
            throw new IllegalArgumentException("Invalid property");
        }
        if (!ownedProperties.contains(property)) {
            ownedProperties.add(property);
        }
    }

}
