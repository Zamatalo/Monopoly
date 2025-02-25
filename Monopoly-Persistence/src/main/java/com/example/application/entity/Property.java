package com.example.application.entity;

import com.example.application.PropertyNames;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Enumerated(EnumType.STRING)
    private PropertyNames propertyName;

    private int cost;

    private int rent;

    private boolean upgradable;
}