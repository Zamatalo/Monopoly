import {create} from 'zustand';
import {World} from "../components/models/World";
import {GameDTO} from "../components/models/GameDTO";
import {Dice} from "../components/models/Dice";
import {PlayerDTO} from "../components/models/PlayerDTO";


interface GameStoreState {
    game: GameDTO | null;
    world: World | null;
    currentPlayer: PlayerDTO | null;
    dice: Dice | null;
    loading: boolean;
    error: string | null;

    setCurrentPlayer: (currentPlayer: PlayerDTO) => void;
    setGame: (game: GameDTO) => void;
    setWorld: (world: World) => void;
    loadBoardModel: () => void
    loadDiceModel: () => void;
    loadPlayerModels: () => void;
    rollDice: () => number;
}

export const useGameStore = create<GameStoreState>((set, get) => ({
    game: null,
    world: null,
    currentPlayer: null,
    dice: null,
    loading: false,
    error: null,

    setCurrentPlayer: (player) => set({currentPlayer: player}),
    setGame: (game) => set({game}),
    setWorld: (world) => set({world}),

    loadBoardModel: async () => {
        const {game, world} = get();
        if (!game || !world) return;

        set({loading: true});
        try {
            await game.loadBoardModel();
        } catch (err) {
            set({error: 'Failed to load board model.'});
        } finally {
            set({loading: false});
        }
    },

    loadDiceModel: () => {
        const {world} = get();
        if (!world) return;

        set({loading: true});
        try {
            const dice = new Dice();
            dice.loadDice(world);

            set({dice});
        } catch (err) {
            set({error: 'Failed to load dice model.'});
        } finally {
            set({loading: false});
        }
    },

    loadPlayerModels: () => {
        const {game, world} = get();
        if (!game || !world) return;

        set({loading: true});
        try {
            for (const player of game.players) {
                player.loadPlayerModel(world);
            }
        } catch (err) {
            set({error: 'Failed to load player models.'});
        } finally {
            set({loading: false});
        }
    },

    rollDice: () => {
        const {dice} = get();
        if (!dice || !dice.model) return -1;

        return dice.getDiceTopFace();
    },

}));
