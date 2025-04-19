import { GameDTO } from "../models/GameDTO";

class GameSingleton {
    private static instance: GameDTO | null = null;

    static initialize(rawGameData: any): GameDTO {
        this.instance = GameDTO.fromRaw(rawGameData);
        localStorage.setItem("activeGame", JSON.stringify(rawGameData));
        return this.instance;
    }

    static getInstance(): GameDTO {
        if (!this.instance) {
            throw new Error("Game not initialized. Call initialize() first.");
        }
        return this.instance;
    }

    static tryRestore(): GameDTO | null {
        const raw = localStorage.getItem("activeGame");
        if (raw) {
            this.instance = GameDTO.fromRaw(JSON.parse(raw));
            return this.instance;
        }
        return null;
    }
    static reset(): void {
        this.instance = null;
    }

}

export default GameSingleton;
