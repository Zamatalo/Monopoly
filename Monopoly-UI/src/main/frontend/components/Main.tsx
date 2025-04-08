import {Object3D} from 'three';
import {GameDTO} from "Frontend/components/GameDTO";
import {World} from "./World";
import * as RAPIER from "@dimforge/rapier3d";
import {GUI} from 'three/addons/libs/lil-gui.module.min.js'
import Stats from 'three/addons/libs/stats.module.js'
import {Dice} from "Frontend/components/Dice";


let game: GameDTO;
let world: World;
let dice: Dice, stats: Stats, diceState:any;
export function initThreeJS(container: HTMLDivElement) {
    world = new World(container);

    function animate() {
        requestAnimationFrame(animate);
        if (world.world) {
            world.update(1 / 60);
        }
        if (dice!=null && dice.body) {
            const pos = dice.body.translation();
            const rot = dice.body.rotation();
            // if(!dicebody.isSleeping()) {
            //     socket.emit('updateDiceState', {position: pos, rotation: rot});
            // }
            diceState.position.x = pos.x;
            diceState.position.y = pos.y;
            diceState.position.z = pos.z;
            diceState.rotation.x = rot.x;
            diceState.rotation.y = rot.y;
            diceState.rotation.z = rot.z;
            diceState.rotation.w = rot.w;
            diceState.isSleeping = dice.body.isSleeping();
            stats.update();
        }
        world.controls.update();
        world.renderer.render(world.scene, world.camera);
    }
    animate();
}

function setupGui() {
    const gui = new GUI();
    diceState = {
        position: {x: 0, y: 0, z: 0},
        rotation: {x: 0, y: 0, z: 0, w: 1},
        isSleeping: false,
        isMoving: () => {
            if (!dice.body) return false;
            const linvel = dice.body.linvel();
            const angvel = dice.body.angvel();
            return !dice.body.isSleeping() ||
                Math.abs(linvel.x) > 0.01 ||
                Math.abs(linvel.y) > 0.01 ||
                Math.abs(linvel.z) > 0.01 ||
                Math.abs(angvel.x) > 0.01 ||
                Math.abs(angvel.y) > 0.01 ||
                Math.abs(angvel.z) > 0.01;
        },
        resetDice: () => {
            if (!dice.body) return;
            dice.body.setTranslation(new RAPIER.Vector3(0, 5, 0), true);
            dice.body.setLinvel(new RAPIER.Vector3(0, 0, 0), true);
            dice.body.setAngvel(new RAPIER.Vector3(0, 0, 0), true);
        }
    };

    const diceFolder = gui.addFolder('Dice State');
    diceFolder.add(diceState.position, 'x').name('Pos X').listen();
    diceFolder.add(diceState.position, 'y').name('Pos Y').listen();
    diceFolder.add(diceState.position, 'z').name('Pos Z').listen();
    diceFolder.add(diceState, 'isSleeping').name('Is Sleeping').listen();
    diceFolder.add(diceState, 'resetDice').name('Reset Dice');
}


function createInboundBox() {
    const wallThickness = 0.2;
    const boxSize = 10;
    const rigidBodyForBox = RAPIER.RigidBodyDesc.fixed();
    const rigidBodyBox = world.world.createRigidBody(rigidBodyForBox);
    const floorDesc = RAPIER.ColliderDesc.cuboid(boxSize, wallThickness, boxSize).setTranslation(0, -boxSize, 0);
    world.world.createCollider(floorDesc, rigidBodyBox);
    const topDesc = RAPIER.ColliderDesc.cuboid(boxSize, wallThickness, boxSize).setTranslation(0, boxSize, 0);
    world.world.createCollider(topDesc, rigidBodyBox);
    const leftDesc = RAPIER.ColliderDesc.cuboid(wallThickness, boxSize, boxSize).setTranslation(-boxSize, 0, 0);
    world.world.createCollider(leftDesc, rigidBodyBox);
    const rightDesc = RAPIER.ColliderDesc.cuboid(wallThickness, boxSize, boxSize).setTranslation(boxSize, 0, 0);
    world.world.createCollider(rightDesc, rigidBodyBox);
    const frontDesc = RAPIER.ColliderDesc.cuboid(boxSize, boxSize, wallThickness).setTranslation(0, 0, -boxSize);
    world.world.createCollider(frontDesc, rigidBodyBox);
    const backDesc = RAPIER.ColliderDesc.cuboid(boxSize, boxSize, wallThickness).setTranslation(0, 0, boxSize);
    world.world.createCollider(backDesc, rigidBodyBox);
}


async function animDice(diceFactor: number) {
    var rigidBody = world.bodies.filter(e => e.userData === "isDice");
    rigidBody.forEach(e => {
        e.resetForces(true);
        e.setRotation({x: 0, y: 0, z: 0, w: 0}, true)
        e.resetTorques(true);
        e.setTranslation({x: 0, y: 0.2, z: 0}, true)
    });

    const linvelX = (1 * 0.001) + 0.012;
    const linvelY = (1 * 0.001) + 0.013;
    const linvelZ = (1 * 0.001) + 0.014;
    console.log(linvelX, linvelY, linvelZ);
}

window.addEventListener("keydown", ev => {
    var rigidBody = world.bodies.filter(e => e.userData === "isDice");
    if ((ev.key == "r" || ev.key == "R") && rigidBody.every(e => !e.isMoving())) {
        dice.throwDice()
    }
    if (ev.key == " " || ev.code == "Space") {
        dice.getCurrentDicePos();
    }
    if (ev.key == "q" || ev.key == "Q") {
        console.log("Current top result:", dice.getDiceTopFace());
    }
});



export async function loadState(newGame: GameDTO) {
    async function initIfNewDTO() {
        try {
            game = newGame;
            await game.loadBoardModel(world);
            for (const player of game.players) {
                await player.loadPlayerModel(world);
            }
            world.setupLighting()
            dice = new Dice();
            await dice.loadDice(world);
            dice.getCurrentDicePos()
            setupGui()
            stats = new Stats();
            document.body.appendChild(stats.dom)
            createInboundBox()
            console.log('Loaded scene with game state:', game);
        } catch (error) {
            console.error('Error loading game state:', error);
        }
    }

    if (world.scene.children.length === 0) { //init new
        await initIfNewDTO();
    } else {  //update
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
}

