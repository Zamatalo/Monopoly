import {PlayerObject} from "./PlayerObject.js";
import * as THREE from "three";
import {GameObject} from "./GameObject.js";
import {OrbitControls} from "three/addons/controls/OrbitControls.js";
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js';
import {PlayerColor} from "./globalVars.js";

class Main {
    constructor(GameObject) {
        this.container = document.getElementById("GameBoardComponent");
        this.scene = this.initScene();
        this.renderer = this.initRenderer();
        this.camera = this.initCamera();
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
        this.loader = new GLTFLoader();
        this.handleResize();
        this.loadState(GameObject);
        this.loadModels()
        this.animate();
    }

    loadModels() {
        this.game.players.map(player => {
            player.loadPlayerModel(this.loader, this.scene)
        });

        this.game.loadBoardModel(this.loader, this.scene);
        this.game.addHelpers(this.scene);
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

        directionalLight.castShadow = true;
        // directionalLight.shadow.mapSize.width = 512;  // Default: 512
        // directionalLight.shadow.mapSize.height = 512; // Default: 512
        // directionalLight.shadow.camera.near = 0.5;    // Default: 0.5
        // directionalLight.shadow.camera.far = 500;     // Default: 500
        //
        //this.renderer.shadowMap.enabled = true;

        return scene;
    }

    initRenderer() {
        const renderer = new THREE.WebGLRenderer({
            antialias: true,
            powerPreference: "high-performance",
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

    saveState() {
        console.log(JSON.stringify(this.game));
        return this.game;
    }

    loadState(state) {
        console.log(JSON.stringify(this.game));
        const parsedGame = JSON.parse(state);

        this.game = new GameObject(
            parsedGame.gameId,
            parsedGame.status,
            parsedGame.players.map(playerData =>
                new PlayerObject(
                    playerData.playerId,
                    PlayerColor[playerData.name],
                    playerData.balance,
                    playerData.position,
                    playerData.inJail,
                    playerData.ownedProperties,
                    null
                )
            ),
        );
    }
}

window.init = function (GameObject) {
    const main = new Main(GameObject);

    window.movePlayer = (button, color) =>
        main.game.players.find(player => player.color === PlayerColor[color]).movePlayer(button);
    window.saveState = () => main.saveState();
};