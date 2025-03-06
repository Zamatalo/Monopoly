import {PlayerColor, positions} from "Frontend/src/globalVars.js";
import gsap from "gsap";

export class PlayerObject {
    constructor(playerId, color, balance, position = 0, inJail, ownedProperties, model) {
        this.playerId = playerId;
        this.color = color;
        this.balance = balance;
        this.position = position;
        this.inJail = inJail;
        this.ownedProperties = ownedProperties;
        this.model = model;
        this.isAnimating = false;
    }

    loadPlayerModel(loader, scene) {
        if (!this.model) {
            loader.load(
                `/models/my_monopoly/${this.color}_pawn.glb`,
                (gltf) => {
                    this.model = gltf.scene;
                    console.log(`Loaded model for ${this.color} player`);
                    const {xOffset, zOffset} = this.helperSwitch(this.color);

                    const coords = positions[this.position];
                    this.model.position.set(coords.x + xOffset, 0.1, coords.z + zOffset);
                    this.model.scale.set(0.75, 0.75, 0.75);
                    this.model.userData = {
                        isPlayer: true,
                        playerId: this.playerId,
                        color: this.color,
                    };
                    scene.add(this.model);
                },
                undefined,
                (error) => console.error(`Error loading model for ${this.color}: ${error}`)
            );
        }
    }

    animatePlayerMovement(targetPosition, callback = () => {
    }) {
        if (!this.model) {
            console.error("Model not loaded yet!");
            return;
        }
        if (!positions || targetPosition == null || targetPosition < 0 || targetPosition >= positions.length) {
            console.error("Game positions are undefined or invalid.");
            return;
        }
        const {xOffset, zOffset} = this.helperSwitch(this.color);
        let currentStep = this.position;

        const moveNext = () => {
            if (currentStep !== targetPosition) {
                currentStep = (currentStep + 1) % positions.length;
                const nextPosition = positions[currentStep];

                if (!nextPosition) {
                    console.error("Invalid position data.");
                    return;
                }

                gsap.to(this.model.position, {
                    x: nextPosition.x + xOffset,
                    z: nextPosition.z + zOffset,
                    y: 0.7,
                    duration: 0.33,
                    ease: "power1.inOut", 
                    onComplete: () => {
                        gsap.to(this.model.position, {
                            y: 0.2,
                            duration: 0.33,
                            ease: "power1.inOut", 
                            onComplete: () => {
                                moveNext();
                            }
                        });
                    }
                });
            } else {
                this.position = targetPosition;
                if (typeof callback === "function") {
                    callback(); 
                }
            }
        };
        moveNext();
    }

    helperSwitch(name) {
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

    toJSON() {
        const {model, isAnimating, ...rest} = this;
        return rest;
    }
}