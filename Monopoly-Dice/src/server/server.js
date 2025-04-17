import {createClient} from "redis";

const RAPIER = await import('@dimforge/rapier3d-compat');

await RAPIER.init()
const gravity = new RAPIER.Vector3(0.0, -9.81, 0.0)
const world = new RAPIER.World(gravity)
let games = new Map();


const redisClient = createClient();
await redisClient.connect();

await redisClient.subscribe('game:game_roll', (message) => {
    const { gameId } = JSON.parse(message);
    const game = games.get(gameId) || new DiceGame(gameId);
    game.throwDice();
});
function sendDiceResult(gameId, result) {
    redisClient.publish('dice_result', JSON.stringify({
        gameId,
        value: result
    }));
}
async function validateSession(sessionId, gameId) {
    const session = await redisClient.hGetAll(`session:${sessionId}`);
    if (!session || session.gameId !== gameId) {
        throw new Error("Invalid session");
    }
    await redisClient.hSet(`session:${sessionId}`, "lastActivity", new Date().toISOString());
}

class DiceGame {
    constructor(gameId) {
        this.gameId = gameId;
        this.dice = null;
        this.setupPhysics();
        setInterval(() => {
            this.update();
        }, 10);

    }

    setupPhysics() {
        const rigidBodyDesc = RAPIER.RigidBodyDesc.fixed();
        const rigidBody = world.createRigidBody(rigidBodyDesc);
        const colliderDesc = RAPIER.ColliderDesc.cuboid(11, 0.1, 11).setTranslation(0, 0.1, 0);
        world.createCollider(colliderDesc, rigidBody);

        const diceDesc = RAPIER.RigidBodyDesc.dynamic()
            .setTranslation(0, 0.2, 0)
            .setUserData("isDice");
        this.dice = world.createRigidBody(diceDesc);
        const diceCollider = RAPIER.ColliderDesc.cuboid(0.16, 0.16, 0.16)
            .setFriction(10)
            .setMass(0.06)
            .setRestitution(0.4);
        world.createCollider(diceCollider, this.dice);

        this.createInboundBox();
    }

    createInboundBox() {
        const wallThickness = 0.2;
        const boxSize = 9;
        const rigidBodyBox = world.createRigidBody(RAPIER.RigidBodyDesc.fixed());

        const walls = [
            RAPIER.ColliderDesc.cuboid(boxSize, wallThickness, boxSize).setTranslation(0, boxSize, 0), // Top
            RAPIER.ColliderDesc.cuboid(wallThickness, boxSize / 2, boxSize).setTranslation(-boxSize, boxSize / 2, 0), // Left
            RAPIER.ColliderDesc.cuboid(wallThickness, boxSize / 2, boxSize).setTranslation(boxSize, boxSize / 2, 0), // Right
            RAPIER.ColliderDesc.cuboid(boxSize, boxSize / 2, wallThickness).setTranslation(0, boxSize / 2, -boxSize), // Front
            RAPIER.ColliderDesc.cuboid(boxSize, boxSize / 2, wallThickness).setTranslation(0, boxSize / 2, boxSize) // Back
        ];

        walls.forEach(desc => world.createCollider(desc, rigidBodyBox));
    }

    throwDice() {
        this.resultSent = false;
        const linvel = new RAPIER.Vector3(
            (Math.random() - 0.5) * 15,
            Math.random() * 20,
            (Math.random() - 0.5) * 15
        );
        this.dice.setLinvel(linvel, true);

        const angvel = new RAPIER.Vector3(
            (Math.random() - 0.5) * 10,
            (Math.random() - 0.5) * 10,
            (Math.random() - 0.5) * 10
        );
        this.dice.setAngvel(angvel, true);
    }

    update() {
        world.step();

        if (!this.dice.isSleeping()) {
            const pos = this.dice.translation();
            const rot = this.dice.rotation();
            var a = {pos: pos, rot: rot}
            publisher.publish('dice_StateUpdated', JSON.stringify(a));
        } else if (!this.resultSent) {
            this.sendDiceResult();
            this.resultSent = true;
        }
    }

    sendDiceResult() {
        const faces = [1, 2, 3, 4, 5, 6];
        const result = faces[Math.floor(Math.random() * faces.length)];
        publisher.publish('dice_TopFace',JSON.stringify({gameId:this.gameId,asd:result}));
    }
}



