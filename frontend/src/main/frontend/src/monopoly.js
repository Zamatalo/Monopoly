// const scene = new THREE.Scene();
// const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
// const renderer = new THREE.WebGLRenderer();
// renderer.setSize(window.innerWidth, window.innerHeight);
// document.body.appendChild(renderer.domElement);
//
// // Add a light to the scene
// const light = new THREE.AmbientLight(0xffffff, 1);
// scene.add(light);
//
// // Define the square properties
// const boardSize = 10;
// const squareSize = 1;
// const boardSquares = 100; // Monopoly has 40 squares
//
// // Create material for the squares
// const squareMaterial = new THREE.MeshBasicMaterial({ color: 0x00ff00, wireframe: true });
//
// for (let i = 0; i < boardSquares; i++) {
//     const geometry = new THREE.BoxGeometry(squareSize, 0.1, squareSize);
//     const square = new THREE.Mesh(geometry, squareMaterial);
//
//     let x = 0, z = 0;
//
//     // Determine row and column
//     const row = Math.floor(i / boardSize);
//     const col = i % boardSize;
//
//     // Create border squares
//     if (row === 0 || row === boardSize - 1 || col === 0 || col === boardSize - 1) {
//         x = (col - boardSize / 2) * squareSize;
//         z = (boardSize / 2 - row) * squareSize;
//         square.position.set(x, 0, z);
//         scene.add(square);
//     }
//
// }
//
// // Position the camera
// camera.position.z = 15;
//
// // Add OrbitControls to allow for mouse interaction
// const controls = new OrbitControls(camera, renderer.domElement);
//
// // Create an animate function to render the scene
// function animate() {
//     requestAnimationFrame(animate);
//     controls.update(); // Update controls in each frame
//     renderer.render(scene, camera);
// }
//
// animate();

import * as THREE from "three";
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

const boardSize = 10;
const squareSize = 1;
const boardSquares = 100; // Monopoly has 40 squares

class ThreeTest {
    init(element) {
        this.element = element;
        this.camera = new THREE.PerspectiveCamera(
            70,
            window.innerWidth / window.innerHeight,
            0.01,
            10
        );
        this.camera.position.z = 15;

        this.scene = new THREE.Scene();

        for (let i = 0; i < boardSquares; i++) {
            this.geometry = new THREE.BoxGeometry(squareSize, 0.1, squareSize);
            const square = new THREE.Mesh(geometry, squareMaterial);

            let x = 0, z = 0;

            // Determine row and column
            this.row = Math.floor(i / boardSize);
            this.col = i % boardSize;

            // Create border squares
            if (row === 0 || row === boardSize - 1 || col === 0 || col === boardSize - 1) {
                x = (col - boardSize / 2) * squareSize;
                z = (boardSize / 2 - row) * squareSize;
                square.position.set(x, 0, z);
                scene.add(square);
            }
        }
        this.material = new THREE.MeshNormalMaterial();

        this.mesh = new THREE.Mesh(this.geometry, this.material);
        this.scene.add(this.mesh);

        this.renderer = new THREE.WebGLRenderer({
            antialias: true,
            canvas: element
        });

        // Use a random spinning direction
        this.xIncrement = (Math.random() - 0.5) / 10;
        this.yIncrement = (Math.random() - 0.5) / 10;
    }
    render() {
        this.renderer.render(this.scene, this.camera);
    }
    animate() {
        requestAnimationFrame(this.animate.bind(this));

        this.render();
    }
}

window.initThree = function(element) {
    // Called from Java with the DOM element for the Three component instance
    const tt = new ThreeTest();
    tt.init(element);
    tt.animate();
};