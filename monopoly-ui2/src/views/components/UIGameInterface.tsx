import React, {useEffect, useState} from 'react';
import GameSingleton from "../../stores/singletons/GameSingleton";
import {useMutation, useQuery} from "@apollo/client";
import {BUY_PROPERTY_MUTATION, END_TURN, ROLL_DICE} from "../../graphql/queries";
import {PlayerDTO} from "../../components/models/PlayerDTO";
import CurrentPlayerSingleton from "../../stores/singletons/CurrentPlayerSingleton";
import {ColorHexMap, GameState, PlayerActions} from "../../components/utils/constants";
import '../../styles/gameInterface.css'

const UIGameInterface: React.FC = () => {
    const game = GameSingleton.getInstance();
    const [rollDice, {error}] = useMutation(ROLL_DICE);
    const [buyProperty] = useMutation(BUY_PROPERTY_MUTATION);
    const [endTurn] = useMutation(END_TURN);
    const [currentPlayer, setCurrentPlayer] = useState<PlayerDTO | null>(null);
    const [currentPlayerSingleton, setCurrentPlayerSingleton] = useState<PlayerDTO | null>(null);
    const [rolledValue, setRolledValue] = useState<number | null>(null);

    useEffect(() => {
        setCurrentPlayerSingleton(CurrentPlayerSingleton.getInstance());
    }, [game]);

    useEffect(() => {
        const current = game.players[game.currentPlayerIndex];
        setCurrentPlayer(current);
    }, [game.currentPlayerIndex]);


    const handleRollDice = async () => {
        if (!game?.gameId || !currentPlayer?.playerId) return;
        try {
            const {data} = await rollDice({
                variables: {
                    gameId: game.gameId,
                    playerId: currentPlayer.playerId
                }
            });
            const value = data?.rollDice;
            if (value != null) {
                setRolledValue(value);
            }
        } catch (e) {
            console.error(e);
        }
    };

    const handleBuyProperty = async () => {
        if (!game?.gameId || !currentPlayer?.playerId) return;

        try {
            await buyProperty({
                variables: {
                    gameId: game.gameId,
                    playerId: currentPlayerSingleton?.playerId
                }
            });
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
        } catch (e) {
            console.error(e);
        }
    };

    if (!game?.gameId) {
        return <div className="game-interface loading">Loading game data...</div>;
    }

    const currentGameState = typeof game.gameState === 'string' ? game.gameState : GameState[game.gameState];

    return (
        <div className="game-interface compact">
            <div className="current-player">
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
                    {currentPlayer?.inJail && <span className="jail">in Jail</span>}
                </div>
            </div>

            {rolledValue && !error && (
                <div className="rolled-value">🎲 You rolled: {rolledValue}</div>
            )}
            {error && (
                <div className="error">
                    Not your turn. Current player is: {game.players[game.currentPlayerIndex].color}
                </div>
            )}

            <div className="action-buttons">
                {!currentPlayer?.inJail && (
                    <button onClick={handleRollDice} className="btn dice">
                        Roll Dice
                    </button>
                )}
                <button onClick={handleBuyProperty} className="btn buy"
                        disabled={!currentPlayerSingleton?.playerActions?.includes(PlayerActions.BUY_PROPERTY)}>
                    Buy Property
                </button>
                <button onClick={handleEndTurn} className="btn end">
                    End Turn
                </button>
            </div>

            <div className="players-list">
                {game.players.map((player, index) => (
                    <div
                        key={player.playerId}
                        className={`player-chip ${index === game.currentPlayerIndex ? 'active' : ''}`}
                        style={{
                            backgroundColor: `${ColorHexMap[player.color]}20`,
                            borderColor: ColorHexMap[player.color],
                        }}
                    >
                        <span>Player: {player.playerName}</span>
                        <span>${player.balance.toLocaleString()}</span>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UIGameInterface;
