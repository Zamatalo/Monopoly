import {useEffect, useRef, useState} from 'react';
import WorldSingleton from '../components/utils/WorldSingleton';
import {useMutation, useQuery, useSubscription} from "@apollo/client";
import {GAME_UPDATED_SUBSCRIPTION, GET_FIND_BY_ID, ROLL_DICE} from "../graphql/queries";
import GameSingleton from "../components/utils/GameSingleton";
import {updateGame} from "../stores/GameEnvironment";
import {GameDTO} from "../components/models/GameDTO";
import UIGameInterfaceProps from "../components/UIGameInterfaceProps";
import "../styles/gameView.css";

export const GameView = () => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const [game, setGame] = useState<GameDTO | null>(null);
    const [sceneInitialized, setSceneInitialized] = useState(false);
    const {data: gameData, loading: loadingGame} = useQuery(GET_FIND_BY_ID, {
        variables: {gameId: game?.gameId ?? ''}
    });

    const {data, error} = useSubscription(GAME_UPDATED_SUBSCRIPTION, {
        variables: {
            gameId: game?.gameId,
            playerId: 1,

        },
        onData: ({data}) => {
            console.log('Game update received:', data);
            const updatedGame = data?.data?.gameUpdated;
            if (updatedGame) {
                updateGame(GameDTO.fromRaw(updatedGame));
            }
        }
    });

    const [rollDice] = useMutation(ROLL_DICE);


    useEffect(() => {
        const restored = GameSingleton.tryRestore();
        if (restored) {
            setGame(restored);
        } else if (gameData?.getGameById) {
            const g = GameSingleton.initialize(gameData.getGameById);
            setGame(g);
        }
    }, [gameData]);


    useEffect(() => {
        const container = containerRef.current;
        if (!container || !game) return;
        WorldSingleton.getInstance(container);
    }, [game]);

    useEffect(() => {
        if (!game || !game.players || sceneInitialized) return;

        game.loadBoardModel();
        game.players.forEach((player) => {
            player.loadPlayerModel();
        });
        setSceneInitialized(true);
    }, [game, sceneInitialized]);

    useEffect(() => {
        return () => {
            console.log("Cleaning up");
            WorldSingleton.reset();
            GameSingleton.reset();
        };
    }, []);

    return (
        <div className="game-view">
            <div ref={containerRef} className="canvas"/>
            {game && (
                <UIGameInterfaceProps></UIGameInterfaceProps>
            )}
        </div>
    );

};
