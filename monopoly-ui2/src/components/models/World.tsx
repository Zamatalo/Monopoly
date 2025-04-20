import * as THREE from 'three';
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls.js";
import {Dice} from "./Dice";
import GUI from "three/examples/jsm/libs/lil-gui.module.min";

export class World {
    scene: THREE.Scene;
    renderer: THREE.WebGLRenderer;
    controls: OrbitControls;
    camera: THREE.PerspectiveCamera;

    constructor(container: HTMLElement) {
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.CubeTextureLoader()
            .setPath('/assets/skybox/')
            .load([
                'cubemap_0.png', // Right
                'cubemap_1.png', // Left
                'cubemap_2.png', // Top
                'cubemap_3.png', // Bottom
                'cubemap_4.png', // Back
                'cubemap_5.png', // Front
            ]);
        this.camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
        this.camera.position.z = 10;
        this.camera.position.y = 15;
        this.camera.lookAt(new THREE.Vector3(0, 0, 0));

        this.renderer = new THREE.WebGLRenderer({
            antialias: true,
            powerPreference: "high-performance"
        });
        this.renderer.setSize(container.clientWidth, container.clientHeight);
        this.renderer.shadowMap.enabled = true;
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        container.appendChild(this.renderer.domElement);


        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
        this.controls.enableDamping = true
        this.controls.target.y = 1

        window.addEventListener('resize', () => {
            const width = container.clientWidth;
            const height = container.clientHeight;

            this.camera.aspect = width / height;
            this.camera.updateProjectionMatrix();
            this.renderer.setSize(width, height);
        });
        this.setupLighting()
        this.renderer.setAnimationLoop(this.animate);
    }

    addToScene(object: THREE.Object3D) {
        this.scene.add(object);
    }

    reattachCanvas(container: HTMLElement) {
        if (this.renderer.domElement.parentElement !== container) {
            container.appendChild(this.renderer.domElement);
            this.renderer.setSize(container.clientWidth, container.clientHeight);
            this.camera.aspect = container.clientWidth / container.clientHeight;
            this.camera.updateProjectionMatrix();
        }
    }

    private setupLighting() {
        let ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        this.scene.add(ambientLight);

        const color = 0xFFCC33;
        const intensity2 = 75000;
        const light2 = new THREE.PointLight(color, intensity2);
        light2.castShadow = true;
        light2.position.set(100, 100, -260);
        light2.shadow.bias = -0.001;
        light2.shadow.mapSize.width = 4096;
        light2.shadow.mapSize.height = 4096;
        this.scene.add(light2);
    }

    private setupGui(dice: Dice) {
        const gui = new GUI();

        let diceState = {
            position: {x: 0, y: 0, z: 0},
            rotation: {x: 0, y: 0, z: 0, w: 1},
            isSleeping: false,
            topFace: 1,
            isMoving: () => {
                if (!dice.model) return false;
                return false;
            },
            resetDice: () => {
                dice.model.position.set(0, 0, 0);
                dice.model.rotation.set(0, 0, 0);
            }
        };

        const diceFolder = gui.addFolder('Dice State');
        diceFolder.add(diceState.position, 'x').name('Pos X').listen();
        diceFolder.add(diceState.position, 'y').name('Pos Y').listen();
        diceFolder.add(diceState.position, 'z').name('Pos Z').listen();
        diceFolder.add(diceState, 'isSleeping').name('Is Sleeping').listen();
        diceFolder.add(diceState, 'topFace').name('Top Face').listen();
        diceFolder.add(diceState, 'resetDice').name('Reset Dice');

        let stats = new Stats();
        document.body.appendChild(stats.dom);
    }




    animate = () => {
        this.controls.update();
        this.renderer.render(this.scene, this.camera);
    };

    dispose() {
        while (this.scene.children.length > 0) {
            const child = this.scene.children[0];
            this.scene.remove(child);
            if ((child as any).dispose) {
                (child as any).dispose();
            }
        }
        this.renderer.dispose();
        this.controls.dispose();
    }
}