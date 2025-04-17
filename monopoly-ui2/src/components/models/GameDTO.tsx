import {PlayerDTO} from "./PlayerDTO";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {GameState} from "../utils/constants";
import {useGameStore} from "../../stores/gameStore";

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

    async loadBoardModel() {
        const boardPath = '/assets/models/monopolyBoard.glb';
        const loader = new GLTFLoader();
        const world = useGameStore.getState().world;
        if (!world) return;
        return new Promise<void>((resolve, reject) => {
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
                    console.log('Board model loaded');
                    resolve();
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
