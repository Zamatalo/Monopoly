import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {World} from "./World";
import * as THREE from "three";
import {Object3D, Quaternion, Vector3} from "three";
import * as RAPIER from "@dimforge/rapier3d";
import { io } from 'socket.io-client';

const socket = io('http://localhost:3001');

export class Dice {
    model!: Object3D;
    body!:RAPIER.RigidBody;
    public topFace: number = 0;

    getDiceTopFace(): number {
        const faceNormals = [
            new THREE.Vector3(0, 0, -1),
            new THREE.Vector3(1, 0, 0),
            new THREE.Vector3(0, 1, 0),
            new THREE.Vector3(0, -1, 0),
            new THREE.Vector3(-1, 0, 0),
            new THREE.Vector3(0, 0, 1)
        ];
        const diceQuaternion = new Quaternion(this.body.rotation().x, this.body.rotation().y, this.body.rotation().z, this.body.rotation().w);
        const worldUp = new Vector3(0, 1, 0);

        let maxDot = -Infinity;
        for (let i = 0; i < faceNormals.length; i++) {
            const normal = faceNormals[i].clone();
            normal.applyQuaternion(diceQuaternion);
            const dot = normal.dot(worldUp);
            if (dot > maxDot) {
                maxDot = dot;
                this.topFace = i + 1;
            }
        }
        return this.topFace;
    }

    async loadDice(world: World) {
        const boardPath = "/assets/models/dice3.glb";
        const loader = new GLTFLoader();
        return new Promise<void>((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf: any) => {
                    let model = gltf.scene;
                    model.userData = {isDice: true};
                    model.traverse((obj: any) => {
                        if (obj.castShadow !== undefined) {
                            obj.castShadow = true;
                            obj.receiveShadow = true;
                        }
                    });
                    world.scene.add(model);

                    const rigidBodyDesc = RAPIER.RigidBodyDesc.dynamic()
                        .setTranslation(0, 0.2, 0)
                        .setUserData("isDice");
                    const rigidBody = world.world.createRigidBody(rigidBodyDesc);
                    const colliderDesc = RAPIER.ColliderDesc.cuboid(0.16, 0.16, 0.16)
                        .setTranslation(0, 0, 0)
                        .setFriction(10)
                        .setRestitution(0.4);
                    world.world.createCollider(colliderDesc, rigidBody);
                    world.addBody(model, rigidBody);
                    this.body=rigidBody;
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

    throwDice() {
        socket.emit("throw", "", (ack?: string) => {
            console.log("Throw acknowledged from server", ack);
        });

        socket.off("diceStateUpdated");
        socket.on("diceStateUpdated", (diceState) => {
            //console.log("Dice state received from server:", diceState);
            this.body.setTranslation(diceState.pos,true);
            this.body.setRotation(diceState.rot,true);
        });
    }

    getCurrentDicePos(){
        socket.emit("getCurrentDicePos", "", (ack:any) => {
            console.log("Throw acknowledged from server", ack);
            this.body.setTranslation(ack.pos,true);
            this.body.setRotation(ack.rot,true);
        });

    }

}