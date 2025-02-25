import {PlayerObject} from "./PlayerObject.js";
import * as THREE from "three";
import {GameObject} from "./GameObject.js";
import {OrbitControls} from "three/addons/controls/OrbitControls.js";
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js';
import {PlayerColor} from "./globalVars.js";

class Main {
    constructor() {
        this.container = document.getElementById("GameBoardComponent");
        this.scene = this.initScene();
        this.renderer = this.initRenderer();
        this.camera = this.initCamera();
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
        this.loader = new GLTFLoader();
        this.handleResize();
        this.animate();
    }

    createNewGame() {
        this.players = [
            new PlayerObject(1, PlayerColor.PLAYER_RED, 1500, 0, false, null, null),
            new PlayerObject(2, PlayerColor.PLAYER_GREEN, 1500, 0, false, null, null),
            new PlayerObject(3, PlayerColor.PLAYER_BLUE, 1500, 0, false, null, null),
            new PlayerObject(4, PlayerColor.PLAYER_YELLOW, 1500, 0, false, null, null),
        ];
        this.game = new GameObject(1, "STARTED", this.players, 0);
    }

    async initGame() {
        if (!this.game) {
            console.warn("Game not found. Creating a new one...");
            this.createNewGame();
        }

        await Promise.all(
            this.game.players.map(player => {
                player.loadPlayerModel(this.loader, this.scene)
            })
        );

        await Promise.all([
            this.game.loadBoardModel(this.loader, this.scene),

        ]);
        this.game.addHelpers(this.scene);
    }

    initScene() {
        const scene = new THREE.Scene();
        scene.background = new THREE.Color(0xa0a0a0);

        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        scene.add(ambientLight);

        const directionalLight = new THREE.DirectionalLight(0xffffff, 1);
        directionalLight.position.set(5, 10, 7.5);
        scene.add(directionalLight);

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

window.init = function () {
    const main = new Main();
    window.movePlayer = (button, color) =>
        main.game.players.find(player => player.color === PlayerColor[color]).movePlayer(button);
    window.saveState = () => main.saveState();
    window.loadState = (state) => {
        main.scene = main.initScene(); //clear scene
        main.loadState(state);
        main.initGame().then(r => console.log("Game loaded"));
    }
    window.createNewGame = () => {
        main.scene = main.initScene();
        main.createNewGame();
        main.initGame().then(r => "");
    }

};