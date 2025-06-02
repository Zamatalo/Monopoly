import {PlayerDTO} from "./PlayerDTO";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import WorldSingleton from "../../stores/singletons/WorldSingleton";
import {GameActions, GameState} from "../utils/constants";

export class GameDTO {
    gameId: string;
    gameState: GameState;
    players: PlayerDTO[];
    currentPlayerIndex: number;
    createdTime: Date;
    gameActions: GameActions[]

    constructor({gameId, gameState, players, currentPlayerIndex, createdTime, gameActions}: GameDTO) {
        this.gameId = gameId;
        this.gameState = gameState;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.createdTime = createdTime;
        this.gameActions = gameActions;
    }

    static fromRaw(raw: any): GameDTO {
        return new GameDTO({
            ...raw,
            players: raw.players ? raw.players.map((player: any) => PlayerDTO.fromRaw(player)) : [],
            createdTime: raw.createdTime,
        });
    }

    updateFromRaw(raw: any): void {
        this.gameState = raw.gameState;
        this.gameActions = raw.gameActions;
        this.currentPlayerIndex = raw.currentPlayerIndex;
        this.players = raw.players.map((player: any) => PlayerDTO.fromRaw(player));
    }

    async loadBoardModel(loader:GLTFLoader): Promise<void> {
        const boardPath = '/assets/models/monopolyBoard.glb';
        const world = WorldSingleton.getInstance();

        return new Promise((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf) => {
                    const model = gltf.scene;
                    model.position.set(0, 0, 0);
                    model.userData = {isBoard: true};
                    model.scale.set(1, 1, 1);
                    model.traverse((obj: any) => {
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
