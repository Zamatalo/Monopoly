export {};
declare global {
    interface Window {
        loadBoard: () => Promise<void>;
        loadPlayers: () => void;
        roll: () => number;
    }
}
