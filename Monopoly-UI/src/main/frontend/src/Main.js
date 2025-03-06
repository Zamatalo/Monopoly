import {PlayerObject} from "./PlayerObject.js";
import * as THREE from "three";
import {GameObject} from "./GameObject.js";
import {OrbitControls} from "three/addons/controls/OrbitControls.js";
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js';

class Main {
    constructor(container) {
        this.container = container;
        this.scene = this.initScene();
        this.renderer = this.initRenderer();
        this.camera = this.initCamera();
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
        this.loader = new GLTFLoader();
        this.handleResize();
        this.animate();
    }

    loadModels() {
        if (this.game) {
            this.game.players.forEach(player => {
                player.loadPlayerModel(this.loader, this.scene);
            });
            this.game.loadBoardModel(this.loader, this.scene);
            //this.game.addHelpers(this.scene);
        }
    }

    initScene() {
        const scene = new THREE.Scene();
        scene.background = new THREE.Color(0xa0a0a0);

        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        ambientLight.position.set(5, 10, 5);
        scene.add(ambientLight);

        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
        directionalLight.position.set(10, 10, 10);
        scene.add(directionalLight);

        return scene;
    }

    initRenderer() {
        const renderer = new THREE.WebGLRenderer({
            antialias: true,
            powerPreference: "high-performance"
        });
        renderer.setSize(this.container.offsetWidth, this.container.offsetHeight);
        renderer.setPixelRatio(window.devicePixelRatio);
        this.container.appendChild(renderer.domElement);
        return renderer;
    }

    initCamera() {
        const camera = new THREE.PerspectiveCamera(
            70,
            this.container.clientWidth / this.container.clientHeight,
            0.01,
            100
        );
        camera.position.set(0, 2, 10);
        return camera;
    }

    handleResize() {
        window.addEventListener("resize", () => {
            const width = this.container.clientWidth;
            const height = this.container.clientHeight;

            this.camera.aspect = width / height;
            this.camera.updateProjectionMatrix();

            this.renderer.setSize(width, height);
        });
    }

    animate() {
        this.renderer.setAnimationLoop(() => {
            this.controls.update();
            this.renderer.render(this.scene, this.camera);
        });
    }

    updateGameState(updatedGame) {
        const parsedGame = JSON.parse(updatedGame);

        if (!this.game) {
            this.game = new GameObject(
                parsedGame.gameId,
                parsedGame.gameState,
                parsedGame.players.map(playerData =>
                    new PlayerObject(
                        playerData.playerId,
                        playerData.color,
                        playerData.balance,
                        playerData.position,
                        playerData.inJail,
                        playerData.ownedProperties
                    )
                ),
                parsedGame.currentPlayerIndex
            );

            this.loadModels();
        } else {
            this.game.gameState = parsedGame.gameState;

            parsedGame.players.forEach((newPlayerData) => {
                let player = this.game.players.find(p => p.color === newPlayerData.color);
                if (player) {
                    const oldPosition = player.position;
                    const newPosition = newPlayerData.position;

                    if (oldPosition !== newPosition) {
                        const playerModel = this.scene.children.find(
                            obj => obj.userData?.playerId === player.playerId
                        );

                        if (playerModel) {
                            player.animatePlayerMovement(newPosition);
                        } else {
                            console.error(`model ${playerModel} not found`);
                        }
                    }
                }
            });
        }
    }

    cleanup() {
        this.renderer.clear();
        this.scene.clear();
        this.controls.dispose();
    }
}

window.init = function (container) {
    window.main = new Main(container);
};

window.updateGame = function (updatedGame) {
    if (window.main) {
        window.main.updateGameState(updatedGame);
    }
};

window.cleanup = function () {
    if (window.main) {
        window.main.cleanup();
    }
};