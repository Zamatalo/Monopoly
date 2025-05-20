import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {PlayerColor, positions} from "../utils/constants";
import gsap from "gsap";
import {PropertyDTO} from "./PropertyDTO";
import {Object3D} from "three";
import WorldSingleton from "../../stores/singletons/WorldSingleton";

export class PlayerDTO {
    playerId: string;
    playerName: string;
    color: PlayerColor;
    inJail: boolean;
    balance: number;
    position: number;
    ownedProperties: PropertyDTO[];

    constructor({playerId, color, inJail, balance, position, ownedProperties, playerName}: PlayerDTO) {
        this.playerId = playerId;
        this.color = color;
        this.playerName = playerName;
        this.inJail = inJail;
        this.balance = balance;
        this.position = position;
        this.ownedProperties = ownedProperties;
    }

    static fromRaw(raw: any): PlayerDTO {
        if (!raw) throw new Error("Cannot construct PlayerDTO from undefined!");
        return new PlayerDTO({
            ...raw,
            ownedProperties: raw.ownedProperties ? raw.ownedProperties.map((prop: any) => PropertyDTO.fromRaw(prop)) : []
        });
    }

    updateFromRaw(raw: any): void {
        this.inJail = raw.inJail;
        this.balance = raw.balance;
        this.position = raw.position;
        this.ownedProperties = raw.ownedProperties || [];
    }

    async loadPlayerModel(loader:GLTFLoader): Promise<void> {
        const playerPath = `/assets/models/${this.color}_pawn.glb`;
        const world = WorldSingleton.getInstance();


        return new Promise((resolve, reject) => {
            loader.load(
                playerPath,
                (gltf) => {
                    const model = gltf.scene;
                    const {xOffset, zOffset} = this.helperSwitch(this.color);
                    const pos = positions[this.position];
                    model.position.set(pos.x + xOffset, 0.14, pos.z + zOffset);
                    model.scale.set(2.5, 2.5, 2.5);

                    model.userData = {
                        isPlayer: true,
                        playerId: this.playerId,
                        color: this.color,
                    };

                    model.traverse((obj) => {
                        if (obj.castShadow !== undefined) {
                            obj.castShadow = true;
                            obj.receiveShadow = true;
                        }
                    });
                    world.addToScene(model);
                    console.log(`Loaded model for ${this.color} player`);
                    resolve();
                },
                undefined,
                (error) => {
                    console.error(`Error loading model for ${this.color}: ${error}`);
                    reject(error);
                }
            );
        });
    }

    animatePlayerMovement(targetPosition: number, model: Object3D, callback: () => void = () => {}): void {
        if (!model) {
            console.error('Model not loaded yet!');
            return;
        }

        if (targetPosition < 0 || targetPosition >= positions.length) {
            console.error('Invalid target position.');
            return;
        }

        const {xOffset, zOffset} = this.helperSwitch(this.color);
        let currentStep = this.position;

        const moveNext = () => {
            if (currentStep !== targetPosition) {
                currentStep = (currentStep + 1) % positions.length;
                const nextPosition = positions[currentStep];
                if (!nextPosition) {
                    console.error('Invalid position data.');
                    return;
                }

                gsap.to(model.position, {
                    x: nextPosition.x + xOffset,
                    z: nextPosition.z + zOffset,
                    y: 0.7,
                    duration: 0.33,
                    ease: 'power1.inOut',
                    onComplete: () => {
                        gsap.to(model.position, {
                            y: 0.13,
                            duration: 0.33,
                            ease: 'power1.inOut',
                            onComplete: moveNext,
                        });
                    },
                });
            } else {
                this.position = targetPosition;
                callback();
            }
        };
        moveNext();
    }

    private helperSwitch(name: string): { xOffset: number; zOffset: number } {
        const offset = 0.4;
        switch (name) {
            case PlayerColor.PLAYER_RED:
                return {xOffset: offset, zOffset: offset};
            case PlayerColor.PLAYER_GREEN:
                return {xOffset: -offset, zOffset: offset};
            case PlayerColor.PLAYER_BLUE:
                return {xOffset: offset, zOffset: -offset};
            case PlayerColor.PLAYER_YELLOW:
                return {xOffset: -offset, zOffset: -offset};
            default:
                return {xOffset: 0, zOffset: 0};
        }
    }
}