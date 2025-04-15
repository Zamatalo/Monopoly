import React, {useEffect, useState} from 'react';
import {useApolloClient, useMutation, useQuery} from '@apollo/client';
import {
    CREATE_GAME_MUTATION,
    GET_ACTIVE_GAMES,
    GET_GAME_BY_PLAYER_ID,
    JOIN_GAME_MUTATION
} from 'Frontend/utils/queries';
import {PlayerDTO} from "Frontend/components/objects/PlayerDTO";
import {Button} from "@vaadin/react-components/Button.js";
import {Icon} from "@vaadin/react-components/Icon.js";
import "@vaadin/icons";
import "../themes/my-theme/lobby.css";
import {GameState, PlayerColor} from "Frontend/utils/constants";
import ColorPickerDialog from '../components/ColorPickerDialog';

interface GameLobbyProps {
    onGameStart: (gameId: string) => void;
    playerId?: string | null;
    onReconnect: (gameId: string) => void;
}

interface GameInfo {
    gameId: string;
    players: PlayerDTO[];
    gameState: string;
    createdTime: string;
    currentPlayerIndex: number;
}

function GameLobby({onGameStart, playerId}: GameLobbyProps) {
    const [playerName, setPlayerName] = useState('');
    const [selectedGameId, setSelectedGameId] = useState<string | null>(null);
    const [games, setGames] = useState<GameInfo[]>([]);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const containerRef = React.useRef<HTMLDivElement | null>(null);
    const [colorDialogOpen, setColorDialogOpen] = useState(false);
    const [pendingGameId, setPendingGameId] = useState<string | null>(null);
    const [checkReconnectLoading, setCheckReconnectLoading] = useState(false);
    const [reconnectAvailable, setReconnectAvailable] = useState(false);
    const [reconnectLoading, setReconnectLoading] = useState(false);
    const client = useApolloClient();
    const {loading: gamesLoading, data, error} = useQuery(GET_ACTIVE_GAMES, {
        pollInterval: 5000
    });

    const [createGame] = useMutation(CREATE_GAME_MUTATION);
    const [joinGame, {loading: joiningGame}] = useMutation(JOIN_GAME_MUTATION);

    useEffect(() => {
        if (data?.getActiveGames) {
            setGames(data.getActiveGames);
        }
    }, [data]);

    useEffect(() => {
        if (error) {
            setErrorMessage(`Error loading games: ${error.message}`);
        }
    }, [error]);

    const checkForExistingGame = async () => {
        if (!playerId) return;

        setCheckReconnectLoading(true);
        setReconnectAvailable(false);
        setErrorMessage(null);

        try {
            const {data} = await client.query({
                query: GET_GAME_BY_PLAYER_ID,
                variables: {playerId},
                fetchPolicy: 'network-only'
            });

            if (data?.findGameByPlayerId) {
                setReconnectAvailable(true);
            } else {
                setErrorMessage("No active game found to reconnect to");
            }
        } catch (error: any) {
            setErrorMessage(`Error checking for existing game: ${error.message}`);
        } finally {
            setCheckReconnectLoading(false);
        }
    };

    const handleReconnect = async () => {
        if (!playerId) return;

        setReconnectLoading(true);
        setErrorMessage(null);

        try {
            const {data} = await client.query({
                query: GET_GAME_BY_PLAYER_ID,
                variables: {playerId},
                fetchPolicy: 'network-only'
            });

            if (data?.findGameByPlayerId) {
                onGameStart(data.findGameByPlayerId.gameId);
            } else {
                setErrorMessage("No active game found to reconnect to");
                setReconnectAvailable(false);
            }
        } catch (error: any) {
            setErrorMessage(`Error reconnecting: ${error.message}`);
            setReconnectAvailable(false);
        } finally {
            setReconnectLoading(false);
        }
    };

    const handleCreateGame = async () => {
        if (playerName.trim()) {
            setErrorMessage(null);
            try {
                const {data} = await createGame();
                if (data) {
                    const gameId = data.createNewGame.gameId;
                    await confirmJoinWithColor(gameId);
                }
            } catch (error: any) {
                setErrorMessage(`Error creating game: ${error.message}`);
            }
        } else {
            setErrorMessage('Please enter your name');
        }
    };

    const confirmJoinWithColor = async (color: PlayerColor) => {
        if (!pendingGameId || !playerName.trim()) return;
        setErrorMessage(null);
        try {
            const {data} = await joinGame({
                variables: {gameId: pendingGameId, playerName, playerColor: color, playerId: playerId},
            });
            if (data) {
                onGameStart(data.joinToGame.gameId);
            }
        } catch (error: any) {
            setErrorMessage(`Error joining game: ${error.message}`);
        } finally {
            setColorDialogOpen(false);
            setPendingGameId(null);
        }
    };

    return (
        <div className="lobby-container">
            <div className="lobby-header">
                <h1>Game Lobby</h1>
                <p>Select a game to join or create your own</p>
            </div>

            <div className="player-input">
                <input
                    type="text"
                    placeholder="Enter your name"
                    value={playerName}
                    onChange={(e) => setPlayerName(e.target.value)}
                    className="name-input"
                    required
                />
            </div>

            {errorMessage && <div className="error-message">{errorMessage}</div>}

            <div className="game-list-container">
                {gamesLoading ? (
                    <div className="loading-spinner">
                        <Icon icon="vaadin:spinner"/>
                        <span>Loading games...</span>
                    </div>
                ) : (
                    <>
                        <div className="game-list-header">
                            <h2>Available Games</h2>
                            <Button
                                theme="primary"
                                onClick={handleCreateGame}
                                className="create-game-btn"
                                disabled={!playerName.trim()}
                            >
                                <Icon icon="vaadin:plus" slot="prefix"/>
                                Create New Game
                            </Button>
                            {!reconnectAvailable && (
                                <Button
                                    theme="secondary"
                                    onClick={checkForExistingGame}
                                    className="check-reconnect-btn"
                                    disabled={checkReconnectLoading || !playerId}
                                >
                                    {checkReconnectLoading ? (
                                        <>
                                            <Icon icon="vaadin:spinner"/>
                                            Checking...
                                        </>
                                    ) : (
                                        <>
                                            <Icon icon="vaadin:circle-thin" slot="prefix"/>
                                            Check for Existing Game
                                        </>
                                    )}
                                </Button>
                            )}
                            {reconnectAvailable && (
                                <Button
                                    theme="primary"
                                    onClick={handleReconnect}
                                    className="reconnect-btn"
                                    disabled={reconnectLoading}
                                >
                                    {reconnectLoading ? (
                                        <>
                                            <Icon icon="vaadin:spinner"/>
                                            Reconnecting...
                                        </>
                                    ) : (
                                        <>
                                            <Icon icon="vaadin:refresh"/>
                                            Reconnect to Game
                                        </>
                                    )}
                                </Button>
                            )}
                        </div>

                        {games.length === 0 ? (
                            <div className="no-games">
                                <Icon icon="vaadin:gamepad" size={48}/>
                                <p>No games available</p>
                                <Button
                                    theme="primary"
                                    onClick={handleCreateGame}
                                    disabled={!playerName.trim()}
                                >
                                    Create First Game
                                </Button>
                            </div>
                        ) : (
                            <div className="game-list">
                                {games.map((game) => (
                                    <div
                                        key={game.gameId}
                                        className={`game-card ${selectedGameId === game.gameId ? "selected" : ""}`}
                                        onClick={() => setSelectedGameId(game.gameId)}
                                    >
                                        <div className="game-info">
                                            <h3 className={`status`}>{game.gameState === GameState.STARTED ? GameState.FINISHED : GameState.IN_PROGRESS}</h3>
                                            <div className="game-meta">
                                                <span className="players">
                                                    <Icon icon="vaadin:user"/>
                                                    {game.players?.length || 0} players
                                                </span>
                                                <span className="created">
                                                    Created: {new Date(game.createdTime).toLocaleString() || `UNDEFINED`}
                                                </span>
                                            </div>
                                        </div>
                                        <Button
                                            theme="primary"
                                            disabled={game.gameState === "IN_PROGRESS" || !playerName.trim() || joiningGame || game.players?.length >= 4}
                                            onClick={() => {
                                                setPendingGameId(game.gameId);
                                                setColorDialogOpen(true);
                                            }}
                                            className="join-btn"
                                        >
                                            {joiningGame && selectedGameId === game.gameId ? (
                                                <>
                                                    <Icon icon="vaadin:spinner"/>
                                                    Joining...
                                                </>
                                            ) : "Join Game"}
                                        </Button>
                                        <ColorPickerDialog
                                            opened={colorDialogOpen}
                                            onClose={() => setColorDialogOpen(false)}
                                            onSelect={confirmJoinWithColor}
                                            takenColors={
                                                games.find(g => g.gameId === pendingGameId)?.players.map(p => p.color) || []
                                            }
                                        />
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}
            </div>
            <div ref={containerRef} style={{width: '100%', height: '100%'}}/>
        </div>
    );
}

export default GameLobby;