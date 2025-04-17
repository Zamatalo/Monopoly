import {useEffect, useRef, useState} from 'react';
import {useGameStore} from '../stores/gameStore';
import {World} from '../components/models/World';

export const GameView = () => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const {
        game,
        world,
        setWorld,
        loadBoardModel,
        loadDiceModel,
        loadPlayerModels
    } = useGameStore();

    const [sceneLoaded, setSceneLoaded] = useState(false);

    useEffect(() => {
        const container = containerRef.current;
        if (!container) return;

        if (!world) {
            const newWorld = new World(container);
            setWorld(newWorld);
        }
    }, []);

    useEffect(() => {
        const initializeScene = async () => {
            if (!game || !world) {
                console.warn('No selected game or world, can’t load scene yet.');
                return;
            }

            try {
                loadBoardModel();
                loadDiceModel();
                loadPlayerModels();
                setSceneLoaded(true);
            } catch (err) {
                console.error('Error loading scene:', err);
            }
        };

        if (game && world && !sceneLoaded) {
            initializeScene();
        }
    }, [game, world]);

    return (
        <div className="game-view">
            <div ref={containerRef} className="webgl-container" style={{ width: '100%', height: '100vh' }} />
        </div>
    );
};
