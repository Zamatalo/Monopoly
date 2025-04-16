import React from 'react';
import {Button} from "@vaadin/react-components/Button.js";
import {Icon} from "@vaadin/react-components/Icon.js";
import "@vaadin/icons";
import {GameDTO} from "Frontend/components/objects/GameDTO";
import {PlayerDTO} from "Frontend/components/objects/PlayerDTO";
import "../themes/my-theme/GameInterface.css";

interface GameInterfaceProps {
    currentGame: GameDTO;
    currentPlayer: PlayerDTO;
    onRollDice: () => void;
    onEndTurn: () => void;
    onBuyProperty: () => void;
}

function getCurrentPlayer(game: GameDTO) {
    return game.players[game.currentPlayerIndex];
}

const GameInterface: React.FC<GameInterfaceProps> = ({
                                                         currentGame,
                                                         currentPlayer,
                                                         onRollDice,
                                                         onEndTurn,
                                                         onBuyProperty
                                                     }) => {
    return (
        <div className="game-interface">
            <div className="player-info">
                <div className="player-color" style={{backgroundColor: currentPlayer.color}}></div>
                <div className="player-details">
                    <h3>{currentPlayer.playerId}</h3>
                    <p>Balance: ${currentPlayer.balance}</p>
                    <p>Position: {currentPlayer.position}</p>
                    {currentPlayer.inJail && <p className="jail-status">In Jail</p>}
                </div>
            </div>

            {/* Game Controls */}
            <div className="game-controls">
                <Button
                    theme="primary"
                    onClick={onRollDice}
                    disabled={getCurrentPlayer(currentGame).playerId != currentPlayer.playerId}
                >
                    <Icon icon="vaadin:dice" slot="prefix"/>
                    Roll Dice
                </Button>

                <Button
                    theme="primary"
                    onClick={onEndTurn}
                    disabled={getCurrentPlayer(currentGame).playerId != currentPlayer.playerId}
                >
                    <Icon icon="vaadin:arrow-forward" slot="prefix"/>
                    End Turn
                </Button>

                <Button
                    theme="primary"
                    onClick={onBuyProperty}
                   // disabled={!currentGame.canBuyProperty(currentPlayer)}
                >
                    <Icon icon="vaadin:money" slot="prefix"/>
                    Buy Property
                </Button>
            </div>

            {/*{currentGame.currentProperty && (*/}
            {/*    <div className="property-info">*/}
            {/*        <h4>{currentGame.currentProperty.name}</h4>*/}
            {/*        <p>Price: ${currentGame.currentProperty.cost}</p>*/}
            {/*        <p>Rent: ${currentGame.currentProperty.rent}</p>*/}
            {/*        {currentGame.currentProperty.owner && (*/}
            {/*            <p>Owner: {currentGame.currentProperty.owner.name}</p>*/}
            {/*        )}*/}
            {/*    </div>*/}
            {/*)}*/}

            <div className="game-log">
                <h4>Game Log</h4>
                <div className="log-entries">
                    {/*{currentGame.logEntries.map((entry, index) => (*/}
                    {/*    <p key={index}>{entry}</p>*/}
                    {/*))}*/}
                </div>
            </div>
        </div>
    );
};

export default GameInterface;