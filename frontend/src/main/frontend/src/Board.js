import * as THREE from "three";

export class Board {
    constructor(scene, players) {
        this.scene = scene;
        this.players = players;
        this.positions = [];
    }

    initializePositions() {
        this.positions = [
            {x: -9.5, z: 9.5},
            {x: -7, z: 9.5},
            {x: -5.25, z: 9.5},
            {x: -3.65, z: 9.5},
            {x: -1.85, z: 9.5},
            {x: 0.0, z: 9.5},
            {x: 1.85, z: 9.5},
            {x: 3.65, z: 9.5},
            {x: 5.25, z: 9.5},
            {x: 7, z: 9.5},
            {x: 9.5, z: 9.5},
            {x: 9.5, z: 7},
            {x: 9.5, z: 5.25},
            {x: 9.5, z: 3.65},
            {x: 9.5, z: 1.85},
            {x: 9.5, z: 0.0},
            {x: 9.5, z: -1.85},
            {x: 9.5, z: -3.65},
            {x: 9.5, z: -5.25},
            {x: 9.5, z: -7},
            {x: 9.5, z: -9.5},
            {x: 7, z: -9.5},
            {x: 5.25, z: -9.5},
            {x: 3.65, z: -9.5},
            {x: 1.85, z: -9.5},
            {x: 0.0, z: -9.5},
            {x: -1.85, z: -9.5},
            {x: -3.65, z: -9.5},
            {x: -5.25, z: -9.5},
            {x: -7, z: -9.5},
            {x: -9.5, z: -9.5},
            {x: -9.5, z: -7},
            {x: -9.5, z: -5.25},
            {x: -9.5, z: -3.65},
            {x: -9.5, z: -1.85},
            {x: -9.5, z: 0.0},
            {x: -9.5, z: 1.85},
            {x: -9.5, z: 3.65},
            {x: -9.5, z: 5.25},
            {x: -9.5, z: 7},
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

    movePlayer(playerId, cellIndex) {
        const player = this.players.find(p => p.id === playerId);
        if (!player) {
            console.error('Player not found');
            return;
        }
        const currentPosition = player.model.position;
        console.log(`Player ${player.name} current position: x:${currentPosition.x} z:${currentPosition.z}`);
        const targetPosition = this.positions[player.pos + cellIndex];

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
            console.log(`Player ${player.name} moved to x:${x} z:${z}`);
            player.model.position.set(x, y, z);
            player.pos = player.pos + cellIndex;
        }
    }
}
