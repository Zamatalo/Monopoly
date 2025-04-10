import express from 'express';
import http from 'http';
import session from 'express-session';
import { Server } from 'socket.io';
import RAPIER from '@dimforge/rapier3d-compat';
import { v4 as uuidv4 } from 'uuid';

await RAPIER.init();
const gravity = new RAPIER.Vector3(0.0, -9.81, 0.0);
const world = new RAPIER.World(gravity);

//TODO for each game should be dedicated simulation, now it uses only one @diceState for all Games
const app = express();
const httpServer = http.createServer(app);
const sessionMiddleware = session({
    secret: 'your-secret',
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false },
});

app.use(sessionMiddleware);

const io = new Server(httpServer, {
    cors: {
        origin: '*',
    }
});
let games = new Map();
class someUI {
    dice;
    clients = new Set();
    constructor() {
        this.loadBoard();
        this.loadDice();
        this.animate();
        setInterval(() => {
            this.animate();
        }, 10);
    }

    loadBoard = () => {
        const rigidBodyDesc = RAPIER.RigidBodyDesc.fixed();
        const rigidBody = world.createRigidBody(rigidBodyDesc);
        const colliderDesc = RAPIER.ColliderDesc.cuboid(11, 0.1, 11).setTranslation(0, 0.1, 0);
        world.createCollider(colliderDesc, rigidBody);
        console.log("Board model loaded");
    };

    loadDice = () => {
        const rigidBodyDesc = RAPIER.RigidBodyDesc.dynamic()
            .setTranslation(0, 0.1, 0)
            .setUserData("isDice");
        this.dice = world.createRigidBody(rigidBodyDesc);
        const colliderDesc = RAPIER.ColliderDesc.cuboid(0.16, 0.16, 0.16)
            .setTranslation(0, 0, 0)
            .setFriction(10)
            .setMass(0.06)
            .setRestitution(0.4);
        world.createCollider(colliderDesc, this.dice);

        this.createInboundBox();
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
        this.dice.applyImpulse({x: linvelX, y: linvelY, z: linvelZ}, true);

        const angvelX = (Math.random() - 0.5) * 0.15;
        const angvelY = (Math.random() - 0.5) * 0.15;
        const angvelZ = (Math.random() - 0.5) * 0.15;
        this.dice.applyTorqueImpulse({x: angvelX, y: angvelY, z: angvelZ}, true);
    };

    animate = () => {
        world.step();
        if (this.dice) {
            const pos = this.dice.translation();
            const rot = this.dice.rotation();
            if (!this.dice.isSleeping()) {
                this.clients.forEach((client) => {
                    io.emit("diceStateUpdated", {pos: pos, rot: rot});
                })
            } else { //transmit current top face to backend with gameId
            }
        }
    };
    addClient = (socket) => {
        this.clients.add(socket);
    };

    removeClient = (socket) => {
        this.clients.delete(socket);
    };

}

io.on("connection", (socket) => {
    console.log(`Client connected: ${socket.id}`);
    socket.on("throw", (msg, callback) => {
        if (callback) {
            if (games.has(msg)) {
                if (games.get(msg).dice.isSleeping()) {
                    console.log(`Throwing dice for already added game: ${msg}`);
                    games.get(msg).throwDice();
                } else {
                    callback(`Dice animation for game ${msg} is still playing`);
                }
            }
        }
    });

    socket.on("getCurrentDicePos",(msg,callback) => {
        if (callback) {
            if (msg === null || msg.length === 0 || !isValidUUID(msg)) {
                callback("Bad uuid")
            }
            //check if game exists
            if (games.has(msg)) {
                if (games.get(msg).dice.isSleeping()) {
                    let game = games.get(msg);
                    callback({pos: game.dice.translation(), rot: game.dice.rotation()});
                } else {
                    callback(`Dice animation for game ${msg} is still playing`);
                }
            }else {
                console.log(`Adding new game: ${msg}`);
                createNewGame(msg);
                let game = games.get(msg);
                callback({pos: game.dice.translation(), rot: game.dice.rotation()});
            }
            console.log(`Adding new Client ${socket.id} to the game: ${msg}`);
            games.get(msg).addClient(socket);
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

const createNewGame = (gameId) => {
    games.set(gameId, new someUI());
}

function isValidUUID(uuid) {
    const regex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/;
    return regex.test(uuid);
}
