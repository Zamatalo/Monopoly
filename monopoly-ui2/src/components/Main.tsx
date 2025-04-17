// import {Object3D} from 'three';
// import {GameDTO} from "./models/GameDTO";
// import {World} from "./models/World";
// import Stats from 'three/addons/libs/stats.module'
// import {Dice} from "./models/Dice";
// import {GUI} from 'three/addons/libs/lil-gui.module.min';
//
// let debug = false;
// let game: GameDTO;
// let world: World;
// let dice: Dice, stats: Stats, diceState: any;
//
// export function initThreeJS(container: HTMLDivElement) {
//     world = new World(container);
//
//
//
//
//
// export function loadState(newGame: GameDTO) {
//     function initIfNewDTO() {
//         try {
//             game = newGame;
//             game.loadBoardModel(world);
//             for (const player of game.players) {
//                 player.loadPlayerModel(world);
//             }
//             dice = new Dice();
//             dice.loadDice(world);
//             if (debug) {
//                 setupGui(dice)
//             }
//             console.log('Loaded scene with game state:', game);
//         } catch (error) {
//             console.error('Error loading game state:', error);
//         }
//     }
//
//     // function initIfUpdate() {
//     //     newGame.players.forEach((newPlayerData) => {
//     //         const oldPlayer = game.players.(p => p.color === newPlayerData.color);
//     //         if (oldPlayer) {
//     //             const newPosition = newPlayerData.position;
//     //             if (oldPlayer.position !== newPosition) {
//     //                 const playerModel = world.scene.children.find(e => e.userData.color == newPlayerData.color) as Object3D;
//     //                 oldPlayer.animatePlayerMovement(newPosition, playerModel);
//     //             }
//     //         }
//     //     });
//     // }
//
//     if (world.scene.children.length === 0) { //init new
//          initIfNewDTO();
//     } else {
//         // initIfUpdate()
//     }
// }
//
// window.addEventListener("keydown", ev => {
//     if ((ev.key == "r" || ev.key == "R")) {
//         //dice.throwDice(game.gameId)
//     }
//     if (ev.key == "q" || ev.key == "Q") {
//         console.log("Current top result:", dice.getDiceTopFace());
//     }
// });
