import {useQuery} from '@apollo/client';
import {GET_ACTIVE_GAMES} from '../graphql/queries';
import {GameDTO} from '../components/models/GameDTO';
import {useNavigate} from 'react-router';
import GameSingleton from "../components/utils/GameSingleton";
import {useState} from "react";
import '../styles/lobby.css'

export const LobbyView = () => {
    const navigate = useNavigate();
    const {data, error, loading} = useQuery(GET_ACTIVE_GAMES, {
        pollInterval: 5000,
        fetchPolicy: 'cache-and-network',
    });
    const [rejoinAvailable, setrejoinAvailable] = useState(false);
    const handleJoinGame = (game: GameDTO) => {
        GameSingleton.initialize(game);
        navigate(`/game/${game.gameId}`);
    };

    const games: GameDTO[] = data?.getActiveGames?.map(GameDTO.fromRaw) || [];

    if (loading) return <div className="centered">
        <div className="spinner"></div>
    </div>;
    if (error) return <div className="error-alert">Error: {error.message}</div>;

    return (
        <div className="lobby-container">
            <div className="lobby-header">
                <h1>Active Games</h1>
            </div>

            {!games.length ? (
                <div className="empty-state">
                    <p>No active games available</p>
                </div>
            ) : (
                <ul className="game-list">
                    {games.map(game => (
                        <li key={game.gameId} className="game-card" onClick={() => handleJoinGame(game)}>
                            <div className="game-icon">🎮</div>
                            <div className="game-info">
                                <div className="game-title">
                                    {game.gameId}
                                    <span className={`chip ${game.gameState === 'WAITING' ? 'warning' : 'success'}`}>
                                        {game.gameState}
                                    </span>
                                </div>
                                <div className="game-players">
                                    👥 {game.players.length} player{game.players.length !== 1 ? 's' : ''}
                                </div>
                            </div>
                            <button
                                className="join-button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleJoinGame(game);
                                }}
                            >
                                Join
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};