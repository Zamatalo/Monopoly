export class GameObject {
    constructor(gameId, gameState, players, currentPlayerIndex, model) {
        this.gameId = gameId;
        this.gameState = gameState;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.model = model;
    }

    loadBoardModel(loader, scene) {
            const boardPath = `/models/my_monopoly/monopolyBoard.glb`;
            loader.load(
                boardPath,
                gltf => {
                    this.model = gltf.scene;
                    console.log('Board model loaded');
                    this.model.position.set(0, 0, 0);
                    this.model.receiveShadow = true;
                    this.model.userData.isBoard = true;
                    scene.add(this.model);
                },
                undefined,
                (error) => (`Error loading model : ${error}`)
            );
    };

    // addHelpers(scene) {
    //     const geometry = new THREE.SphereGeometry(0.1, 16, 16);
    //     const material = new THREE.MeshBasicMaterial({color: 0x00ff00});
    //     positions.forEach((pos) => {
    //         const helper = new THREE.Mesh(geometry, material);
    //         helper.position.set(pos.x, 0.1, pos.z);
    //         scene.add(helper);
    //     });
    // };

    toJSON() {
        const {model, ...rest} = this;
        return rest;
    }

}