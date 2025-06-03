package com.example.application.entity;

import com.example.application.util.enums.GameState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID gameId;

    @Column(name = "game_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState gameState = GameState.STARTED;

    @OneToMany(
            mappedBy = "game",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @OrderColumn(name = "player_order")
    @ToString.Exclude
    private List<Player> players = new ArrayList<>();

    private int currentPlayerIndex = 0;

    @Column(updatable = false)
    @CreationTimestamp
    private String createdTime;

    public void addPlayer(Player player) {
        if (player == null) return;

        if (this.players.size() >= 4) {
            throw new IllegalStateException("Maximum players reached");
        }

        if (!this.players.contains(player)) {
            player.setGame(this);
            this.players.add(player);
        }
    }

    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }
}