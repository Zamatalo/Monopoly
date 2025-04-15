package com.example.application.repo;

import com.example.application.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepo extends JpaRepository<Game, UUID> {
    @Query("SELECT g FROM Game g WHERE g.gameState IN ('CREATED', 'STARTED')")
    List<Game> findActiveGames();

    @Query("SELECT COUNT(g) FROM Game g WHERE g.gameState IN ('CREATED', 'STARTED')")
    long countActiveGames();
}
