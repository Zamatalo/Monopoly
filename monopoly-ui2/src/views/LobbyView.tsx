import { useEffect } from 'react';
import { useQuery } from '@apollo/client';
import {GET_ACTIVE_GAMES, GET_PLAYER} from '../graphql/queries';
import { useGameStore } from '../stores/gameStore';
import { GameDTO } from '../components/models/GameDTO';
import { useNavigate } from 'react-router-dom';
import {Text} from "@react-three/drei";

export const LobbyView = () => {
    const { data, error, loading } = useQuery(GET_ACTIVE_GAMES, {
        pollInterval: 5000,
        fetchPolicy: 'cache-and-network',

    });
    // const {data:playerData,error:getPlayerError,loading:getPlayerLoading} = useQuery(GET_PLAYER, {
    //     variables:
    // });
    const games: GameDTO[] = data?.getActiveGames || [];
    const { setGame,setCurrentPlayer } = useGameStore();
    const navigate = useNavigate();

    const handleJoinGame = (game: GameDTO) => {
        setGame(game);
        navigate(`/game/${game.gameId}`);
    };

    if (loading) return <div>Loading games...</div>;
    if (error) return <div>Error: {error.message}</div>;

    return (
        <div>
            <h2>Active Games</h2>
            {games.length === 0 ? (
                <p>No active games found.</p>
            ) : (
                <ul>
                    {games.map((game) => (
                        <li
                            key={game.gameId}
                            onClick={() => handleJoinGame(game)}
                        >
                            <span>{game.gameId}</span>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};
