import {GameDTO} from "../components/models/GameDTO";

class GameSingleton {
    private static instance: GameDTO | null = null;

    static initialize(rawGameData: any): GameDTO {
        if (!this.instance) {
            this.instance = GameDTO.fromRaw(rawGameData);
        }
        return this.instance;
    }

    static getInstance(): GameDTO {
        if (!this.instance) {
            throw new Error("Game not initialized. Call initialize() first.");
        }
        return this.instance;
    }

    static update(rawGameData: any): GameDTO {
        if (!this.instance) {
            throw new Error("Game not initialized. Call initialize() first.");
        }
        this.instance.updateFromRaw(rawGameData);
        return this.instance;
    }

    static reset(): void {
        this.instance = null;
    }
}

export default GameSingleton;
