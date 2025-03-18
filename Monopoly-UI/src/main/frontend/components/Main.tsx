import * as THREE from 'three';
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls.js";
import {GameDTO} from "Frontend/components/GameDTO";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";

let scene = new THREE.Scene();
let loader = new GLTFLoader();

export function initThreeJS(container: HTMLDivElement) {
    const camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
    camera.position.z = 5;
    const renderer = new THREE.WebGLRenderer({antialias: true});
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    container.appendChild(renderer.domElement);

    const controls = new OrbitControls(camera, renderer.domElement);

    scene.background = new THREE.CubeTextureLoader()
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
        controls.update();
        renderer.render(scene, camera);
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
    let ambientLight = new THREE.AmbientLight(0xffffff, 1);
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


}

export async function loadState(game: GameDTO): Promise<void> {
    if (scene.children.length === 0) {
        try {
            await game.loadBoardModel(loader, scene);
            for (const player of game.players) {
                await player.loadPlayerModel(loader, scene);
            }

            setupLighting();
            console.log(scene);
            console.log('Loaded scene with game state:', game);
        } catch (error) {
            console.error('Error loading game state:', error);
        }
    } else {
        game.players.forEach((newPlayerData) => {
            const player = game.players.find((p) => p.color === newPlayerData.color);
            if (player) {
                const oldPosition = player.position;
                const newPosition = newPlayerData.position;

                if (oldPosition !== newPosition) {
                    const playerModel = scene.children.find(
                        (obj) => obj.userData?.playerId === player.playerId
                    );

                    if (playerModel) {
                        //player.animatePlayerMovement(newPosition);
                    } else {
                        console.error(`Player model for ${player.color} not found`);
                    }
                }
            }
        });
    }
}