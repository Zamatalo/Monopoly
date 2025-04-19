import {PlayerDTO} from "./PlayerDTO";
import {GameState} from "../utils/constants";
import {Object3D} from 'three';
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import WorldSingleton from "../utils/WorldSingleton";

export class GameDTO {
    gameId: string;
    gameState: GameState;
    players: PlayerDTO[];
    currentPlayerIndex: number;
    createdTime: Date;
    model: Object3D | undefined;

    constructor(data: GameDTO) {
        this.gameId = data.gameId;
        this.gameState = data.gameState;
        this.players = data.players.map(PlayerDTO.fromRaw);
        this.currentPlayerIndex = data.currentPlayerIndex;
        this.createdTime = new Date(data.createdTime);
    }

    static fromRaw(raw: any): GameDTO {
        return new GameDTO({
            ...raw,
            players: raw.players || [],
            createdTime: raw.createdTime,
        });
    }

    async loadBoardModel(): Promise<void> {
        const boardPath = '/assets/models/monopolyBoard.glb';
        const loader = new GLTFLoader();
        const world = WorldSingleton.getInstance();

        return new Promise((resolve, reject) => {
            loader.load(
                boardPath,
                (gltf) => {
                    this.model = gltf.scene;
                    this.model.position.set(0, 0, 0);
                    this.model.userData = {isBoard: true};
                    this.model.scale.set(1, 1, 1);
                    this.model.traverse((obj) => {
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


