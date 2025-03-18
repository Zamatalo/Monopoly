import {Scene} from 'three';
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js';
import {PlayerColor, positions} from 'Frontend/utils/constants';
import {PropertyDTO} from 'Frontend/components/PropertyDTO';

export interface PlayerDTO {
    playerId: string;
    color: PlayerColor;
    inJail: boolean;
    balance: number;
    position: number;
    ownedProperties: PropertyDTO[];
}

export class PlayerDTO {
    playerId: string;
    color: PlayerColor;
    inJail: boolean;
    balance: number;
    position: number;
    ownedProperties: PropertyDTO[];

    constructor({playerId, color, inJail, balance, position, ownedProperties}: PlayerDTO) {
        this.playerId = playerId;
        this.color = color;
        this.inJail = inJail;
        this.balance = balance;
        this.position = position;
        this.ownedProperties = ownedProperties;
    }

    loadPlayerModel(loader: GLTFLoader, scene: Scene): Promise<void> {
        const playerPath = `/assets/models/${this.color}_pawn.glb`;
        console.log(this.color);
        return new Promise((resolve, reject) => {
            loader.load(
                playerPath,
                (gltf) => {
                    const model = gltf.scene;

                    const {xOffset, zOffset} = this.helperSwitch(this.color);
                    const coords = positions[this.position];
                    model.position.set(coords.x + xOffset, 0.1, coords.z + zOffset);
                    model.scale.set(0.75, 0.75, 0.75);
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

                    scene.add(model);
                    resolve();
                    console.log(`Loaded model for ${this.color} player`);
                },
                undefined,
                (error) => {
                    console.error(`Error loading model for ${this.color}: ${error}`);
                    reject(error);
                }
            );
        });
    }


    // animatePlayerMovement(targetPosition: number, callback: () => void = () => {}): void {
    //     if (!this.model) {
    //         console.error('Model not loaded yet!');
    //         return;
    //     }
    //
    //     if (targetPosition < 0 || targetPosition >= positions.length) {
    //         console.error('Invalid target position.');
    //         return;
    //     }
    //
    //     const { xOffset, zOffset } = this.helperSwitch(this.color);
    //     let currentStep = this.position;
    //
    //     const moveNext = () => {
    //         if (currentStep !== targetPosition) {
    //             currentStep = (currentStep + 1) % positions.length;
    //             const nextPosition = positions[currentStep];
    //
    //             if (!nextPosition) {
    //                 console.error('Invalid position data.');
    //                 return;
    //             }
    //
    //             gsap.to(this.model!.position, {
    //                 x: nextPosition.x + xOffset,
    //                 z: nextPosition.z + zOffset,
    //                 y: 0.7,
    //                 duration: 0.33,
    //                 ease: 'power1.inOut',
    //                 onComplete: () => {
    //                     gsap.to(this.model!.position, {
    //                         y: 0.2,
    //                         duration: 0.33,
    //                         ease: 'power1.inOut',
    //                         onComplete: moveNext,
    //                     });
    //                 },
    //             });
    //         } else {
    //             this.position = targetPosition;
    //             callback();
    //         }
    //     };
    //
    //     moveNext();
    // }

    toJSON(): object {
        const {...rest} = this;
        return rest;
    }

    private helperSwitch(name: string): { xOffset: number; zOffset: number } {
        const offset = 0.5;
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