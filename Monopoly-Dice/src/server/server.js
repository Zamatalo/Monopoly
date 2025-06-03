import { createClient } from "redis";
import * as RAPIER from "@dimforge/rapier3d-compat";

await RAPIER.init();

const gravity = new RAPIER.Vector3(0.0, -9.81, 0.0);
const world = new RAPIER.World(gravity);
const games = new Map();

const sub = createClient();
const pub = createClient();
await sub.connect();
await pub.connect();

await sub.pSubscribe('game:*:dice-roll-action', async (message, channel) => {
    const match = channel.match(/^game:(.*):dice-roll-action$/);
    if (!match) return;
    const gameId = match[1];

    let game = games.get(gameId);
    if (!game) {
        game = new DiceGame(gameId);
        games.set(gameId, game);
    }
    game.throwDice();
});

class DiceGame {
    constructor(gameId) {
        this.gameId = gameId;
        this.dice = null;
        this.resultSent = false;
        this.hasThrown = false;
        this.lastUpdateTime = 0;
        this.UPDATE_INTERVAL_MS = 30;
        this.setupPhysics();

        setInterval(() => this.physicsLoop(), 13);
    }

    setupPhysics() {
        const groundBody = world.createRigidBody(RAPIER.RigidBodyDesc.fixed());
        const groundCollider = RAPIER.ColliderDesc.cuboid(11, 0.1, 11).setTranslation(0, 0.1, 0);
        world.createCollider(groundCollider, groundBody);

        this.dice = world.createRigidBody(RAPIER.RigidBodyDesc.dynamic().setTranslation(0, 0.4, 0));
        const diceCollider = RAPIER.ColliderDesc.cuboid(0.16, 0.16, 0.16)
            .setFriction(3).setMass(1).setRestitution(0.3);
        world.createCollider(diceCollider, this.dice);

        this.createInboundBox();
    }

    createInboundBox() {
        const wallThickness = 0.2;
        const boxSize = 9;
        const rigidBodyBox = world.createRigidBody(RAPIER.RigidBodyDesc.fixed());

        const walls = [
            RAPIER.ColliderDesc.cuboid(boxSize, wallThickness, boxSize).setTranslation(0, boxSize, 0),
            RAPIER.ColliderDesc.cuboid(wallThickness, boxSize / 2, boxSize).setTranslation(-boxSize, boxSize / 2, 0),
            RAPIER.ColliderDesc.cuboid(wallThickness, boxSize / 2, boxSize).setTranslation(boxSize, boxSize / 2, 0),
            RAPIER.ColliderDesc.cuboid(boxSize, boxSize / 2, wallThickness).setTranslation(0, boxSize / 2, -boxSize),
            RAPIER.ColliderDesc.cuboid(boxSize, boxSize / 2, wallThickness).setTranslation(0, boxSize / 2, boxSize)
        ];

        walls.forEach(desc => world.createCollider(desc, rigidBodyBox));
    }

    throwDice() {
        if (this.hasThrown && !this.dice.isSleeping()) return;

        this.hasThrown = true;
        this.resultSent = false;

        const throwStrength = 6;
        const angleVariation = Math.PI / 4;

        const direction = new RAPIER.Vector3(
            (Math.random() - 0.5) * angleVariation,
            0.7 + Math.random() * 0.3,
            (Math.random() - 0.5) * angleVariation
        );

        this.dice.setLinvel(
            new RAPIER.Vector3(
                direction.x * throwStrength,
                direction.y * throwStrength,
                direction.z * throwStrength
            ),
            true
        );

        this.dice.setAngvel(
            new RAPIER.Vector3(
                (Math.random() - 0.5) * 15,
                (Math.random() - 0.5) * 15,
                (Math.random() - 0.5) * 15
            ),
            true
        );
    }

    rotateVectorByQuaternion(vec, quat) {
        const x = quat.x, y = quat.y, z = quat.z, w = quat.w;
        const vx = vec.x, vy = vec.y, vz = vec.z;

        const ix = w * vx + y * vz - z * vy;
        const iy = w * vy + z * vx - x * vz;
        const iz = w * vz + x * vy - y * vx;
        const iw = -x * vx - y * vy - z * vz;

        return new RAPIER.Vector3(
            ix * w + iw * -x + iy * -z - iz * -y,
            iy * w + iw * -y + iz * -x - ix * -z,
            iz * w + iw * -z + ix * -y - iy * -x
        );
    }

    getTopFaceFromQuaternion(quat) {
        const faceNormals = [
            { face: 1, normal: new RAPIER.Vector3(0, 0, -1) },
            { face: 2, normal: new RAPIER.Vector3(1, 0, 0) },
            { face: 3, normal: new RAPIER.Vector3(0, 1, 0) },
            { face: 4, normal: new RAPIER.Vector3(0, -1, 0) },
            { face: 5, normal: new RAPIER.Vector3(-1, 0, 0) },
            { face: 6, normal: new RAPIER.Vector3(0, 0, 1) }
        ];

        const up = new RAPIER.Vector3(0, 1, 0);

        let maxDot = -Infinity;
        let topFace = 1;

        for (const { face, normal } of faceNormals) {
            const rotated = this.rotateVectorByQuaternion(normal, quat);
            const dot = rotated.x * up.x + rotated.y * up.y + rotated.z * up.z;

            if (dot > maxDot) {
                maxDot = dot;
                topFace = face;
            }
        }

        return topFace;
    }


    physicsLoop() {
        world.step();

        const now = Date.now();
        if (now - this.lastUpdateTime >= this.UPDATE_INTERVAL_MS) {
            this.lastUpdateTime = now;
            this.sendUpdate();
        }
    }

    sendUpdate() {
        if (!this.dice.isSleeping()) {
            const pos = this.dice.translation();
            const rot = this.dice.rotation();

            pub.publish(`game:diceUpdate`, JSON.stringify({
                gameId: this.gameId,
                pos: { x: pos.x, y: pos.y, z: pos.z },
                rot: { x: rot.x, y: rot.y, z: rot.z, w: rot.w }
            }));
        } else if (!this.resultSent) {
            const topFace = this.getTopFaceFromQuaternion(this.dice.rotation());

            pub.publish(`game:diceResult`, JSON.stringify({
                gameId: this.gameId,
                diceResult: topFace
            }));

            this.resultSent = true;
        }
    }
}
