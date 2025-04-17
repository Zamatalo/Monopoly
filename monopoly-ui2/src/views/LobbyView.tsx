import { useEffect } from 'react';
import { useQuery } from '@apollo/client';
import { GET_FIND_BY_ID } from "../graphql/queries";
import { useGameActions, useGameStore } from '../stores/gameStore';

export const LobbyView = () => {
    const {
        game,
        isLoading,
        error,
        debugMode
    } = useGameStore();
    const {
        initializeWorld,
        loadGameState
    } = useGameActions();

    const { data } = useQuery(GET_FIND_BY_ID, {
        variables: { gameId: "50771991-f21e-4699-a872-bbad8df3811a" }
    });

    useEffect(() => {
        const container = document.getElementById('game-container') as HTMLDivElement;
        if (container && data?.findGameById) {
            initializeWorld(container);
            loadGameState(data.findGameById);
        }

        return () => {
            // Cleanup handled by store
        };
    }, [data, initializeWorld, loadGameState]);

    if (isLoading) return <div className="loading">Initializing game...</div>;
    if (error) return <div className="error">{error}</div>;

    return (
        <div className="lobby-view">
            <h1>Game Lobby</h1>

            <div id="game-container" className="game-canvas" />

            <div className="game-ui">
                {game && (
                    <>
                        <GameControls />
                        <PlayerList players={game.players} />
                    </>
                )}
            </div>

            <div className="debug-info">
                {debugMode && (
                    <pre>{JSON.stringify(game, null, 2)}</pre>
                )}
            </div>
        </div>
    );
};