import React, {useEffect, useState} from 'react';
import {useMutation, useQuery, useApolloClient} from '@apollo/client';
import {
    CREATE_GAME_MUTATION,
    GET_ACTIVE_GAMES,
    GET_GAME_BY_PLAYER_ID,
    JOIN_GAME_MUTATION
} from 'Frontend/utils/queries';
import {GameDTO} from "Frontend/components/objects/GameDTO";
import {Button} from "@vaadin/react-components/Button.js";
import {Icon} from "@vaadin/react-components/Icon.js";
import "@vaadin/icons";
import "../themes/my-theme/lobby.css";
import {GameState, PlayerColor} from "Frontend/utils/constants";
import ColorPickerDialog from '../components/ColorPickerDialog';

interface GameLobbyProps {
    onGameStart: (gameId: string) => void;
    playerId?: string | null;
}

const GameLobby: React.FC<GameLobbyProps> = ({onGameStart, playerId}) => {
    const [playerName, setPlayerName] = useState('');
    const [selectedGameId, setSelectedGameId] = useState<string | null>(null);
    const [games, setGames] = useState<GameDTO[]>([]);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [colorDialogOpen, setColorDialogOpen] = useState(false);
    const [pendingGameId, setPendingGameId] = useState<string | null>(null);
    const [reconnectState, setReconnectState] = useState({
        loading: false,
        available: false
    });

    const containerRef = React.useRef<HTMLDivElement | null>(null);
    const client = useApolloClient();

    const {loading: gamesLoading, data: gamesData, error: gamesError} = useQuery(GET_ACTIVE_GAMES, {
        pollInterval: 5000
    });

    const [createGame] = useMutation(CREATE_GAME_MUTATION);
    const [joinGame, {loading: joiningGame}] = useMutation(JOIN_GAME_MUTATION);

    useEffect(() => {
        if (gamesData?.getActiveGames) {
            setGames(gamesData.getActiveGames);
        }
    }, [gamesData]);

    useEffect(() => {
        if (gamesError) {
            setErrorMessage(`Error loading games: ${gamesError.message}`);
        }
    }, [gamesError]);

    const checkForExistingGame = async () => {
        if (!playerId) return;

        setReconnectState({loading: true, available: false});
        setErrorMessage(null);

        try {
            const {data} = await client.query({
                query: GET_GAME_BY_PLAYER_ID,
                variables: {playerId},
                fetchPolicy: 'network-only'
            });

            setReconnectState({
                loading: false,
                available: !!data?.findGameByPlayerId
            });

            if (!data?.findGameByPlayerId) {
                setErrorMessage("No active game found to reconnect to");
            }
        } catch (error: any) {
            setErrorMessage(`Error checking for existing game: ${error.message}`);
            setReconnectState({loading: false, available: false});
        }
    };

    const handleReconnect = async () => {
        if (!playerId) return;

        setReconnectState(prev => ({...prev, loading: true}));
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
                setReconnectState({loading: false, available: false});
            }
        } catch (error: any) {
            setErrorMessage(`Error reconnecting: ${error.message}`);
            setReconnectState({loading: false, available: false});
        }
    };

    const handleCreateGame = async () => {
        if (!playerName.trim()) {
            setErrorMessage('Please enter your name');
            return;
        }

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
    };

    const confirmJoinWithColor = async (color: PlayerColor) => {
        if (!pendingGameId || !playerName.trim()) return;

        setErrorMessage(null);

        try {
            const {data} = await joinGame({
                variables: {
                    gameId: pendingGameId,
                    playerName,
                    playerColor: color,
                    playerId: playerId
                },
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

    const renderReconnectButton = () => {
        if (reconnectState.available) {
            return (
                <Button
                    theme="primary"
                    onClick={handleReconnect}
                    className="reconnect-btn"
                    disabled={reconnectState.loading}
                >
                    {reconnectState.loading ? (
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
            );
        }

        return (
            <Button
                theme="secondary"
                onClick={checkForExistingGame}
                className="check-reconnect-btn"
                disabled={reconnectState.loading || !playerId}
            >
                {reconnectState.loading ? (
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
        );
    };

    const renderGameList = () => {
        if (gamesLoading) {
            return (
                <div className="loading-spinner">
                    <Icon icon="vaadin:spinner"/>
                    <span>Loading games...</span>
                </div>
            );
        }

        if (games.length === 0) {
            return (
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
            );
        }

        return (
            <div className="game-list">
                {games.map((game) => (
                    <div
                        key={game.gameId}
                        className={`game-card ${selectedGameId === game.gameId ? "selected" : ""}`}
                        onClick={() => setSelectedGameId(game.gameId)}
                    >
                        <div className="game-info">
                            <h3 className={`status`}>
                                {game.gameState === GameState.STARTED ? GameState.FINISHED : GameState.IN_PROGRESS}
                            </h3>
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
                            disabled={
                                game.gameState === "IN_PROGRESS" ||
                                !playerName.trim() ||
                                joiningGame ||
                                game.players?.length >= 4
                            }
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
        );
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
                <h2>Available Games</h2>
                <div className="game-list-header">
                        <Button
                            theme="primary"
                            onClick={handleCreateGame}
                            className="create-game-btn"
                            disabled={!playerName.trim()}
                        >
                            <Icon icon="vaadin:plus" slot="prefix"/>
                            Create New Game
                        </Button>
                        {renderReconnectButton()}
                </div>
                {renderGameList()}
            </div>
            <div ref={containerRef} style={{width: '100%', height: '100%'}}/>
        </div>
    );
};

export default GameLobby;