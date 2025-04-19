import WorldSingleton from "../components/utils/WorldSingleton";
import GameSingleton from "../components/utils/GameSingleton";
import {GameDTO} from "../components/models/GameDTO";
import {Object3D} from "three";
import {World} from "../components/models/World";


export function updateGame(newGame: GameDTO) {
    const game = GameSingleton.getInstance();
    const world: World = WorldSingleton.getInstance();
    if(!game&&!world){return;}
    console.log(world.scene);
    newGame.players.forEach((newPlayerData) => {
        const oldPlayer = game.players.find(p => p.color === newPlayerData.color);
        if (oldPlayer) {
            const newPosition = newPlayerData.position;
            if (oldPlayer.position !== newPosition) {
                const playerModel = world.scene.children.find(e => e.userData.color == newPlayerData.color) as Object3D;
                oldPlayer.animatePlayerMovement(newPosition, playerModel);
            }
        }
    });
}

export function resetGameEnvironment() {
    WorldSingleton.reset();
    GameSingleton.reset();
}
