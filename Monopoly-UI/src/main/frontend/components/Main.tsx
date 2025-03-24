import * as THREE from 'three';
import {Object3D} from 'three';
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls.js";
import {GameDTO} from "Frontend/components/GameDTO";
import {World} from "./World";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import * as RAPIER from "@dimforge/rapier3d";

let game: GameDTO;
let world = new World();
export function initThreeJS(container: HTMLDivElement) {
    const camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
    camera.position.z = 10;
    camera.position.y = 15;
    camera.lookAt(new THREE.Vector3(0, 0, 0));

    const renderer = new THREE.WebGLRenderer({
        antialias: true,
        powerPreference: "high-performance"
    });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    container.appendChild(renderer.domElement);


    const controls = new OrbitControls(camera, renderer.domElement);
    world.scene.background = new THREE.CubeTextureLoader()
        .setPath('/assets/skybox/')
        .load([
            'cubemap_0.png', // Right
            'cubemap_1.png', // Left
            'cubemap_2.png', // Top
            'cubemap_3.png', // Bottom
            'cubemap_4.png', // Back
            'cubemap_5.png', // Front
        ]);

    function animate() {
        requestAnimationFrame(animate);

        if (world.world) {
            world.update(1 / 60);
        }
        controls.update();
        renderer.render(world.scene, camera);
    }

    animate();

    window.addEventListener('resize', () => {
        const width = container.clientWidth;
        const height = container.clientHeight;

        camera.aspect = width / height;
        camera.updateProjectionMatrix();
        renderer.setSize(width, height);
    });
}

function setupLighting() {
    let ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
    world.scene.add(ambientLight);

    const color = 0xFFCC33;
    const intensity2 = 75000;
    const light2 = new THREE.PointLight(color, intensity2);
    light2.castShadow = true;
    light2.position.set(100, 100, -260);
    light2.shadow.bias = -0.001;
    light2.shadow.mapSize.width = 4096;
    light2.shadow.mapSize.height = 4096;
    world.scene.add(light2);
}

function createInboundBox() {
    const wallThickness = 0.2;
    const boxSize = 5;
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

async function loadDice() {
    const boardPath = "/assets/models/dice3.glb";
    const loader = new GLTFLoader();
    return new Promise<void>((resolve, reject) => {
        loader.load(
            boardPath,
            (gltf: any) => {
                let model = gltf.scene;
                model.userData = {isDice: true};
                model.traverse((obj: any) => {
                    if (obj.castShadow !== undefined) {
                        obj.castShadow = true;
                        obj.receiveShadow = true;
                    }
                });
                world.scene.add(model);

                const axesHelper = new THREE.AxesHelper(5);
                model.add(axesHelper);

                const rigidBodyDesc = RAPIER.RigidBodyDesc.dynamic()
                    .setTranslation(0, 4, 2)
                    .setUserData("isDice");
                const rigidBody = world.world.createRigidBody(rigidBodyDesc);
                const colliderDesc = RAPIER.ColliderDesc.cuboid(0.16, 0.16, 0.16)
                    .setTranslation(0, 0, 0)
                    .setFriction(7)
                    .setRestitution(0.4);
                world.world.createCollider(colliderDesc, rigidBody);
                world.addBody(model, rigidBody);
                createInboundBox();
                resolve();
                console.log('Dice model loaded');
            },
            undefined,
            (error: any) => {
                console.error(`Error loading model: ${error}`);
                reject(error);
            }
        );
    });
}

window.addEventListener("keydown", ev => {
    var rigidBody = world.bodies.filter(e => e.userData === "isDice");

    if ((ev.key == "r" || ev.key == "R") && rigidBody.every(e => !e.isMoving())) {
        const linvelX = (Math.random() - 0.5) * 0.15;
        const linvelY = Math.random() * 0.2;
        const linvelZ = (Math.random() - 0.5) * 0.15;
        rigidBody.forEach(e => e.applyImpulse({x: linvelX, y: linvelY, z: linvelZ}, true));

        const angvelX = (Math.random() - 0.5) * 0.15;
        const angvelY = (Math.random() - 0.5) * 0.15;
        const angvelZ = (Math.random() - 0.5) * 0.15;
        rigidBody.forEach(e => e.applyTorqueImpulse({x: angvelX, y: angvelY, z: angvelZ}, true));


    }
    if (ev.key == " " || ev.code == "Space") {
        rigidBody.forEach(e => e.setTranslation({x: 0, y: 4, z: 0}, true));
    }
    if (ev.key == "q" || ev.key == "Q") {
        let result: number = 0;
        rigidBody.forEach(e => result += getDiceTopFace(e.rotation()));
        console.log("Current top result:", result);
    }
});

function getDiceTopFace(rotation: any): number {
    const faceNormals = [
        new THREE.Vector3(0, 0, -1),
        new THREE.Vector3(1, 0, 0),
        new THREE.Vector3(0, 1, 0),
        new THREE.Vector3(0, -1, 0),
        new THREE.Vector3(-1, 0, 0),
        new THREE.Vector3(0, 0, 1)
    ];
    const diceQuaternion = new THREE.Quaternion(rotation.x, rotation.y, rotation.z, rotation.w);
    const worldUp = new THREE.Vector3(0, 1, 0);

    let maxDot = -Infinity;
    let topFace = 0;
    for (let i = 0; i < faceNormals.length; i++) {
        const normal = faceNormals[i].clone();
        normal.applyQuaternion(diceQuaternion);
        const dot = normal.dot(worldUp);
        if (dot > maxDot) {
            maxDot = dot;
            topFace = i + 1;
        }
    }
    return topFace;
}
export async function loadState(newGame: GameDTO): Promise<void> {
    if (world.scene.children.length === 0) {
        try {
            game = newGame;
            await game.loadBoardModel(world);
            for (const player of game.players) {
                await player.loadPlayerModel(world);
            }

            setupLighting();
            await loadDice();
            await loadDice();
            console.log('Loaded scene with game state:', game);
        } catch (error) {
            console.error('Error loading game state:', error);
        }
    } else {
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

