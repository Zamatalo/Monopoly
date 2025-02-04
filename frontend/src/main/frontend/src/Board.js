import * as THREE from "three";
import {randInt} from "three/src/math/MathUtils.js";
import gsap from "gsap";

export class Board {
    constructor(scene, players) {
        this.scene = scene;
        this.players = players;
        this.positions = [];
        this.isAnimating = false;
    }

    initializePositions() {
        this.positions = [
            {x: 9.5, z: 9.5}, {x: 7, z: 9.5}, {x: 5.25, z: 9.5}, {x: 3.65, z: 9.5},
            {x: 1.85, z: 9.5}, {x: 0.0, z: 9.5}, {x: -1.85, z: 9.5}, {x: -3.65, z: 9.5},
            {x: -5.25, z: 9.5}, {x: -7, z: 9.5}, {x: -9.5, z: 9.5}, {x: -9.5, z: 7},
            {x: -9.5, z: 5.25}, {x: -9.5, z: 3.65}, {x: -9.5, z: 1.85}, {x: -9.5, z: 0.0},
            {x: -9.5, z: -1.85}, {x: -9.5, z: -3.65}, {x: -9.5, z: -5.25}, {x: -9.5, z: -7},
            {x: -9.5, z: -9.5}, {x: -7, z: -9.5}, {x: -5.25, z: -9.5}, {x: -3.65, z: -9.5},
            {x: -1.85, z: -9.5}, {x: 0.0, z: -9.5}, {x: 1.85, z: -9.5}, {x: 3.65, z: -9.5},
            {x: 5.25, z: -9.5}, {x: 7, z: -9.5}, {x: 9.5, z: -9.5}, {x: 9.5, z: -7},
            {x: 9.5, z: -5.25}, {x: 9.5, z: -3.65}, {x: 9.5, z: -1.85}, {x: 9.5, z: 0.0},
            {x: 9.5, z: 1.85}, {x: 9.5, z: 3.65}, {x: 9.5, z: 5.25}, {x: 9.5, z: 7}
        ];
    }


    addHelpers() {
        const geometry = new THREE.SphereGeometry(0.1, 16, 16);
        const material = new THREE.MeshBasicMaterial({color: 0x00ff00});

        this.positions.forEach((pos, index) => {
            const helper = new THREE.Mesh(geometry, material);
            helper.position.set(pos.x, 0.1, pos.z);
            this.scene.add(helper);
        });
    }

    movePlayer(playerId, button) {
        if (this.isAnimating) {
            return;
        }

        this.isAnimating = true;
        button.disabled = true;
        button.style.backgroundColor = "red";
        const player = this.players.find(p => p.id === playerId);
        if (!player) {
            console.error('Player not found');
            return;
        }

        const randomInt = randInt(1, 5);
        const currentPosition = player.model.position;
        console.log(`Player ${player.name} current position: x:${currentPosition.x} z:${currentPosition.z}`);
        console.log(`Rolled ${randomInt}`);
        const targetPosition = this.positions[(player.pos + randomInt) % 40];

        if (targetPosition) {
            let offset = 0.5;
            let x = 0;
            let y = 0.1;
            let z = 0;

            switch (player.name) {
                case 'red':
                    x = targetPosition.x + offset;
                    z = targetPosition.z + offset;
                    break;
                case 'green':
                    x = targetPosition.x - offset;
                    z = targetPosition.z + offset;
                    break;
                case 'blue':
                    x = targetPosition.x + offset;
                    z = targetPosition.z - offset;
                    break;
                case 'yellow':
                    x = targetPosition.x - offset;
                    z = targetPosition.z - offset;
                    break;
            }

            this.animatePlayerMovement(player, player.pos, randomInt, () => {
                player.pos = (player.pos + randomInt) % 40;
                this.isAnimating = false;
                button.disabled = false;
                button.style.backgroundColor = "";
            });
        }
    }

    animatePlayerMovement(player, startIndex, steps, callback) {
        let step = 0;
        const duration = 0.66;

        const moveNext = () => {
            if (step < steps) {
                const nextPosition = this.positions[(startIndex + step + 1) % 40];

                const offset = 0.5;
                let x = nextPosition.x;
                let z = nextPosition.z;

                switch (player.name) {
                    case 'red':
                        x += offset;
                        z += offset;
                        break;
                    case 'green':
                        x -= offset;
                        z += offset;
                        break;
                    case 'blue':
                        x += offset;
                        z -= offset;
                        break;
                    case 'yellow':
                        x -= offset;
                        z -= offset;
                        break;
                }

                gsap.to(player.model.position, {
                    x: x,
                    z: z,
                    y: 0.7,
                    duration: duration / 2,
                    ease: "power1.inOut",
                    onComplete: () => {
                        gsap.to(player.model.position, {
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
                callback();
            }
        };

        moveNext();
    }
}


