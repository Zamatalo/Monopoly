import { World } from "../components/models/World";

class WorldSingleton {
    private static instance: World | null = null;

    static getInstance(container?: HTMLElement): World {
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