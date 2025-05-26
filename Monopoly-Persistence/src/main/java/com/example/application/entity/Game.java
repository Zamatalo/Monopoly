package com.example.application.entity;


import com.example.application.util.enums.GameState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private GameState gameState = GameState.STARTED;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    private int currentPlayerIndex = 0;

    private boolean isTimerRunning = false;

    private String createdTime = LocalDateTime.now().toString();


    public void addPlayer(Player player) {
        if (this.players!=null&&!this.players.isEmpty()&&!this.players.contains(player) && this.players.size() < 4) {
            player.setGame(this);
            players.add(player);
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
}
