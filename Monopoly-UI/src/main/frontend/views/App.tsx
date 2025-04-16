import React, {useEffect, useRef, useState, useCallback} from 'react';
import {ApolloClient, ApolloProvider, InMemoryCache, useQuery, useSubscription} from '@apollo/client';
import {GraphQLWsLink} from '@apollo/client/link/subscriptions';
import {createClient} from 'graphql-ws';
import {initThreeJS, loadState} from 'Frontend/components/Main';
import {GAME_UPDATED_SUBSCRIPTION, GET_FIND_BY_ID} from 'Frontend/utils/queries';
import {GameDTO} from 'Frontend/components/objects/GameDTO';
import {PlayerDTO} from 'Frontend/components/objects/PlayerDTO';
import GameLobby from "Frontend/views/lobby";
import GameInterface from "Frontend/components/GameInterface";

const wsLink = new GraphQLWsLink(
    createClient({
        url: 'ws://localhost:8081/api/v1/graphql',
        connectionParams: {},
    })
);

const client = new ApolloClient({
    link: wsLink,
    cache: new InMemoryCache(),
    defaultOptions: {
        watchQuery: {
            fetchPolicy: 'cache-and-network',
        },
    },
});

interface GameState {
    gameStarted: boolean;
    currentGameId: string | null;
    playerId: string | null;
    currentGameState: GameDTO | null;
    currentPlayer: PlayerDTO | null;
}

function App() {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const [gameState, setGameState] = useState<GameState>({
        gameStarted: false,
        currentGameId: null,
        playerId: null,
        currentGameState: null,
        currentPlayer: null
    });

    const updateGameState = useCallback((newState: Partial<GameState>) => {
        setGameState(prev => ({...prev, ...newState}));
    }, []);

    useEffect(() => {
        const storedPlayerId = localStorage.getItem('playerId');
        if (storedPlayerId) {
            updateGameState({playerId: storedPlayerId});
        } else {
            const newPlayerId = crypto.randomUUID();
            localStorage.setItem('playerId', newPlayerId);
            updateGameState({playerId: newPlayerId});
        }
    }, [updateGameState]);

    useEffect(() => {
        if (gameState.gameStarted && containerRef.current) {
            initThreeJS(containerRef.current);

        }
    }, [gameState.gameStarted]);

    const handleGameStart = useCallback((gameId: string) => {
        updateGameState({
            currentGameId: gameId,
            gameStarted: true
        });
    }, [updateGameState]);

    const handleGameAction = useCallback((action: 'rollDice' | 'endTurn' | 'buyProperty') => {
        console.log(`Handling game action: ${action}`);
        // TODO: Implement actual game actions
    }, []);

    if (!gameState.gameStarted) {
        return (
            <ApolloProvider client={client}>
                <GameLobby
                    onGameStart={handleGameStart}
                    playerId={gameState.playerId}
                />
            </ApolloProvider>
        );
    }

    return (
        <ApolloProvider client={client}>
            <GameInitializer
                gameId={gameState.currentGameId!}
                onStateUpdate={(game, player) => updateGameState({
                    currentGameState: game,
                    currentPlayer: player
                })}
            />
            <GameUpdates
                gameId={gameState.currentGameId!}
                onStateUpdate={(game, player) => updateGameState({
                    currentGameState: game,
                    currentPlayer: player
                })}
            />
            <div ref={containerRef} style={{width: '100%', height: '100%'}}/>
            {gameState.currentGameState && gameState.currentPlayer && (
                <GameInterface
                    currentGame={gameState.currentGameState}
                    currentPlayer={gameState.currentPlayer}
                    onRollDice={() => handleGameAction('rollDice')}
                    onEndTurn={() => handleGameAction('endTurn')}
                    onBuyProperty={() => handleGameAction('buyProperty')}
                />
            )}
        </ApolloProvider>
    );
}

interface GameComponentProps {
    gameId: string;
    onStateUpdate: (game: GameDTO, player: PlayerDTO) => void;
}

function GameInitializer({gameId, onStateUpdate}: GameComponentProps) {
    const {data, loading, error} = useQuery(GET_FIND_BY_ID, {
        variables: {gameId},
        fetchPolicy: 'network-only',
        notifyOnNetworkStatusChange: false,
    });
    const [initialized, setInitialized] = useState(false);

    useEffect(() => {
        if (data && !initialized) {
            console.log('Fetched initial game state:', data);
            setInitialized(true);

            const gameData = data.findGameById;
            const players = gameData.players.map(
                (player: any) => new PlayerDTO({
                    playerId: player.playerId,
                    color: player.color,
                    inJail: player.inJail,
                    balance: player.balance,
                    position: player.position,
                    ownedProperties: player.ownedProperties,
                } as PlayerDTO)
            );

            const game = new GameDTO({
                gameId: gameData.gameId,
                gameState: gameData.gameState,
                players,
                currentPlayerIndex: gameData.currentPlayerIndex,
                createdTime: gameData.createdTime
            } as GameDTO);

            onStateUpdate(game, game.players[game.currentPlayerIndex]);
            loadState(game).then(() => console.log('Initial game state loaded'));
        }
    }, [data, initialized, onStateUpdate]);

    if (loading && !initialized) return <p>Loading initial game state...</p>;
    if (error) return <p>Error fetching initial game state: {error.message}</p>;

    return null;
}

function GameUpdates({gameId, onStateUpdate}: GameComponentProps) {
    const {data, error} = useSubscription(GAME_UPDATED_SUBSCRIPTION, {
        variables: {gameId},
    });

    useEffect(() => {
        if (data) {
            const gameData = data.gameUpdated;
            const players = gameData.players.map(
                (player: any) => new PlayerDTO({
                    playerId: player.playerId,
                    color: player.color,
                    inJail: player.inJail,
                    balance: player.balance,
                    position: player.position,
                    ownedProperties: player.ownedProperties,
                } as PlayerDTO)
            );

            const newGame = new GameDTO({
                gameId: gameData.gameId,
                gameState: gameData.gameState,
                players,
                currentPlayerIndex: gameData.currentPlayerIndex,
                createdTime: gameData.createdTime
            } as GameDTO);

            onStateUpdate(newGame, newGame.players[newGame.currentPlayerIndex]);
            loadState(newGame).then(() => console.log('Game state updated'));
        }
    }, [data, onStateUpdate]);

    if (error) return <p>Error: {error.message}</p>;

    return null;
}

export default App;