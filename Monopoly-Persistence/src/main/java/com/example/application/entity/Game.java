package com.example.application.entity;


import com.example.application.GameState;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID gameId;

    @Column(name = "game_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState gameState;

    /// if game is deleted, players should be deleted too
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Player> players;

    private int currentPlayerIndex;
}
