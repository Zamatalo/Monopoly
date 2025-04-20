import GameSingleton from "./GameSingleton";
import {GameDTO} from "../components/models/GameDTO";
import WorldSingleton from "./WorldSingleton";
import {Object3D} from "three";
import CurrentPlayerSingleton from "./CurrentPlayerSingleton";

export function updateGame(newGameRaw: GameDTO) {
    const oldGame = GameSingleton.getInstance();
    const world = WorldSingleton.getInstance();
    if (!oldGame || !world) return;
    const newGame = GameDTO.fromRaw(newGameRaw);
    const oldPlayersColors = new Set(oldGame.players.map(p => p.color));

    newGame.players.forEach((newPlayer) => {
        if (!oldPlayersColors.has(newPlayer.color)) {
            newPlayer.loadPlayerModel();
        }
    });

    newGame.players.forEach((newPlayer) => {
        const oldPlayer = oldGame.players.find(p => p.color === newPlayer.color);
        if (oldPlayer && oldPlayer.position !== newPlayer.position) {
            const model = world.scene.children.find(e => e.userData.color === newPlayer.color) as Object3D;
            if (model) {
                oldPlayer.animatePlayerMovement(newPlayer.position, model);
            }
        }
    });

    GameSingleton.update(newGame);
    const currentPlayer = newGame.players[newGame.currentPlayerIndex];
    CurrentPlayerSingleton.update(currentPlayer);
}

export function resetGameEnvironment() {
    const world = WorldSingleton.hasInstance() ? WorldSingleton.getInstance() : null;
    if (world) {
        const container = world.renderer.domElement.parentElement;
        if (container) {
            container.removeChild(world.renderer.domElement);
        }
    }
    WorldSingleton.reset();
    GameSingleton.reset();
    CurrentPlayerSingleton.reset();
}
