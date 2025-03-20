import * as RAPIER from '@dimforge/rapier3d';
import * as THREE from 'three';


export class World {
    world: RAPIER.World;
    bodies: RAPIER.RigidBody[] = [];
    meshes: THREE.Object3D[] = [];
    scene: THREE.Scene;
    private r: any;

    constructor() {
        this.world = new RAPIER.World({x: 0, y: -9.81, z: 0});
        this.scene = new THREE.Scene();
    }

    addBody(mesh: THREE.Object3D, body: RAPIER.RigidBody) {
        this.bodies.push(body);
        this.meshes.push(mesh);
    }

    addToScene(object: THREE.Object3D) {
        this.scene.add(object);
    }

    update(deltaTime: number) {
        this.world.step();

        for (let i = 0; i < this.bodies.length; i++) {
            const body = this.bodies[i];
            const mesh = this.meshes[i];

            if (body && mesh) {
                const position = body.translation();
                const rotation = body.rotation();
                if (this.r) {
                    this.r.update();
                }
                mesh.position.set(position.x, position.y, position.z);
                mesh.quaternion.set(rotation.x, rotation.y, rotation.z, rotation.w);
            }
        }
    }


}