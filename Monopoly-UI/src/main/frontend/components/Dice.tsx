import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {World} from "./World";
import * as THREE from "three";
import {Object3D} from "three";
import {io} from 'socket.io-client';

interface DiceState {
    pos: { x: number, y: number, z: number };
    rot: { x: number, y: number, z: number, w: number };
}
export class Dice {
    socket = io('http://localhost:3001');

    model!: Object3D;
    constructor() {
        this.socket.off("diceStateUpdated");
        this.socket.on("diceStateUpdated", (diceState) => {
            this.model.position.set(diceState.pos.x, diceState.pos.y, diceState.pos.z);
            this.model.setRotationFromQuaternion(diceState.rot);
        });
    }

    getDiceTopFace(): number {
        const faceNormals = [
            new THREE.Vector3(0, 0, -1),  // 1
            new THREE.Vector3(1, 0, 0),    // 2
            new THREE.Vector3(0, 1, 0),    // 3
            new THREE.Vector3(0, -1, 0),   // 4
            new THREE.Vector3(-1, 0, 0),   // 5
            new THREE.Vector3(0, 0, 1)     // 6
        ];

        const diceRot = new THREE.Quaternion();
        this.model.getWorldQuaternion(diceRot);

        let maxDot = -Infinity;
        let topFace = 1;
        const upVector = new THREE.Vector3(0, 1, 0);

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

    async loadDice(world: World) {
        const boardPath = "/assets/models/dice3.glb";
        const loader = new GLTFLoader();
        return new Promise<void>((resolve, reject) => {
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

                    resolve();
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

    throwDice(gameId: string) {
        this.socket.emit("throw", gameId, (ack?: string) => {
            console.log("Throw acknowledged from server", ack);
        });
    }

    getCurrentDicePos(gameId:string){
        this.socket.emit("getCurrentDicePos", gameId, (diceState?: any) => {
            console.log("Throw acknowledged from server", diceState);
            this.model.position.set(diceState.pos.x, diceState.pos.y, diceState.pos.z);
            this.model.setRotationFromQuaternion(diceState.rot);
        });
    }

}