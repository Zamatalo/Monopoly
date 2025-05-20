import React, {useEffect, useState} from "react";
import "../../styles/StartGameDialog.css"
import {GameState} from "../../components/utils/constants";
import GameSingleton from "../../stores/singletons/GameSingleton";
import {useMutation} from "@apollo/client";
import {START_GAME} from "../../graphql/queries";


const StartGameDialog: React.FC = () => {
    const [isLoading, setIsLoading] = useState(false);
    const game = GameSingleton.hasInstance() ? GameSingleton.getInstance() : null;
    const [startGame] = useMutation(START_GAME);
    const [playerFull, setPlayerFull] = useState(false);

    const handleStartGame = () => {
        startGame({variables: {gameId: game?.gameId}})

    }

    const getStatusClass = () => {
        switch (game?.gameState) {
            case GameState.STARTED:
                return "playing";
            case GameState.FINISHED:
                return "ended";
            case GameState.IN_PROGRESS:
                return "waiting";
        }
    };


    return (

        <div className="compact-game-dialog">
            <div className="dialog-header">
                <h3>Game Lobby</h3>
                <span className={`status ${getStatusClass()}`}>
          {game?.gameState}
        </span>
            </div>

            <div className="players-info">
                <div className="players-count">
                    <span>Players:</span>
                    <span className="count">
            {game?.players.length}/{4}
          </span>
                </div>
            </div>

            <div className="players-list">
                {game?.players.map(player => (
                    <div
                        key={player.playerId}
                        // className={`player-tag ${player ? 'bot' : ''}`}
                    >
                        {player.playerName}
                        {/*{player.isBot && <span className="bot-icon">🤖</span>}*/}
                    </div>
                ))}
            </div>

            <div className="action-buttons">
                <button
                    className="btn start-btn"
                    onClick={handleStartGame}
                    disabled={game?.players.length !== 4}
                >
                    {isLoading ? "Starting..." : "Start Game"}
                </button>

            </div>
        </div>
    );
};

export default StartGameDialog;