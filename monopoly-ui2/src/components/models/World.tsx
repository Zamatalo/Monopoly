import * as THREE from 'three';
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls.js";
// @ts-ignore
import Stats from 'three/addons/libs/stats.module.js'


export class World {
    scene: THREE.Scene;
    renderer: THREE.WebGLRenderer;
    controls: OrbitControls;
    camera: THREE.PerspectiveCamera;
    stats: Stats;
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
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.5));
        this.renderer.setSize(container.clientWidth, container.clientHeight);
        this.renderer.shadowMap.enabled = true;
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        container.appendChild(this.renderer.domElement);


        this.stats = new Stats()
        document.body.appendChild(this.stats.dom)

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

    private setupLighting() {
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        this.scene.add(ambientLight);

        const color = 0xFFCC33;
        const dirLight = new THREE.DirectionalLight(color, 1.2);
        dirLight.position.set(10, 10, -26);
        dirLight.castShadow = true;

        dirLight.shadow.mapSize.width = 2048;
        dirLight.shadow.mapSize.height = 2048;
        dirLight.shadow.bias = -0.001;
        const d = 50;
        dirLight.shadow.camera.left = -d;
        dirLight.shadow.camera.right = d;
        dirLight.shadow.camera.top = d;
        dirLight.shadow.camera.bottom = -d;
        dirLight.shadow.camera.near = 1;
        dirLight.shadow.camera.far = 200;

        this.scene.add(dirLight);
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

    animate = () => {
        this.controls.update();
        this.renderer.render(this.scene, this.camera);
        this.stats.update()
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