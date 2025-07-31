import React, {useEffect, useState} from 'react';
import GameSingleton from "../../stores/singletons/GameSingleton";
import {useMutation} from "@apollo/client";
import {BUY_PROPERTY_MUTATION, END_TURN, ROLL_DICE, SPECIAL_TILE} from "../../graphql/queries";
import {PlayerDTO} from "../../components/models/PlayerDTO";
import CurrentPlayerSingleton from "../../stores/singletons/CurrentPlayerSingleton";
import {ColorHexMap, PlayerActions} from "../../components/utils/constants";
import '../../styles/gameInterface.css'
import {useNotification} from "./NotificationContextType";

const UIGameInterface: React.FC = () => {
    const game = GameSingleton.getInstance();
    const { showNotification } = useNotification();
    const [rollDice, {error}] = useMutation(ROLL_DICE);
    const [buyProperty] = useMutation(BUY_PROPERTY_MUTATION);
    const [endTurn] = useMutation(END_TURN);
    const [specialTile] = useMutation(SPECIAL_TILE);
    const [currentPlayer, setCurrentPlayer] = useState<PlayerDTO | null>(null);
    const [currentPlayerSingleton, setCurrentPlayerSingleton] = useState<PlayerDTO | null>(null);


    useEffect(() => {
        setCurrentPlayerSingleton(CurrentPlayerSingleton.getInstance());
    }, [game]);

    useEffect(() => {
        const current = game.players[game.currentPlayerIndex];
        setCurrentPlayer(current);
    }, [game.currentPlayerIndex]);



    const handleSpecialTile = async () => {
        if (!game?.gameId || !currentPlayer?.playerId) return;

        try {
            const {data:specialEffect} = await specialTile({
                variables: {
                    gameId: game.gameId,
                    playerId: currentPlayerSingleton?.playerId
                }
            });
            showNotification(specialEffect.resolveSpecialTile.text);
        } catch (e) {
            console.error(e);
        }
    }

    const handleRollDice = async () => {
        if (!game?.gameId || !currentPlayer?.playerId) return;
        try {
            const {data:rollDiceData} = await rollDice({
                variables: {
                    gameId: game.gameId,
                    playerId: currentPlayerSingleton?.playerId
                }
            });
            const value = rollDiceData?.rollDice;
            if (value != null) {
                showNotification(`🎲 You rolled ${value}`);
            }
            if (value==0){
                showNotification(`You are in Jail.\n Turns Left: ${currentPlayerSingleton?.inJail_Turns}`);
            }
        } catch (e) {
            console.error(e);
        }
    };

    const handleBuyProperty = async () => {
        if (!game?.gameId || !currentPlayer?.playerId) return;

        try {
            const {data: buyPropData} = await buyProperty({
                variables: {
                    gameId: game.gameId,
                    playerId: currentPlayerSingleton?.playerId
                }
            });
            showNotification(`Bought:\n ${buyPropData.buyPropertyForPlayer.displayName}`);
        } catch (e) {
            console.error(e);
        }
    };

    const handleEndTurn = async () => {
        if (!game?.gameId || !currentPlayer?.playerId) return;

        try {
            await endTurn({
                variables: {
                    gameId: game.gameId,
                    playerId: currentPlayerSingleton?.playerId
                }
            });
            showNotification(`Turn ended`);
        } catch (e) {
            console.error(e);
        }
    };

    if (!game?.gameId) {
        return <div className="game-interface loading">Loading game data...</div>;
    }
    return (
        <div className="game-interface compact">
            <div className="current-player">
                <span
                    style={{color: "red"}}>DEBUG_POSSIBLE_ACTIONS: {JSON.stringify(currentPlayerSingleton?.playerActions)}
                </span>
                <div className="player-info">
                    <span className="name">Name: {currentPlayerSingleton?.playerName}</span>
                    <span className="balance">${currentPlayerSingleton?.balance.toLocaleString()}</span>

                </div>
                <div className="position">
                    <span>Pos: {currentPlayerSingleton?.position}</span>
                    <span style={{
                        color: ColorHexMap[currentPlayerSingleton?.color || "RED"],
                        marginLeft: "auto"
                    }}>Color: {currentPlayerSingleton?.color}</span>

                    {(currentPlayerSingleton?.inJail_Turns ?? 0) > 0 && (
                        <span className="jail">in Jail</span>
                    )}
                </div>
            </div>

            {error && (
                <div className="error">
                    Not your turn. Current player is: {game.players[game.currentPlayerIndex].color}
                </div>
            )}

            <div className="action-buttons">
                <button onClick={handleRollDice}
                        disabled={!currentPlayerSingleton?.playerActions?.includes(PlayerActions.ROLL_DICE)}
                        className="btn dice">
                    Roll Dice
                </button>
                <button onClick={handleSpecialTile}
                        className="btn special-tile"
                        disabled={!currentPlayerSingleton?.playerActions?.includes(PlayerActions.SPECIAL_TILE_EFFECT)}
                >
                    Special Tile
                </button>

                <button onClick={handleBuyProperty}
                        className="btn buy"
                        disabled={!currentPlayerSingleton?.playerActions?.includes(PlayerActions.BUY_PROPERTY)}>
                    Buy Property
                </button>
                <button onClick={handleEndTurn}
                        disabled={!currentPlayerSingleton?.playerActions?.includes(PlayerActions.END_TURN)}
                        className="btn end">
                    End Turn
                </button>
            </div>

            <div className="players-list">
                {game.players.map((player, index) => (
                    <div
                        key={player.playerId}
                        className={`player-chip ${index === game.currentPlayerIndex ? 'active' : ''}`}
                        style={{
                            backgroundColor: `${ColorHexMap[player.color]}30`,
                            borderColor: ColorHexMap[player.color],
                        }}
                    >
                        <span>{player.playerName}{player.inJail_Turns > 0 ? " (in Jail)" : ""}</span>
                        <span>{player.balance.toLocaleString()}</span>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UIGameInterface;
