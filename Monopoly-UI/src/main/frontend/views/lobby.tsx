import React, {useEffect, useState} from 'react';
import { useMutation, useQuery } from '@apollo/client';
import { CREATE_GAME_MUTATION, JOIN_GAME_MUTATION, GET_ACTIVE_GAMES } from 'Frontend/utils/queries';
import { PlayerDTO } from "Frontend/components/PlayerDTO";
import { Button } from "@vaadin/react-components/Button.js";
import { Icon } from "@vaadin/react-components/Icon.js";
import "@vaadin/icons";
import "../themes/my-theme/lobby.css";
import {PlayerColor} from "Frontend/utils/constants";

interface GameLobbyProps {
    onGameStart: (gameId: string) => void;
}

interface GameInfo {
    gameId: string;
    players: PlayerDTO[];
    gameState: string;
    createdTime: string;
    currentPlayerIndex: number;
}

function GameLobby({ onGameStart }: GameLobbyProps) {
    const [playerName, setPlayerName] = useState('');
    const [selectedGameId, setSelectedGameId] = useState<string | null>(null);
    const [games, setGames] = useState<GameInfo[]>([]);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const containerRef = React.useRef<HTMLDivElement | null>(null);

    const { loading: gamesLoading, data, error } = useQuery(GET_ACTIVE_GAMES, {
        pollInterval: 5000
    });

    const [createGame] = useMutation(CREATE_GAME_MUTATION);
    const [joinGame, { loading: joiningGame }] = useMutation(JOIN_GAME_MUTATION);

    useEffect(() => {
        if (data) {
            setGames(data.getActiveGames);
        }
    }, [data]);

    useEffect(() => {
        if (error) {
            setErrorMessage(`Error loading games: ${error.message}`);
        }
    }, [error]);

    const handleCreateGame = async () => {
        if (playerName.trim()) {
            setErrorMessage(null);
            try {
                const { data } = await createGame();
                if (data) {
                    const gameId = data.createGame.gameId;
                    await handleJoinGame(gameId);
                }
            } catch (error: any) {
                setErrorMessage(`Error creating game: ${error.message}`);
            }
        } else {
            setErrorMessage('Please enter your name');
        }
    };

    const handleJoinGame = async (gameId: string) => {
        if (playerName.trim()) {
            setErrorMessage(null);
            let playerColor =PlayerColor.PLAYER_RED;
            try {
                const { data } = await joinGame({ variables: { gameId, playerName,playerColor} });
                if (data) {
                    onGameStart(data.joinToGame.gameId);
                }
            } catch (error: any) {
                setErrorMessage(`Error joining game: ${error.message}`);
            }
        } else {
            setErrorMessage('Please enter your name');
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
                                            <h3>{game.gameState}</h3>
                                            <div className="game-meta">
                                                <span className="players">
                                                    <Icon icon="vaadin:user"/>
                                                    {game.players?.length || 0} players
                                                </span>
                                                <span className={`status ${game.gameState}`}>
                                                    {game.gameState === "STARTED" ? "Waiting" : "In Progress"}
                                                </span>
                                                <span className="created">
                                                    Created: {new Date(game.createdTime || Date.now()).toLocaleString()}
                                                </span>
                                            </div>
                                        </div>
                                        <Button
                                            theme="primary"
                                            disabled={game.gameState === "IN_PROGRESS" || !playerName.trim() || joiningGame}
                                            onClick={() => handleJoinGame(game.gameId)}
                                            className="join-btn"
                                        >
                                            {joiningGame && selectedGameId === game.gameId ? (
                                                <>
                                                    <Icon icon="vaadin:spinner"/>
                                                    Joining...
                                                </>
                                            ) : "Join Game"}
                                        </Button>
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