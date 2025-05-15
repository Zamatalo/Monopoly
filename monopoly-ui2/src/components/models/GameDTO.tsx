import {PlayerDTO} from "./PlayerDTO";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import WorldSingleton from "../../stores/singletons/WorldSingleton";
import {GameState, PlayerColor} from "../utils/constants";
import {Object3D} from "three";

export class GameDTO {
    gameId: string;
    gameState: GameState;
    players: PlayerDTO[];
    currentPlayerIndex: number;
    createdTime: Date;
    model: Object3D | undefined;

    constructor({gameId, gameState, players, currentPlayerIndex,createdTime}: GameDTO) {
        this.gameId = gameId;
        this.gameState = gameState;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.createdTime = createdTime;
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
        this.currentPlayerIndex = raw.currentPlayerIndex;
        this.players = raw.players.map((player: any) => PlayerDTO.fromRaw(player));
    }

    async loadBoardModel(loader:GLTFLoader): Promise<void> {
        if (this.model) {
            console.log('Board model already loaded');
            return;
        }

        const boardPath = '/assets/models/monopolyBoard.glb';
        const world = WorldSingleton.getInstance();

        return new Promise((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf) => {
                    this.model = gltf.scene;
                    this.model.position.set(0, 0, 0);
                    this.model.userData = { isBoard: true };
                    this.model.scale.set(1, 1, 1);
                    this.model.traverse((obj:any) => {
                        if (obj.castShadow !== undefined) {
                            obj.receiveShadow = true;
                        }
                    });

                    world.addToScene(this.model);
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
