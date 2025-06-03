import GameSingleton from "./singletons/GameSingleton";
import {GameDTO} from "../components/models/GameDTO";
import WorldSingleton from "./singletons/WorldSingleton";
import {Object3D} from "three";
import CurrentPlayerSingleton from "./singletons/CurrentPlayerSingleton";
import {Dice} from "../components/models/Dice";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";

const loader = new GLTFLoader();

export function initGame() {
    const game = GameSingleton.getInstance();

    game.loadBoardModel(loader);

    game.players.forEach(player => {
        player.loadPlayerModel(loader)
    });

    game.players.forEach(player => {
        player.ownedProperties.forEach(ele => {
            if (!getExistingBuildingIds().has(ele.displayName)) {
                ele.loadBuildingModel(loader,player.color);
            }
        });
    });

    const dice = new Dice();
    dice.loadDice(loader);
}

export function updateGame(newGameRaw: GameDTO) {
    const oldGame = GameSingleton.getInstance();
    const world = WorldSingleton.getInstance();

    if (!oldGame || !world) return;
    const newGame = GameDTO.fromRaw(newGameRaw);
    const oldPlayersColors = new Set(oldGame.players.map(p => p.color));

    newGame.players.forEach((newPlayer) => {
        if (!oldPlayersColors.has(newPlayer.color)) {
            newPlayer.loadPlayerModel(loader);
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

    newGame.players.forEach(player => {
        player.ownedProperties.forEach(ele => {
            if (!getExistingBuildingIds().has(ele.displayName)) {
                ele.loadBuildingModel(loader,player.color);
            }
        });
    });

    console.log(world.scene.children);
    GameSingleton.update(newGame);
    const currentPlayer = newGame.players[newGame.currentPlayerIndex];
    CurrentPlayerSingleton.update(currentPlayer);
}

export const diceUpdate = (() => {
    const queue: any[] = [];
    let intervalId: any = null;
    let world: any = null;

    function applyUpdate(update: any) {
        if (!world) {
            try {
                world = WorldSingleton.getInstance();
            } catch {
                return;
            }
        }

        world.scene.children.forEach((child: any) => {
            if (child.userData.isDice) {
                child.position.set(update.pos.x, update.pos.y, update.pos.z);
                child.quaternion.set(update.rot.x, update.rot.y, update.rot.z, update.rot.w);
            }
        });
    }

    function startInterval() {
        if (intervalId !== null) return;

        intervalId = setInterval(() => {
            if (queue.length === 0) return;

            const update = queue.shift();
            applyUpdate(update);
        }, 16);
    }

    return function enqueue(update: any) {
        queue.push(update);
        startInterval();
    };
})();





export function resetGameEnvironment() {
    const world = WorldSingleton.hasInstance() ? WorldSingleton.getInstance() : null;
    if (world) {
        world.dispose()
        const container = world.renderer.domElement.parentElement;
        if (container) {
            container.removeChild(world.renderer.domElement);
        }
    }
    WorldSingleton.reset();
    GameSingleton.reset();
    CurrentPlayerSingleton.reset();
}

function getExistingBuildingIds() {
    const world = WorldSingleton.getInstance();
    return new Set(
        world.scene.children
            .filter(e => e.userData.isBuilding)
            .map(b => b.userData.buildingName)
    )
}

