import {PlayerDTO} from "../../components/models/PlayerDTO";

class CurrentPlayerInstance {
    private static instance: PlayerDTO | null = null;

    static initialize(rawPlayerData: any): PlayerDTO | null {
        if (!this.instance) {
            this.instance = PlayerDTO.fromRaw(rawPlayerData);
        }
        return this.instance;
    }

    static getInstance(): PlayerDTO | null {
        if (!this.instance) {
            throw new Error("Player not initialized. Call initialize() first.");
        }
        return this.instance;
    }

    static reset(): void {
        this.instance = null;
    }
    static update(rawPlayerData: any): PlayerDTO {
        if (!this.instance) {
            throw new Error("Player not initialized. Call initialize() first.");
        }
        this.instance.updateFromRaw(rawPlayerData);
        return this.instance;
    }
}

export default CurrentPlayerInstance;
