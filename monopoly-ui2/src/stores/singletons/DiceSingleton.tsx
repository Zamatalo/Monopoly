// import {PlayerDTO} from "../../components/models/PlayerDTO";
// import {GameDTO} from "../../components/models/GameDTO";
// import {Dice} from "../../components/models/Dice";
//
// class DiceSingletonInstance {
//     private static instance: PlayerDTO | null = null;
//
//     static initialize(rawPlayerData: any): Dice | null {
//         if (!this.instance) {
//             this.instance = Dice.fromRaw(rawPlayerData);
//         }
//         return this.instance;
//     }
//
//     static getInstance(): PlayerDTO | null {
//         if (!this.instance) {
//             throw new Error("Player not initialized. Call initialize() first.");
//         }
//         return this.instance;
//     }
//
//     static reset(): void {
//         this.instance = null;
//     }
//     static update(rawPlayerData: any): PlayerDTO {
//         if (!this.instance) {
//             throw new Error("Player not initialized. Call initialize() first.");
//         }
//         this.instance.updateFromRaw(rawPlayerData);
//         return this.instance;
//     }
// }
//
// export default DiceSingletonInstance;
