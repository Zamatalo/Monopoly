import {useEffect, useRef, useState} from 'react';
import {useQuery, useSubscription} from "@apollo/client";
import {
    DICE_UPDATED_SUBSCRIPTION,
    GAME_UPDATED_SUBSCRIPTION,
    GET_AVAILABLE_ACTIONS,
    GET_FIND_BY_ID,
    GET_PLAYER
} from "../graphql/queries";
import GameSingleton from "../stores/singletons/GameSingleton";
import "../styles/gameView.css";
import {useParams} from "react-router";
import WorldSingleton from '../stores/singletons/WorldSingleton';
import {diceUpdate, initGame, resetGameEnvironment, updateGame} from "../stores/GameEnvironment";
import CurrentPlayerSingleton from "../stores/singletons/CurrentPlayerSingleton";
import currentPlayerSingleton from "../stores/singletons/CurrentPlayerSingleton";
import StartGameDialog from "./components/StartGameDialog";
import {GameState, PlayerActions} from "../components/utils/constants";
import UIGameInterface from "./components/UIGameInterface";

export const GameView = () => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const [sceneInitialized, setSceneInitialized] = useState(false);
    const [gameInitialized, setGameInitialized] = useState(false);
    const {gameId} = useParams<{ gameId: string }>();

    const {data: actionsData} = useQuery(GET_AVAILABLE_ACTIONS, {
        variables: {
            gameId: gameId,
        }
    });

    const {data: gameData} = useQuery(GET_FIND_BY_ID, {
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
        fetchPolicy: "no-cache",
        onData: ({data}) => {
            const updatedGame = data?.data?.gameUpdated;
            console.log("Game updated", updatedGame);
            if (updatedGame && gameInitialized) {
                updateGame(updatedGame);
            }
        }
    });

    useSubscription(DICE_UPDATED_SUBSCRIPTION, {
        variables: {gameId},
        fetchPolicy: "standby",
        onData: ({data}) => {
            const updatedDicePos = data?.data?.diceUpdated;
            if (updatedDicePos && gameInitialized) {
                diceUpdate(updatedDicePos);
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
            console.log()

            setSceneInitialized(true);
        }
    }, [gameData, findPlayer]);

    useEffect(() => {
        if (!sceneInitialized || !currentPlayerSingleton.hasInstance() || gameInitialized) return;
        initGame();
        setGameInitialized(true);
    }, [sceneInitialized, gameInitialized]);



    useEffect(() => {
        return () => {
            console.log("Cleaning up");
            const container = containerRef.current;
            if (container && container.children.length > 0) {
                container.remove();
            }
            resetGameEnvironment();
        };
    }, []);

    return (
        <div className="game-view">
            <div ref={containerRef} className="canvas"/>
            {sceneInitialized && actionsData && actionsData?.getPossibleCurrentPlayerActions.includes(PlayerActions.START_GAME) && GameSingleton.hasInstance() && (GameSingleton.getInstance().gameState != GameState.IN_PROGRESS) && (
                <StartGameDialog />
            )}
            {sceneInitialized && GameSingleton.hasInstance() && GameSingleton.getInstance().gameState === GameState.IN_PROGRESS &&
                <UIGameInterface/>}
        </div>
    );
};
