package com.example.application.util.data;

import com.example.application.config.GameConfig;
import com.example.application.util.enums.SpecialTileEffect;

import java.util.List;

public record SpecialTileData(String text, SpecialTileEffect effect, Integer amount) {

    public static final List<SpecialTileData> CHEST_CARDS = List.of(
            //Chest
            new SpecialTileData("Chest_1", SpecialTileEffect.MOVE_TO_GO_AND_COLLECT, 200),
            new SpecialTileData("Chest_2", SpecialTileEffect.COLLECT, 200),
            new SpecialTileData("Chest_3", SpecialTileEffect.PAY, 50),
            new SpecialTileData("Chest_4", SpecialTileEffect.COLLECT, 50),
            // new SpecialTileData("Get Out of Jail Free. Keep until needed.",ChestEffect.GET_OUT_OF_JAIL_FREE),
            new SpecialTileData("Chest_5", SpecialTileEffect.GO_TO_JAIL, 0),
            new SpecialTileData("Chest_6", SpecialTileEffect.COLLECT_FROM_EACH_PLAYER, 50),
            new SpecialTileData("Chest_7", SpecialTileEffect.COLLECT, 100),
            new SpecialTileData("Chest_8", SpecialTileEffect.COLLECT, 20),
            new SpecialTileData("Chest_9", SpecialTileEffect.COLLECT_FROM_EACH_PLAYER, 10),
            new SpecialTileData("Chest_10", SpecialTileEffect.COLLECT, 100),
            new SpecialTileData("Chest_11", SpecialTileEffect.PAY, 50),
            new SpecialTileData("Chest_12", SpecialTileEffect.PAY, 50),
            new SpecialTileData("Chest_13", SpecialTileEffect.COLLECT, 25),
            new SpecialTileData("Chest_14", SpecialTileEffect.COLLECT, 10),
            new SpecialTileData("Chest_15", SpecialTileEffect.COLLECT, 100)

    );
    public static final List<SpecialTileData> CHANCE_CARDS = List.of(
            //Chance
            new SpecialTileData("Chance_1", SpecialTileEffect.MOVE_TO_GO_AND_COLLECT, 200),
            new SpecialTileData("Chance_2", SpecialTileEffect.MOVE_TO_TILE_AND_COLLECT_IF_PASS_GO, 200),
            //new SpecialTileData("Chance_3", SpecialTileEffect.MOVE_TO_TILE_AND_COLLECT_IF_PASS_GO, 200),
            // new SpecialTileData("Chance_4", SpecialTileEffect.MOVE_TO_NEAREST_UTILITY, 0),
            //  new SpecialTileData("Chance_5", SpecialTileEffect.MOVE_TO_NEAREST_RAILROAD, 0),
            new SpecialTileData("Chance_6", SpecialTileEffect.COLLECT, 50),
            new SpecialTileData("Chance_7", SpecialTileEffect.GET_OUT_OF_JAIL_FREE, 0),
            //new SpecialTileData("Chance_8", SpecialTileEffect.MOVE_BACKWARD, 3),
            new SpecialTileData("Chance_9", SpecialTileEffect.GO_TO_JAIL, 0),
            //new SpecialTileData("Chance_10", SpecialTileEffect.PAY_REPAIRS, 0),
            new SpecialTileData("Chance_11", SpecialTileEffect.MOVE_TO_TILE_AND_COLLECT_IF_PASS_GO, 200),
            new SpecialTileData("Chance_12", SpecialTileEffect.PAY, 15),
            new SpecialTileData("Chance_13", SpecialTileEffect.MOVE_TO_TILE_AND_COLLECT_IF_PASS_GO, 200),
            new SpecialTileData("Chance_14", SpecialTileEffect.PAY_TO_EACH_PLAYER, 50),
            new SpecialTileData("Chance_15", SpecialTileEffect.COLLECT, 150)
    );
    public static final SpecialTileData TAX =
            new SpecialTileData("Income_tax", SpecialTileEffect.PAY, GameConfig.INCOME_TAX);

    public static final SpecialTileData LUXURY_TAX =
            new SpecialTileData("Luxury_tax", SpecialTileEffect.PAY, 200);
}
