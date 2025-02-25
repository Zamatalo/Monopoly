package com.example.application.dto;

import com.example.application.PropertyNames;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDTO {
    private PropertyNames propertyName;
    private int cost;
    private int rent;
    private UUID ownerId;
    private boolean upgradable;
}