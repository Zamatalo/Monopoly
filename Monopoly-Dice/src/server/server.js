import {createServer} from 'http';
import {Server} from 'socket.io';
import RAPIER from '@dimforge/rapier3d-compat';

await RAPIER.init();
const gravity = new RAPIER.Vector3(0.0, -9.81, 0.0);
const world = new RAPIER.World(gravity);
let dice;
const httpServer = createServer();
let diceStateFromClient = [];
let prevStateFromClient = [];
const io = new Server(httpServer, {
    cors: {
        origin: "*",
    },
});

class someUI {
    dynamicBodies = [];

    constructor() {
        this.loadBoard();
        this.loadDice();
        this.animate();
    }

    loadBoard = () => {
        const rigidBodyDesc = RAPIER.RigidBodyDesc.fixed();
        const rigidBody = world.createRigidBody(rigidBodyDesc);
        const colliderDesc = RAPIER.ColliderDesc.cuboid(11, 0.1, 11).setTranslation(0, 0.1, 0);
        world.createCollider(colliderDesc, rigidBody);
        this.dynamicBodies.push(rigidBody);
        console.log("Board model loaded");
    };

    loadDice = () => {
        const rigidBodyDesc = RAPIER.RigidBodyDesc.dynamic()
            .setTranslation(0, 0.1, 0)
            .setUserData("isDice");
        dice = world.createRigidBody(rigidBodyDesc);
        const colliderDesc = RAPIER.ColliderDesc.cuboid(0.16, 0.16, 0.16)
            .setTranslation(0, 0, 0)
            .setFriction(10)
            .setMass(0.06)
            .setRestitution(0.4);
        world.createCollider(colliderDesc, dice);

        this.createInboundBox();
        this.dynamicBodies.push(dice);
        console.log("Dice model loaded");
    };

    createInboundBox = () => {
        const wallThickness = 0.2;
        const boxSize = 9;
        const rigidBodyForBox = RAPIER.RigidBodyDesc.fixed();
        const rigidBodyBox = world.createRigidBody(rigidBodyForBox);
        const topDesc = RAPIER.ColliderDesc.cuboid(boxSize, wallThickness, boxSize).setTranslation(0, boxSize, 0);
        world.createCollider(topDesc, rigidBodyBox);

        const leftDesc = RAPIER.ColliderDesc.cuboid(wallThickness, boxSize / 2, boxSize).setTranslation(-boxSize, boxSize / 2, 0);
        world.createCollider(leftDesc, rigidBodyBox);

        const rightDesc = RAPIER.ColliderDesc.cuboid(wallThickness, boxSize / 2, boxSize).setTranslation(boxSize, boxSize / 2, 0);
        world.createCollider(rightDesc, rigidBodyBox);

        const frontDesc = RAPIER.ColliderDesc.cuboid(boxSize, boxSize / 2, wallThickness).setTranslation(0, boxSize / 2, -boxSize);
        world.createCollider(frontDesc, rigidBodyBox);
        const backDesc = RAPIER.ColliderDesc.cuboid(boxSize, boxSize / 2, wallThickness).setTranslation(0, boxSize / 2, boxSize);
        world.createCollider(backDesc, rigidBodyBox);
    };

    throwDice = () => {
        const linvelX = (Math.random() - 0.5) * 0.15;
        const linvelY = Math.random() * 0.2;
        const linvelZ = (Math.random() - 0.5) * 0.15;
        dice.applyImpulse({x: linvelX, y: linvelY, z: linvelZ}, true);

        const angvelX = (Math.random() - 0.5) * 0.15;
        const angvelY = (Math.random() - 0.5) * 0.15;
        const angvelZ = (Math.random() - 0.5) * 0.15;
        dice.applyTorqueImpulse({x: angvelX, y: angvelY, z: angvelZ}, true);
    };

    isDiceEmitted = false;
    animate = () => {
        world.step();
        if (dice) {
            const pos = dice.translation();
            const rot = dice.rotation();
            if (!dice.isSleeping()) {
                io.emit("diceStateUpdated", {pos: pos, rot: rot});
            } else if (!this.isDiceEmitted && diceStateFromClient.length > 0) {
                prevStateFromClient = JSON.parse(JSON.stringify(diceStateFromClient))
                //io.emit("diceStateUpdated", prevStateFromClient);
                // diceStateFromClient = [];
                this.isDiceEmitted = true;
            }
        }
    };
}

var simulation = new someUI();

setInterval(() => {
    simulation.animate();
}, 10);

io.on("connection", (socket) => {
    console.log(`Client connected: ${socket.id}`);

    socket.on("throw", (msg, callback) => {
        if (callback && dice.isSleeping()) {
            simulation.throwDice();
        }
    });
    socket.on("getCurrentDicePos",(msg,callback) => {
        if (callback) {
            callback({pos:dice.translation(), rot:dice.rotation()});
        }
    });
    socket.on("disconnect", () => {
        socket.disconnect()
    });

    socket.on("error", (err) => {
        console.error("Socket error:", err);
    });
});

httpServer.listen(3001, () => {
    console.log("Server running on http://localhost:3001");
});
