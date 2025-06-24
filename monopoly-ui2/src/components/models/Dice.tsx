import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import WorldSingleton from "../../stores/singletons/WorldSingleton";
import {Mesh} from "three";

export class Dice {
    async loadDice(loader:GLTFLoader,
                   posX:number,
                   poxZ:number,
                   diceId:number): Promise<void> {
        const boardPath = "/assets/models/dice3.glb";
        const world = WorldSingleton.getInstance();

        return new Promise((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf: any) => {
                    const model = gltf.scene;
                    model.userData = {dice: diceId};
                    model.position.y = 0.36;
                    model.position.z = poxZ;
                    model.position.x= posX;

                    model.traverse((obj: any) => {
                        if (obj instanceof Mesh) {
                            obj.castShadow = true;
                        }
                    });
                    world.scene.add(model);
                    resolve()
                    console.log('Dice model loaded');
                },
                undefined,
                (error: any) => {
                    console.error(`Error loading model: ${error}`);
                    reject(error);
                }
            );
        });
    }
}