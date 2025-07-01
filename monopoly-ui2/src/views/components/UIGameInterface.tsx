import React, {useEffect, useState} from 'react';
import GameSingleton from "../../stores/singletons/GameSingleton";
import {useMutation} from "@apollo/client";
import {BUY_PROPERTY_MUTATION, END_TURN, ROLL_DICE, SPECIAL_TILE} from "../../graphql/queries";
import {PlayerDTO} from "../../components/models/PlayerDTO";
import CurrentPlayerSingleton from "../../stores/singletons/CurrentPlayerSingleton";
import {ColorHexMap, GameState, PlayerActions} from "../../components/utils/constants";
import '../../styles/gameInterface.css'

const UIGameInterface: React.FC = () => {
    const game = GameSingleton.getInstance();
    const [rollDice, {error}] = useMutation(ROLL_DICE);
    const [buyProperty] = useMutation(BUY_PROPERTY_MUTATION);
    const [endTurn] = useMutation(END_TURN);
    const [specialTile] = useMutation(SPECIAL_TILE);
    const [currentPlayer, setCurrentPlayer] = useState<PlayerDTO | null>(null);
    const [currentPlayerSingleton, setCurrentPlayerSingleton] = useState<PlayerDTO | null>(null);
    const [rolledValue, setRolledValue] = useState<number | null>(null);
    const [specialTileEffet,setSpecialTileEffet] = useState<String | null>(null);
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
            console.log(specialEffect);
        } catch (e) {
            console.error(e);
        }
    }

    const handleRollDice = async () => {
        if (!game?.gameId || !currentPlayer?.playerId) return;
        try {
            const {data} = await rollDice({
                variables: {
                    gameId: game.gameId,
                    playerId: currentPlayerSingleton?.playerId
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
            const {data: response} = await buyProperty({
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

            {rolledValue && !error && (
                <div className="rolled-value">🎲 You rolled: {rolledValue}</div>
            )}
            {error && (
                <div className="error">
                    Not your turn. Current player is: {game.players[game.currentPlayerIndex].color}
                </div>
            )}

            <div className="action-buttons">
                {/*{(currentPlayerSingleton?.inJail_Turns ?? 0) === 0 && (*/}
                {/*    */}
                {/*)}*/}
                <button onClick={handleRollDice}
                        disabled={!currentPlayerSingleton?.playerActions?.includes(PlayerActions.ROLL_DICE)}
                        className="btn dice">
                    Roll Dice
                </button>
                <button onClick={handleSpecialTile}
                        className="btn special-tile"
                        disabled={!currentPlayerSingleton?.playerActions?.includes(PlayerActions.SPECIAL_TILE)}
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
                        <span>{player.playerName}</span>
                        <span>${player.balance.toLocaleString()}</span>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UIGameInterface;
