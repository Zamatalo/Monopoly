import {useEffect, useRef, useState} from 'react';
import {useQuery, useSubscription} from "@apollo/client";
import {GAME_UPDATED_SUBSCRIPTION, GET_FIND_BY_ID, GET_PLAYER} from "../graphql/queries";
import GameSingleton from "../stores/GameSingleton";
import {GameDTO} from "../components/models/GameDTO";
import UIGameInterface from "../components/UIGameInterface";
import "../styles/gameView.css";
import {useParams} from "react-router";
import WorldSingleton from '../stores/WorldSingleton';
import {resetGameEnvironment, updateGame} from "../stores/GameEnvironment";
import CurrentPlayerSingleton from "../stores/CurrentPlayerSingleton";
import {PlayerDTO} from "../components/models/PlayerDTO";

export const GameView = () => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const [sceneInitialized, setSceneInitialized] = useState(false);
    const {gameId} = useParams<{ gameId: string }>();
    const {data: gameData, loading: loadingGame} = useQuery(GET_FIND_BY_ID, {
        variables: {gameId},
        fetchPolicy: 'cache-and-network',
    });

    const {data: findPlayer} = useQuery(GET_PLAYER, {
        variables: {
            playerId: localStorage.getItem('playerId')
        },
        fetchPolicy: 'cache-and-network',
    });

    useSubscription(GAME_UPDATED_SUBSCRIPTION, {
        variables: {gameId},
        fetchPolicy: 'network-only',
        onData: ({data}) => {
            const updatedGame = data?.data?.gameUpdated;
            if (updatedGame) {
                updateGame(updatedGame);
            }
        }
    });

    useEffect(() => {
        if (gameData && !sceneInitialized && findPlayer) {
            const container = containerRef.current;
            if (!container) return;
            WorldSingleton.getInstance(container);

            const initializedGame = gameData?.findGameById;
            GameSingleton.initialize(initializedGame);

            if (findPlayer?.getPlayer) {
                const currentPlayer = findPlayer.getPlayer;
                CurrentPlayerSingleton.initialize(currentPlayer);
            } else {
                console.error("No player data found in findPlayer", findPlayer);
            }

            setSceneInitialized(true);
        }
    }, [gameData, findPlayer]);


    useEffect(() => {
        if (!sceneInitialized || !findPlayer?.getPlayer) return;


        const game = GameSingleton.getInstance();
        game.loadBoardModel();
        game.players.forEach(player => player.loadPlayerModel());
    }, [sceneInitialized, findPlayer]);


    useEffect(() => {
        return () => {
            console.log("Cleaning up");
            const container = containerRef.current;
            if (container && container.children.length > 0) {
                container.removeChild(container.children[0]);
            }
            resetGameEnvironment();
        };
    }, []);

    return (
        <div className="game-view">
            <div ref={containerRef} className="canvas"/>
            {sceneInitialized && <UIGameInterface/>}
        </div>
    );
};
