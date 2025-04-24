import {useMutation, useQuery} from '@apollo/client';
import {CREATE_GAME_MUTATION, GET_ACTIVE_GAMES, GET_GAME_BY_PLAYER_ID, JOIN_GAME_MUTATION} from '../graphql/queries';
import {GameDTO} from '../components/models/GameDTO';
import {useNavigate} from 'react-router';
import GameSingleton from '../stores/singletons/GameSingleton';
import {useEffect, useState} from 'react';
import '../styles/lobby.css';
import {PlayerColor} from "../components/utils/constants";
import {ColorPickerDialog} from "./components/ColorPickerDialog";

export const LobbyView = () => {
    const navigate = useNavigate();
    /**
     * checking if playerId exist in localstorage, if not -> generating new one
     **/
    const [playerId] = useState(() => {
        let id = localStorage.getItem('playerId');
        if (!id) {
            id = crypto.randomUUID();
            localStorage.setItem('playerId', id);
        }

        return id;
    });

    const [playerName, setPlayerName] = useState('');
    const isNameEntered = playerName.trim() !== '';

    const [showColorDialog, setShowColorDialog] = useState(false);
    const [selectedGame, setSelectedGame] = useState<GameDTO | null>(null);

    const [createGame] = useMutation(CREATE_GAME_MUTATION);
    const [joinGameMutation] = useMutation(JOIN_GAME_MUTATION);
    const [foundGameForPlayer, setFoundGameForPlayer] = useState<GameDTO | null>(null);
    const [rejoinAvailable, setRejoinAvailable] = useState(false);

    const {data: allGames, error: gamesError, loading: gamesLoading} = useQuery(GET_ACTIVE_GAMES, {
        pollInterval: 5000,
        fetchPolicy: 'cache-and-network'
    });

    const {data: findGameData} = useQuery(GET_GAME_BY_PLAYER_ID, {
        variables: {playerId},
        fetchPolicy: 'cache-and-network',
        skip: !playerId,
    });

    const games: GameDTO[] = allGames?.getActiveGames?.map(GameDTO.fromRaw) || [];

    useEffect(() => {
        if (findGameData?.findGameByPlayerId) {
            const foundGame = GameDTO.fromRaw(findGameData.findGameByPlayerId);
            setFoundGameForPlayer(foundGame);
            setRejoinAvailable(true);
        }
    }, [findGameData]);

    const handleColorSelect = (color: PlayerColor) => {
        if (!selectedGame) return;
        joinGameMutation({
            variables: {
                playerId,
                gameId: selectedGame.gameId,
                playerName,
                playerColor: color,
            },
        }).then((response) => {
            setSelectedGame(response.data.joinToGame);
            joinGame()
        });
        setShowColorDialog(false);
    };

    const joinGame = () => {
        const gameToJoin = rejoinAvailable ? foundGameForPlayer : selectedGame;

        if (!gameToJoin) {
            console.warn("No game to join.");
            return;
        }

        GameSingleton.initialize(gameToJoin);
        navigate(`/game/${gameToJoin.gameId}`);
    };


    if (gamesLoading) {
        return (
            <div className="centered">
                <div className="spinner"></div>
            </div>
        );
    }

    if (gamesError) {
        return <div className="error-alert">Error: {gamesError.message}</div>;
    }

    return (
        <div className="lobby-container">
            <button onClick={
                () => localStorage.setItem('playerId', crypto.randomUUID())
            }style={{width:"fit-content"}}>REGENERATE PLAYERID (DEBUG)
            </button>
            <div className="lobby-header">
                <h1>Active Games</h1>
            </div>

            <div className="player-name-input">
                <label htmlFor="playerName"></label>
                <input
                    type="text"
                    id="playerName"
                    value={playerName}
                    onChange={(e) => setPlayerName(e.target.value)}
                    placeholder="Enter your name"
                />

                <button
                    className="button createButton"
                    onClick={() => createGame()}
                    disabled={!isNameEntered}
                >
                    Create Game
                </button>
            </div>

            {!games.length ? (
                <div className="empty-state">
                    <p>No active games available</p>
                </div>
            ) : (
                <ul className="game-list">
                    {games.map(game => (
                        <li
                            key={game.gameId}
                            className="game-card"
                            onClick={() => {
                                if (isNameEntered) {
                                    setSelectedGame(game);
                                    setShowColorDialog(true);
                                }
                            }}
                        >
                            <div className="game-icon">🎮</div>
                            <div className="game-info">
                                <div className="game-title">
                                    {game.gameId.slice(0,30)}...
                                    <span className={`chip ${game.gameState === 'STARTED' ? 'warning' : 'success'}`}>
                                        {game.gameState}
                                    </span>
                                </div>
                                <div className="game-players">
                                    👥 {game.players.length} player{game.players.length !== 1 ? 's' : ''}
                                </div>
                            </div>

                            <button
                                className={`button ${rejoinAvailable && game.gameId === foundGameForPlayer?.gameId ? 'rejoinButton' : 'joinButton'}`}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    if (rejoinAvailable && game.gameId === foundGameForPlayer?.gameId) {
                                        setSelectedGame(game);
                                        joinGame();
                                    } else {
                                        setSelectedGame(game);
                                        setShowColorDialog(true);
                                    }
                                }}
                                disabled={!isNameEntered && !(rejoinAvailable && game.gameId === foundGameForPlayer?.gameId)}
                            >
                                {rejoinAvailable && game.gameId === foundGameForPlayer?.gameId ? 'Rejoin' : 'Join'}
                            </button>
                        </li>
                    ))}
                </ul>
            )}

            <ColorPickerDialog
                opened={showColorDialog}
                onClose={() => setShowColorDialog(false)}
                onSelect={handleColorSelect}
                takenColors={selectedGame?.players.map(p => p.color) || []}
            />
        </div>
    );
};
