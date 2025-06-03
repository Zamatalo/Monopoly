package com.example.application.util;

import com.example.application.util.enums.PropertyNames;
import jakarta.persistence.Embeddable;

import java.util.Map;
import java.util.Objects;

@Embeddable
public record PropertyData(
        String displayName,
        int boardPosition,
        int cost,
        boolean upgradable,
        boolean isOwned
) {
    public static final Map<PropertyNames, PropertyData> ALL = Map.ofEntries(
            // Brown
            Map.entry(PropertyNames.BROWN_1, new PropertyData("San Diego Drive", 1, 60, true,false)),
            Map.entry(PropertyNames.BROWN_2, new PropertyData("Kansas Drive", 3, 60, true,false)),

            // Light Blue
            Map.entry(PropertyNames.LIGHTBLUE_1, new PropertyData("Vermont Drive", 6, 120, true,false)),
            Map.entry(PropertyNames.LIGHTBLUE_2, new PropertyData("Phoenix Drive", 8, 130, true,false)),
            Map.entry(PropertyNames.LIGHTBLUE_3, new PropertyData("Boston Drive", 9, 150, true,false)),

            // Pink
            Map.entry(PropertyNames.PINK_1, new PropertyData("Olivia Gardens", 11, 140, true,false)),
            Map.entry(PropertyNames.PINK_2, new PropertyData("California Drive", 13, 160, true,false)),
            Map.entry(PropertyNames.PINK_3, new PropertyData("States Drive", 14, 140, true,false)),


            // Orange
            Map.entry(PropertyNames.ORANGE_1, new PropertyData("Bethany Drive", 16,  180, true,false)),
            Map.entry(PropertyNames.ORANGE_2, new PropertyData("New York Drive", 18,  200, true,false)),
            Map.entry(PropertyNames.ORANGE_3, new PropertyData("Atlanta Drive", 19,  240, true,false)),

            // Red
            Map.entry(PropertyNames.RED_1, new PropertyData("Almond Drive", 21,  200, true,false)),
            Map.entry(PropertyNames.RED_2, new PropertyData("Clement Drive", 23,  200, true,false)),
            Map.entry(PropertyNames.RED_3, new PropertyData("Pacific Drive", 24,  260, true,false)),

            // Yellow
            Map.entry(PropertyNames.YELLOW_1, new PropertyData("Rodeo Drive", 26,  260, true,false)),
            Map.entry(PropertyNames.YELLOW_2, new PropertyData("Nashville Drive", 27,  260, true,false)),
            Map.entry(PropertyNames.YELLOW_3, new PropertyData("Oakville", 29,  230, true,false)),

            // Green
            Map.entry(PropertyNames.GREEN_1, new PropertyData("Clement Drive", 31, 300, true,false)),
            Map.entry(PropertyNames.GREEN_2, new PropertyData("Atlantic Drive", 32,  320, true,false)),
            Map.entry(PropertyNames.GREEN_3, new PropertyData("Riverside", 34,  320, true,false)),

            // Blue
            Map.entry(PropertyNames.BLUE_1, new PropertyData("Folklore Heights", 38,  400, true,false)),
            Map.entry(PropertyNames.BLUE_2, new PropertyData("Salt Lake", 39, 350, true,false)),

            // Railroads
            Map.entry(PropertyNames.RAILROAD_1, new PropertyData("Beverly Railroad", 5,  60, false,false)),
            Map.entry(PropertyNames.RAILROAD_2, new PropertyData("Manhattan Railroad", 15,  200, false,false)),
            Map.entry(PropertyNames.RAILROAD_3, new PropertyData("Water Works", 25,  200, false,false)),
            Map.entry(PropertyNames.RAILROAD_4, new PropertyData("Short Line", 35,  200, false,false)),

            // Special Tiles
            // #TODO START should not be added as property
            Map.entry(PropertyNames.START, new PropertyData("Start", 0,  0, false,false)),
            Map.entry(PropertyNames.COMMUNITY_CHEST1, new PropertyData("Community Chest1", 2, 0,false,false)),
            Map.entry(PropertyNames.COMMUNITY_CHEST2, new PropertyData("Community Chest2", 17, 0,false,false)),
            Map.entry(PropertyNames.COMMUNITY_CHEST3, new PropertyData("Community Chest3", 33, 0,false,false)),

            Map.entry(PropertyNames.CHANCE1, new PropertyData("Chance1", 7,  0, false,false)),
            Map.entry(PropertyNames.CHANCE2, new PropertyData("Chance2", 22,  0, false,false)),
            Map.entry(PropertyNames.CHANCE3, new PropertyData("Chance3", 36,  0, false,false)),

            Map.entry(PropertyNames.INCOME_TAX, new PropertyData("Income Tax", 4,  0, false,false)),
            Map.entry(PropertyNames.JAIL, new PropertyData("Jail", 10,  0, false,false)),
            Map.entry(PropertyNames.FREE_PARKING, new PropertyData("Free Parking", 20, 0, false,false)),
            Map.entry(PropertyNames.GO_TO_JAIL, new PropertyData("Go to Jail", 30,  0, false,false)),

            Map.entry(PropertyNames.CAR_COMPANY, new PropertyData("CAR_COMPANY", 12,  150, false,false)),
            Map.entry(PropertyNames.RAILROAD5, new PropertyData("Railroad", 28,  130, false,false))

            );

    public static PropertyData ofPos(int pos) {
        return ALL.values().stream()
                .filter(p -> p.boardPosition() == pos)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid board position: " + pos));
    }

}
