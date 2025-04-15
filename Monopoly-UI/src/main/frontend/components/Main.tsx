import {Object3D} from 'three';
import {GameDTO} from "Frontend/components/objects/GameDTO";
import {World} from "./World";
import Stats from 'three/addons/libs/stats.module.js'
import {Dice} from "Frontend/components/objects/Dice";
import {GUI} from 'three/addons/libs/lil-gui.module.min.js';

let debug = false;
let game: GameDTO;
let world: World;
let dice: Dice, stats: Stats, diceState: any;

export function initThreeJS(container: HTMLDivElement) {
    world = new World(container);

    function animate() {
        requestAnimationFrame(animate);
        if (dice != null && dice.model && debug) {
            diceState.position.x = dice.model.position.x;
            diceState.position.y = dice.model.position.y;
            diceState.position.z = dice.model.position.z;
            diceState.topFace = dice.getDiceTopFace();
            stats.update()
        }
        world.controls.update();
        world.renderer.render(world.scene, world.camera);
    }
    animate();
}

function setupGui(dice: Dice) {
    const gui = new GUI();

    diceState = {
        position: {x: 0, y: 0, z: 0},
        rotation: {x: 0, y: 0, z: 0, w: 1},
        isSleeping: false,
        topFace: 1,
        isMoving: () => {
            if (!dice.model) return false;
            return false;
        },
        resetDice: () => {
            dice.model.position.set(0, 0, 0);
            dice.model.rotation.set(0, 0, 0);
        }
    };

    const diceFolder = gui.addFolder('Dice State');
    diceFolder.add(diceState.position, 'x').name('Pos X').listen();
    diceFolder.add(diceState.position, 'y').name('Pos Y').listen();
    diceFolder.add(diceState.position, 'z').name('Pos Z').listen();
    diceFolder.add(diceState, 'isSleeping').name('Is Sleeping').listen();
    diceFolder.add(diceState, 'topFace').name('Top Face').listen();
    diceFolder.add(diceState, 'resetDice').name('Reset Dice');

    stats = new Stats();
    document.body.appendChild(stats.dom);
}


export async function loadState(newGame: GameDTO) {
    async function initIfNewDTO() {
        try {
            game = newGame;
            game.loadBoardModel(world);
            for (const player of game.players) {
                await player.loadPlayerModel(world);
            }
            world.setupLighting()
            dice = new Dice();
            await dice.loadDice(world);
            if (debug) {
                setupGui(dice)
            }
            console.log('Loaded scene with game state:', game);
        } catch (error) {
            console.error('Error loading game state:', error);
        }
    }

    async function initIfUpdate() {
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

    if (world.scene.children.length === 0) { //init new
        await initIfNewDTO();
    } else {
        await initIfUpdate()
    }
}

window.addEventListener("keydown", ev => {
    if ((ev.key == "r" || ev.key == "R")) {
        //dice.throwDice(game.gameId)
    }
    if (ev.key == "q" || ev.key == "Q") {
        console.log("Current top result:", dice.getDiceTopFace());
    }
});
