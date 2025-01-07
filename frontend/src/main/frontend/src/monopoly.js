import * as THREE from "three";
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js';
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js';

class ThreeTest {
    init(element) {
        this.element = element;
        console.log(this.element);
        this.renderer = new THREE.WebGLRenderer({
            antialias: true,
            canvas: element,
            powerPreference: "high-performance"
        });
        this.renderer.setSize(window.innerWidth, window.innerHeight);

        this.camera = new THREE.PerspectiveCamera(
            70,
            window.innerWidth / window.innerHeight,
            0.01,
            100
        );

        this.scene = new THREE.Scene();
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        this.scene.add(ambientLight);
        this.scene.background = new THREE.Color(0xa0a0a0);
        this.directionalLight = new THREE.DirectionalLight(0xffffff, 1);
        this.directionalLight.position.set(5, 10, 7.5);
        this.scene.add(this.directionalLight);
        this.camera.position.set(0, 2, 10);
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);

        this.createBoard()
        this.loadGLTFModel();

        window.addEventListener('resize', () => {
            this.camera.aspect = window.innerWidth / window.innerHeight;
            this.camera.updateProjectionMatrix();
            this.renderer.setSize(window.innerWidth, window.innerHeight);
            this.renderer.setPixelRatio(window.devicePixelRatio);
        });
    }

    loadGLTFModel() {
        const loader = new GLTFLoader();
        loader.load(
            '/models/my_monopoly/monopolyBoard.glb',
            (gltf) => {
                const model = gltf.scene;
                //console.log("Model structure:", model);
                model.position.set(0, 0, 0);
                model.scale.set(0.57, 0.57, 0.57);


                this.scene.add(model);
            },
            undefined,
            (error) => {
                console.error('Model loading error:', error);
            }
        );
    }

    render() {
        this.renderer.render(this.scene, this.camera);
    }

    animate() {
        requestAnimationFrame(this.animate.bind(this));
        this.controls.update();
        this.render();
    }

    createBoard() {
        const boardSize = 11;
        const squareSize = 1;
        const squareMaterial = new THREE.MeshBasicMaterial({
            color: 0x00ff00,
            wireframe: true,
        });

        for (let row = 0; row < boardSize; row++) {
            for (let col = 0; col < boardSize; col++) {
                if (row === 0 || row === boardSize - 1 || col === 0 || col === boardSize - 1) {
                    const geometry = new THREE.BoxGeometry(squareSize, 0.1, squareSize);
                    const square = new THREE.Mesh(geometry, squareMaterial);
                    const x = (col - boardSize / 2) * squareSize;
                    const z = (boardSize / 2 - row) * squareSize;
                    square.position.set(x + 0.5, squareSize / 10, z - 0.5);
                    this.scene.add(square);
                }
            }
        }
    }

}

window.initThree = function (element) {
    const tt = new ThreeTest();
    tt.init(element);
    tt.animate();
};
