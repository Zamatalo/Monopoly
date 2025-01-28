import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js';

export class GtlfLoader {
    constructor(players, mainInstance) {
        this.players = players;
        this.loader = new GLTFLoader();
        this.main = mainInstance;
    }

    init() {
        if (!this.players || this.players.length === 0) {
            console.error('Players are not defined or empty.');
            return;
        }
        console.log('Players:', this.players);

        this.players.forEach((player, index) => {
            if (!player.name) {
                console.error(`Player at index ${index} is missing a "name" property.`);
                return;
            }

            const modelPath = `/models/my_monopoly/${player.name}_pawn.glb`;
            this.loader.load(
                modelPath,
                gltf => {
                    console.log(`Loaded model for ${player.name}`);
                    const model = gltf.scene;

                    let x = -9.5;
                    let y = 0.1;
                    let z = 9.5;
                    let offset = 0.5;
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
                        default:
                            console.warn(`No custom position adjustments for player: ${player.name}`);
                    }

                    model.position.set(x, y, z);
                    model.scale.set(0.75, 0.75, 0.75);

                    this.main.scene.add(model);
                    player.model = model;
                    player.pos = 0;
                },
                undefined,
                error => {
                    console.error(`Error loading model for player "${player.name}":`, error);
                }
            );
        });


        const boardPath = `/models/my_monopoly/monopolyBoard.glb`;
        this.loader.load(
            boardPath,
            gltf => {
                console.log('Board model loaded');
                const board = gltf.scene;
                board.position.set(0, 0, 0);
                this.main.scene.add(board);
            },
            undefined,
            error => {
                console.error('Error loading board model:', error);
            }
        );
    }
}
