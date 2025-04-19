import React from 'react';
import { GameState } from "./utils/constants";
import GameSingleton from "./utils/GameSingleton";
import "../styles/gameInterface.css";

interface UIGameInterfaceProps {
    onRollDice?: () => void;
    onEndTurn?: () => void;
    onBuyProperty?: () => void;
    onPayBail?: () => void;
}

const UIGameInterface: React.FC<UIGameInterfaceProps> = ({
                                                             onRollDice,
                                                             onEndTurn,
                                                             onBuyProperty,
                                                             onPayBail
                                                         }) => {
    const game = GameSingleton.getInstance();

    if (!game.gameId) {
        return <div className="game-interface loading">Loading game data...</div>;
    }

    const {
        gameId,
        gameState,
        players,
        currentPlayerIndex
    } = game;

    const currentPlayer = players[currentPlayerIndex];

    return (
        <div className="game-interface compact">
            <div className="header">
                <h3>Monopoly</h3>
                <span className={`status ${gameState.toLowerCase()}`}>
          {GameState[gameState]}
        </span>
            </div>

            <div className="current-player">
                <div className="player-info">
                    <span className="name">{currentPlayer?.playerId}</span>
                    <span className="balance">${currentPlayer?.balance.toLocaleString()}</span>
                </div>
                <div className="position">
                    Pos: {currentPlayer?.position}
                    {currentPlayer?.inJail && <span className="jail">⛓</span>}
                </div>
            </div>

            <div className="action-buttons">
                {!currentPlayer?.inJail && (
                    <button onClick={onRollDice} className="btn dice">
                        Roll Dice
                    </button>
                )}
                {currentPlayer?.inJail && (
                    <button onClick={onPayBail} className="btn bail">
                        Pay Bail ($50)
                    </button>
                )}
                <button onClick={onBuyProperty} className="btn buy" disabled={!currentPlayer}>
                    Buy Property
                </button>
                <button onClick={onEndTurn} className="btn end">
                    End Turn
                </button>
            </div>

            <div className="players-list">
                {players.map((player, index) => (
                    <div
                        key={player.playerId}
                        className={`player-chip ${index === currentPlayerIndex ? 'active' : ''}`}
                        style={{ backgroundColor: `${player.color}20`, borderColor: player.color }}
                    >
                        <span>{player.playerId}</span>
                        <span>${player.balance.toLocaleString()}</span>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UIGameInterface;