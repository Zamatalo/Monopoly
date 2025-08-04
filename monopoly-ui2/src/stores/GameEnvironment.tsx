import GameSingleton from "./singletons/GameSingleton";
import {GameDTO} from "../components/models/GameDTO";
import WorldSingleton from "./singletons/WorldSingleton";
import {Object3D} from "three";
import CurrentPlayerSingleton from "./singletons/CurrentPlayerSingleton";
import {Dice} from "../components/models/Dice";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {PlayerDTO} from "../components/models/PlayerDTO";

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

    const dice_1 = new Dice();
    dice_1.loadDice(loader,0,0,1);

    const dice_2 = new Dice();
    dice_2.loadDice(loader,1,1,2);
}

export function updateGame(newGameRaw: GameDTO) {
    const oldGame = GameSingleton.getInstance();
    const world = WorldSingleton.getInstance();

    if (!oldGame || !world) return;
    const newGame = GameDTO.fromRaw(newGameRaw);

    const newPlayersColors = new Set(newGame.players.map(p => p.color));
    const removedPlayers = oldGame.players.filter(p => !newPlayersColors.has(p.color));
    removedPlayers.forEach(removedPlayer => {
        const modelToRemove = world.scene.children.find(
            (child) => child.userData.color === removedPlayer.color
        );
        if (modelToRemove) {
            world.scene.remove(modelToRemove);
        }
    });

    const oldBuildingNames = new Set(
        world.scene.children
            .filter(e => e.userData.isBuilding)
            .map(b => b.userData.buildingName)
    );
    const newBuildingNames = new Set(
        newGame.players.flatMap(player =>
            player.ownedProperties.map(prop => prop.displayName)
        )
    );
    const removedBuildings = Array.from(oldBuildingNames).filter(
        name => !newBuildingNames.has(name)
    );
    removedBuildings.forEach(buildingName => {
        const buildingToRemove = world.scene.children.find(
            child => child.userData.isBuilding && child.userData.buildingName === buildingName
        );
        if (buildingToRemove) {
            world.scene.remove(buildingToRemove);
        }
    });

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

    GameSingleton.update(newGame);
    const currentPlayer: PlayerDTO | undefined = newGame.players.find(
        p => p.playerId === CurrentPlayerSingleton.getInstance()?.playerId
    );

    if (currentPlayer) {
        CurrentPlayerSingleton.update(currentPlayer);
    }
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
            if (child?.userData?.dice===1) {
                child.position.set(update.pos_dice1.x, update.pos_dice1.y, update.pos_dice1.z);
                child.quaternion.set(update.rot_dice1.x, update.rot_dice1.y, update.rot_dice1.z, update.rot_dice1.w);
            }
            if (child?.userData?.dice===2) {
                child.position.set(update.pos_dice2.x, update.pos_dice2.y, update.pos_dice2.z);
                child.quaternion.set(update.rot_dice2.x, update.rot_dice2.y, update.rot_dice2.z, update.rot_dice2.w);
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

