package com.example.application.repo;

import com.example.application.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerRepo extends JpaRepository<Player, UUID> {
}
