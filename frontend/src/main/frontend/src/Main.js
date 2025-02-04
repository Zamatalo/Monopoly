import {GtlfLoader} from "./GtlfLoader.js";
import {Player} from "./Player.js";
import * as THREE from "three";
import {OrbitControls} from "three/addons/controls/OrbitControls.js";
import {Board} from "./Board.js";

class Main {
    constructor() {
        this.container = document.getElementById("GameBoardComponent");
        this.renderer = null;
        this.camera = null;
        this.scene = null;
        this.controls = null;
        this.directionalLight = null;
        this.players = [
            new Player(1, 'red'),
            new Player(2, 'green'),
            new Player(3, 'blue'),
            new Player(4, 'yellow'),
        ];
        this.board = null;
        this.loader = new GtlfLoader(this.players, this);

    }

    init() {
        console.log('Initializing game...');
        this.initRenderer();
        this.initCamera();
        this.initScene();
        this.initControls();
        this.handleResize();
        this.loader.init();

        this.board = new Board(this.scene, this.players);
        this.board.initializePositions();
        this.board.addHelpers();
        this.animate();


    }

    initRenderer() {
        this.renderer = new THREE.WebGLRenderer({
            antialias: true,
            powerPreference: "high-performance",
        });

        this.renderer.setSize(
            this.container.offsetWidth,
            this.container.offsetHeight
        );
        this.renderer.setPixelRatio(window.devicePixelRatio);
        this.container.appendChild(this.renderer.domElement);
    }

    initCamera() {
        this.camera = new THREE.PerspectiveCamera(
            70,
            this.container.clientWidth / this.container.clientHeight,
            0.01,
            100
        );
        this.camera.position.set(0, 2, 10);
    }

    initScene() {
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0xa0a0a0);

        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        this.scene.add(ambientLight);

        this.directionalLight = new THREE.DirectionalLight(0xffffff, 1);
        this.directionalLight.position.set(5, 10, 7.5);
        this.scene.add(this.directionalLight);
    }

    initControls() {
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    }

    handleResize() {
        window.addEventListener('resize', () => {
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
        var state = this.players.map(player => ({
            id: player.id,
            name: player.name,
            inJail: player.inJail,
            pos: player.pos,
            coords: {x: player.model.position.x, y: player.model.position.y, z: player.model.position.z},
            properties: player.properties
        }));
        console.log(`Saved state: ${JSON.stringify(state)} `);
        return state;
    }

    loadState(state) {
        state.forEach(savedPlayer => {
            const player = this.players.find(p => p.id === savedPlayer.id);
            if (player && player.model && savedPlayer.coords) {
                player.model.position.set(
                    savedPlayer.coords.x,
                    savedPlayer.coords.y,
                    savedPlayer.coords.z
                );
                player.pos = savedPlayer.pos;
            }
        });
    }
}


window.init = function () {
    const main = new Main();
    main.init();
    window.movePlayer = (playerId, button) => main.board.movePlayer(playerId, button);
    window.saveState = () => main.saveState();
    window.loadState = (state) => main.loadState(state);


};

