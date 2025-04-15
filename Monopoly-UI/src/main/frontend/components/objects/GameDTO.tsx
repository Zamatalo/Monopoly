import {PlayerDTO} from "./PlayerDTO";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {World} from "../World";
import {GameState} from "Frontend/utils/constants";

export class GameDTO {
    gameId: string;
    gameState: GameState;
    players: PlayerDTO[];
    currentPlayerIndex: number;
    createdTime: Date;

    constructor({gameId, gameState, players, currentPlayerIndex,createdTime}: GameDTO) {
        this.gameId = gameId;
        this.gameState = gameState;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.createdTime = createdTime;
    }

    async loadBoardModel(world: World): Promise<void> {
        const boardPath = '/assets/models/monopolyBoard.glb';
        const loader = new GLTFLoader();

        return new Promise((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf) => {
                    const model = gltf.scene;
                    model.position.set(0, 0, 0);
                    model.userData = {isBoard: true};
                    model.scale.set(1, 1, 1);
                    model.traverse((obj) => {
                        if (obj.castShadow !== undefined) {
                            obj.receiveShadow = true;
                        }
                    });
                    world.addToScene(model);

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
}