import { World } from "../models/World";

class WorldSingleton {
    private static instance: World | null = null;

    static getInstance(container?: HTMLElement): World {
        if (!this.instance && container) {
            this.instance = new World(container);
        }
        if (!this.instance) {
            throw new Error("World not initialized yet. Pass a container to initialize.");
        }
        return this.instance;
    }

    static reset(): void {
        this.instance = null;
    }
}

export default WorldSingleton;
