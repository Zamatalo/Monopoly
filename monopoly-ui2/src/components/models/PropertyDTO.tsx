import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader";
import WorldSingleton from "../../stores/singletons/WorldSingleton";
import {positions} from "../utils/constants";

export class PropertyDTO {
    id: string;
    displayName: string;
    boardPosition: number;
    cost: number;
    upgradable: boolean;


    constructor(data: PropertyDTO) {
        this.id = data.id;
        this.displayName = data.displayName;
        this.boardPosition = data.boardPosition;
        this.cost = data.cost;
        this.upgradable = data.upgradable;
    }

    static fromRaw(raw: any): PropertyDTO {
        return new PropertyDTO(raw);
    }

    public async loadBuildingModel(loader:GLTFLoader): Promise<void> {
        if (!WorldSingleton.hasInstance()) {
            return;
        }
        const world = WorldSingleton.getInstance();
        const posi = {...positions[this.boardPosition]};
        const buildingPath = '/assets/models/building.glb';

        return new Promise((resolve, reject) => {
            loader.load(
                buildingPath,
                (gltf) => {
                    const model = gltf.scene;
                    model.scale.set(1.5, 1.5, 1.5);

                    model.traverse((obj: any) => {
                        if (obj.castShadow !== undefined) {
                            obj.castShadow = true;
                            obj.receiveShadow = true;
                        }
                    });

                    model.userData = {
                        isBuilding: true,
                        buildingName: this.displayName
                    };

                    if (posi.x === 9.5) {
                        posi.x -= 2.2;
                    } else if (posi.x === -9.5) {
                        model.rotateY(4.72);
                        posi.x += 2.2;
                    } else if (posi.z === 9.5) {
                        posi.z -= 2.2;
                    } else if (posi.z === -9.5) {
                        posi.z += 2.2;
                    }


                    model.position.set(posi.x, 0.14, posi.z);
                    world.scene.add(model);
                    console.log(`Building Model loaded`);
                    resolve();
                },
                undefined,
                (error) => {
                    console.error(`Error loading model`, error);
                    reject(error);
                }
            );
        });
    }
}