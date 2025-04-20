import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {Object3D} from "three";
import WorldSingleton from "../../stores/singletons/WorldSingleton";


export class Dice {
    async loadDice(): Promise<void> {
        const boardPath = "/assets/models/dice3.glb";
        const loader = new GLTFLoader();
        const world = WorldSingleton.getInstance();

        return new Promise((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf: any) => {
                    const model = gltf.scene;
                    model.userData = {isDice: true};
                    model.position.y = 0.36;
                    model.traverse((obj: any) => {
                        if (obj.castShadow !== undefined) {
                            obj.castShadow = true;
                            obj.receiveShadow = true;
                        }
                    })
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