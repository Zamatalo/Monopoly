import * as THREE from 'three'
import {OrbitControls} from 'three/addons/controls/OrbitControls.js'
import Stats from 'three/addons/libs/stats.module.js'
import {GUI} from 'three/addons/libs/lil-gui.module.min.js'
import RAPIER from '@dimforge/rapier3d-compat'
import {GLTFLoader} from "three/addons";



await RAPIER.init()
const gravity = new RAPIER.Vector3(0.0, -9.81, 0.0)
const world = new RAPIER.World(gravity)
const dynamicBodies = []
const loader = new GLTFLoader()
const scene = new THREE.Scene()
const clock = new THREE.Clock()
let dice;
let delta

class RapierDebugRenderer {
    mesh
    world
    enabled = true

    constructor(scene, world) {
        this.world = world
        this.mesh = new THREE.LineSegments(new THREE.BufferGeometry(), new THREE.LineBasicMaterial({
            color: 0xffffff,
            vertexColors: true
        }))
        this.mesh.frustumCulled = false
        scene.add(this.mesh)
    }

    update() {
        if (this.enabled) {
            const {vertices, colors} = world.debugRender()
            this.mesh.geometry.setAttribute('position', new THREE.BufferAttribute(vertices, 3))
            this.mesh.geometry.setAttribute('color', new THREE.BufferAttribute(colors, 4))
            this.mesh.visible = true
        } else {
            this.mesh.visible = false
        }
    }
}
const rapierDebugRenderer = new RapierDebugRenderer(scene, world)
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 100)
camera.position.set(0, 2, 5)

const renderer = new THREE.WebGLRenderer({antialias: true})
renderer.setSize(window.innerWidth, window.innerHeight)
renderer.shadowMap.enabled = true
renderer.shadowMap.type = THREE.PCFSoftShadowMap
document.body.appendChild(renderer.domElement)

window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight
    camera.updateProjectionMatrix()
    renderer.setSize(window.innerWidth, window.innerHeight)
})

const controls = new OrbitControls(camera, renderer.domElement)
controls.enableDamping = true
controls.target.y = 1


const stats = new Stats()
document.body.appendChild(stats.dom)

const gui = new GUI()
gui.add(rapierDebugRenderer, 'enabled').name('Rapier Degug Renderer')

const physicsFolder = gui.addFolder('Physics')
physicsFolder.add(world.gravity, 'x', -10.0, 10.0, 0.1)
physicsFolder.add(world.gravity, 'y', -10.0, 10.0, 0.1)
physicsFolder.add(world.gravity, 'z', -10.0, 10.0, 0.1)

const diceState = {
    position: { x: 0, y: 0, z: 0 },
    rotation: { x: 0, y: 0, z: 0, w: 1 },
    isSleeping: false,
    isMoving: () => {
        if (!dice) return false;
        const linvel = dice.linvel();
        const angvel = dice.angvel();
        return !dice.isSleeping() ||
            Math.abs(linvel.x) > 0.01 ||
            Math.abs(linvel.y) > 0.01 ||
            Math.abs(linvel.z) > 0.01 ||
            Math.abs(angvel.x) > 0.01 ||
            Math.abs(angvel.y) > 0.01 ||
            Math.abs(angvel.z) > 0.01;
    },
    resetDice: () => {
        if (!dice) return;
        dice.setTranslation(new RAPIER.Vector3(0, 5, 0), true);
        dice.setLinvel(new RAPIER.Vector3(0, 0, 0), true);
        dice.setAngvel(new RAPIER.Vector3(0, 0, 0), true);
    }
};

const diceFolder = gui.addFolder('Dice State');
diceFolder.add(diceState.position, 'x').name('Pos X').listen();
diceFolder.add(diceState.position, 'y').name('Pos Y').listen();
diceFolder.add(diceState.position, 'z').name('Pos Z').listen();
diceFolder.add(diceState, 'isSleeping').name('Is Sleeping').listen();
diceFolder.add(diceState, 'resetDice').name('Reset Dice');

let ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
scene.add(ambientLight);
const color = 0xFFCC33;
const intensity2 = 75000;
const light2 = new THREE.PointLight(color, intensity2);
light2.castShadow = true;
light2.position.set(100, 100, -260);
light2.shadow.bias = -0.001;
light2.shadow.mapSize.width = 4096;
light2.shadow.mapSize.height = 4096;
scene.add(light2);


const loadBoard = () => {
    const boardPath = 'assets/monopolyBoard.glb';
    loader.load(
        boardPath,
        (gltf) => {
            const model = gltf.scene;
            model.position.set(0, 0, 0);
            model.userData = {isBoard: true};
            model.scale.set(1, 1, 1);
            model.traverse((obj) => {
                if (obj.castShadow !== undefined) {
                    obj.receiveShadow = true;
                }
            });
            scene.add(model);

            const rigidBodyDesc = RAPIER.RigidBodyDesc.fixed();
            const rigidBody = world.createRigidBody(rigidBodyDesc);
            const colliderDesc = RAPIER.ColliderDesc.cuboid(11, 0.1, 11).setTranslation(0,0.1,0);
            world.createCollider(colliderDesc, rigidBody);
            dynamicBodies.push([model, rigidBody])
            console.log('Board model loaded');
        },
        undefined,
        (error) => {
            console.error(`Error loading model: ${error}`);
        }
    );
}

function loadDice() {
    const boardPath = "/assets/dice3.glb";
    loader.load(
        boardPath,
        (gltf) => {
            let model = gltf.scene;
            model.userData = {isDice: true};
            model.traverse((obj) => {
                if (obj.castShadow !== undefined) {
                    obj.castShadow = true;
                    obj.receiveShadow = true;
                }
            });
            scene.add(model);

            const rigidBodyDesc = RAPIER.RigidBodyDesc.dynamic()
                .setTranslation(0, 0.2, 0)
                .setUserData("isDice");
            dice = world.createRigidBody(rigidBodyDesc);
            const colliderDesc = RAPIER.ColliderDesc.cuboid(0.16, 0.16, 0.16)
                .setTranslation(0, 0, 0)
                .setFriction(10)
                .setMass(0.06)
                .setRestitution(0.4);
            world.createCollider(colliderDesc, dice);

            createInboundBox();
            dynamicBodies.push([model, dice]);
            console.log('Dice model loaded');
        },
        undefined,
        (error) => {
            console.error(`Error loading model: ${error}`);
        }
    );
}

function createInboundBox() {
    const wallThickness = 0.2;
    const boxSize = 9;
    const rigidBodyForBox = RAPIER.RigidBodyDesc.fixed();
    const rigidBodyBox = world.createRigidBody(rigidBodyForBox);
    const topDesc = RAPIER.ColliderDesc.cuboid(boxSize, wallThickness, boxSize).setTranslation(0, boxSize, 0);
    world.createCollider(topDesc, rigidBodyBox);

    const leftDesc = RAPIER.ColliderDesc.cuboid(wallThickness, boxSize/2, boxSize).setTranslation(-boxSize, boxSize/2, 0);
    world.createCollider(leftDesc, rigidBodyBox);

    const rightDesc = RAPIER.ColliderDesc.cuboid(wallThickness, boxSize/2, boxSize).setTranslation(boxSize, boxSize/2, 0);
    world.createCollider(rightDesc, rigidBodyBox);

    const frontDesc = RAPIER.ColliderDesc.cuboid(boxSize, boxSize/2, wallThickness).setTranslation(0, boxSize/2, -boxSize);
    world.createCollider(frontDesc, rigidBodyBox);
    const backDesc = RAPIER.ColliderDesc.cuboid(boxSize, boxSize/2, wallThickness).setTranslation(0, boxSize/2, boxSize);
    world.createCollider(backDesc, rigidBodyBox);
}

function animate() {
    requestAnimationFrame(animate)

    delta = clock.getDelta()
    world.timestep = Math.min(delta, 0.01)
    world.step()

    for (let i = 0, n = dynamicBodies.length; i < n; i++) {
        dynamicBodies[i][0].position.copy(dynamicBodies[i][1].translation())
        dynamicBodies[i][0].quaternion.copy(dynamicBodies[i][1].rotation())
    }
    if (dice) {
        const pos = dice.translation();
        const rot = dice.rotation();
        diceState.position.x = pos.x;
        diceState.position.y = pos.y;
        diceState.position.z = pos.z;
        diceState.rotation.x = rot.x;
        diceState.rotation.y = rot.y;
        diceState.rotation.z = rot.z;
        diceState.rotation.w = rot.w;
        diceState.isSleeping = dice.isSleeping();
    }

    rapierDebugRenderer.update()
    controls.update()
    renderer.render(scene, camera)
    stats.update()
}

function setupKeyboardControls() {
    window.addEventListener('keydown', (ev) => {
        if (ev.key.toLowerCase() === 'r'&& !dice.isMoving()) {
            const linvelX = (Math.random() - 0.5) * 0.15;
            const linvelY = Math.random() * 0.2;
            const linvelZ = (Math.random() - 0.5) * 0.15;
            dice.applyImpulse({x: linvelX, y: linvelY, z: linvelZ}, true);

            const angvelX = (Math.random() - 0.5) * 0.15;
            const angvelY = (Math.random() - 0.5) * 0.15;
            const angvelZ = (Math.random() - 0.5) * 0.15;
            dice.applyTorqueImpulse({x: angvelX, y: angvelY, z: angvelZ}, true);
        }
    });
}

setupKeyboardControls()
loadBoard()
loadDice()
animate()


