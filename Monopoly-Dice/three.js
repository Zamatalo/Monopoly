const WebSocket = require("ws");
const RAPIER = require("@dimforge/rapier3d");

const wss = new WebSocket("ws://localhost:8083");
let stateArray = [];
const world = new RAPIER.World({x: 0, y: -9.81, z: 0});

const rigidBodyDesc = RAPIER.RigidBodyDesc.dynamic().setTranslation(0, 5, 0);
const rigidBody = world.createRigidBody(rigidBodyDesc);
const colliderDesc = RAPIER.ColliderDesc.cuboid(1, 1, 1);
world.createCollider(colliderDesc, rigidBody);

function update() {
    world.step();
    const pos = rigidBody.translation();
    const state = JSON.stringify(pos);
    stateArray.push(state);
    setTimeout(update, 16);
}

update();

