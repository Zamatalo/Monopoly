package com.example.application.repo;

import com.example.application.entity.Player;
import com.example.application.util.data.PropertyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepo extends JpaRepository<Player, UUID> {
    @Query("""
                SELECT p FROM Player p
                JOIN p.ownedProperties op
                WHERE p.game.gameId = :gameId AND op = :property
            """)
    Optional<Player> findPlayerByGameIdAndProperty(@Param("gameId") UUID gameId, @Param("property") PropertyData property);
}
