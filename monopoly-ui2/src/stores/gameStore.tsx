// stores/gameStore.ts
import { create } from 'zustand';
import { GameDTO } from "Frontend/components/objects/GameDTO";
import { PlayerDTO } from "Frontend/components/objects/PlayerDTO";
import {initThreeJS} from "../components/Main";

interface GameState {
    currentGame: GameDTO | null;
    currentPlayer: PlayerDTO | null;
    actions: {
        initGame: GameDTO;
    };
}

export const useGameStore = create<GameState>((set) => ({
    gameStarted: false,
    currentGameId: null,
    playerId: null,
    currentGame: null,
    currentPlayer: null,
    actions: {
        initGame(): ({ gameId, gameState }) => {
            initThreeJS()
        };
    },
}));

// Selector hooks for better performance
export const useGameStarted = () => useGameStore((state) => state.gameStarted);
export const useCurrentGameId = () => useGameStore((state) => state.currentGameId);
export const usePlayerId = () => useGameStore((state) => state.playerId);
export const useCurrentGameState = () => useGameStore((state) => state.currentGame);
export const useCurrentPlayer = () => useGameStore((state) => state.currentPlayer);
export const useGameActions = () => useGameStore((state) => state.actions);