package com.example.shared.model.player;

import com.example.shared.model.property.Property;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private int balance;

    private int position;

    private boolean isInJail;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> ownedProperties= new ArrayList<>();

}