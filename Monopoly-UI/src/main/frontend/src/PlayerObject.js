import gsap from "gsap";
import {randInt} from "three/src/math/MathUtils.js";
import {PlayerColor, positions} from "./globalVars.js";

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
        return new Promise((resolve, reject) => {
            loader.load(
                `/models/my_monopoly/${this.color}_pawn.glb`,
                (gltf) => {
                    this.model = gltf.scene;
                    console.log(`Loaded model for ${this.color} player`);
                    const {xOffset, zOffset} = this.helperSwitch(this.color);

                    if (this.position === null) {
                        this.position = 0;
                    }
                    const coords = positions[this.position];
                    this.model.position.set(coords.x + xOffset, 0.1, coords.z + zOffset);
                    this.model.scale.set(0.75, 0.75, 0.75);
                    scene.add(this.model);
                    resolve();
                },
                undefined,
                (error) => reject(`Error loading model for ${this.color}: ${error}`)
            );
        });
    }


    movePlayer(button) {
        if (this.isAnimating) return;

        this.isAnimating = true;
        button.disabled = true;
        button.style.backgroundColor = "red";

        this.randomInt = randInt(1, 5);
        console.log(`Player ${this.color} rolled ${this.randomInt}`);

        this.animatePlayerMovement(() => {
            this.isAnimating = false;
            button.disabled = false;
            button.style.backgroundColor = "";
        });
    }


    animatePlayerMovement(callback = () => {
    }) {
        if (!this.model) {
            console.error("Model not loaded yet!");
            return;
        }
        if (!positions) {
            console.error("Game positions are undefined.");
            return;
        }

        let step = 0;
        const duration = 0.66;

        const moveNext = () => {
            if (step < this.randomInt) {
                const nextPosIndex = (this.position + step + 1) % 40;
                const nextPosition = positions[nextPosIndex];

                if (!nextPosition) {
                    console.error("Invalid position data.");
                    return;
                }

                const {xOffset, zOffset} = this.helperSwitch(this.color);

                gsap.to(this.model.position, {
                    x: nextPosition.x + xOffset,
                    z: nextPosition.z + zOffset,
                    y: 0.7,
                    duration: duration / 2,
                    ease: "power1.inOut",
                    onComplete: () => {
                        gsap.to(this.model.position, {
                            y: 0.2,
                            duration: duration / 2,
                            ease: "power1.inOut",
                            onComplete: () => {
                                step++;
                                moveNext();
                            }
                        });
                    }
                });
            } else {
                this.position = (this.position + this.randomInt) % 40;
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
