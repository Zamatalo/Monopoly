import {World} from "../../components/models/World";

class WorldSingleton {
    private static instance: World | null = null;

    static initialize(container?: HTMLElement): World {
        if (!this.instance && container) {
            this.instance = new World(container);
        }
        if (!this.instance) {
            throw new Error("World not initialized yet. Pass a container to initialize.");
        }
        if (container && this.instance.renderer.domElement.parentElement !== container) {
            this.instance.reattachCanvas(container);
        }
        return this.instance;
    }

    static getInstance(): World {
        if (!this.instance) {
            throw new Error("Game not initialized. Call initialize() first.");
        }
        return this.instance;
    }

    static reset(): void {
        if (this.instance) {
            this.instance.dispose();
        }
        this.instance = null;
    }

    static hasInstance(): boolean {
        return this.instance !== null;
    }
}

export default WorldSingleton;