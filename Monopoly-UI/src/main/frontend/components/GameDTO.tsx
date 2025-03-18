import {PlayerDTO} from "Frontend/components/PlayerDTO";
import {GameState} from "Frontend/utils/constants";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";

export class GameDTO {
    gameId: string;
    gameState: GameState;
    players: PlayerDTO[];
    currentPlayerIndex: number;

    constructor({gameId, gameState, players, currentPlayerIndex}: GameDTO) {
        this.gameId = gameId;
        this.gameState = gameState;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
    }

    async loadBoardModel(loader: GLTFLoader, scene: any): Promise<void> {
        const boardPath = `/assets/models/monopolyBoard.glb`;
        return new Promise((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf) => {
                    const model = gltf.scene;
                    model.position.set(0, 0, 0);
                    model.userData = {isBoard: true};
                    model.traverse((obj) => {
                        if (obj.castShadow !== undefined) {
                            obj.castShadow = true;
                            obj.receiveShadow = true;
                        }
                    });
                    scene.add(model);
                    resolve();
                    console.log('Board model loaded');
                },
                undefined,
                (error) => {
                    console.error(`Error loading model: ${error}`);
                    reject(error);
                }
            );
        });
    }

    toJSON(): object {
        const {...rest} = this;
        return rest;
    }
}