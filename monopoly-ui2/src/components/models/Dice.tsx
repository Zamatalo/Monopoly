import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {World} from "./World";
import {Object3D, Quaternion, Vector3} from "three";

export class Dice {
    model!: Object3D;

    getDiceTopFace(): number {
        const faceNormals = [
            new Vector3(0, 0, -1),  // 1
            new Vector3(1, 0, 0),    // 2
            new Vector3(0, 1, 0),    // 3
            new Vector3(0, -1, 0),   // 4
            new Vector3(-1, 0, 0),   // 5
            new Vector3(0, 0, 1)     // 6
        ];

        const diceRot = new Quaternion();
        this.model.getWorldQuaternion(diceRot);

        let maxDot = -Infinity;
        let topFace = 1;
        const upVector = new Vector3(0, 1, 0);

        for (let i = 0; i < faceNormals.length; i++) {
            const normal = faceNormals[i].clone();
            normal.applyQuaternion(diceRot).normalize();

            const dot = normal.dot(upVector);

            if (dot > maxDot) {
                maxDot = dot;
                topFace = i + 1;
            }
        }
        return topFace;
    }

    loadDice(world: World) {
        const boardPath = "/assets/models/dice3.glb";
        const loader = new GLTFLoader();

        return () => {
            loader.load(
                boardPath,
                (gltf: any) => {
                    this.model = gltf.scene;
                    this.model.userData = {isDice: true};
                    this.model.traverse((obj: any) => {
                        if (obj.castShadow !== undefined) {
                            obj.castShadow = true;
                            obj.receiveShadow = true;
                        }
                    })
                    world.scene.add(this.model);

                    console.log('Dice model loaded');
                },
                undefined,
                (error: any) => {
                    console.error(`Error loading model: ${error}`);
                }
            );
        };
    }
}