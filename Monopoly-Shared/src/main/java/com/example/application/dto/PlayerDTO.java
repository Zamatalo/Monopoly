package com.example.application.dto;


import com.example.application.PlayerNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDTO {
    private final List<PropertyDTO> ownedProperties = new ArrayList<>();
    private UUID playerId;
    private PlayerNames name;
    private int balance;
    private int position;
    private boolean inJail;


//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Data
//    public static class Position {
//        private float x;
//        private float y;
//        private float z;
//    }

}