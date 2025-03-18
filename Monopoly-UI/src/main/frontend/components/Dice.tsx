import React from 'react';
import {useMutation} from '@apollo/client';
import {ROLL_DICE_MUTATION} from '../utils/queries';

const Dice: React.FC<{ gameId: string; playerId: string }> = ({gameId, playerId}) => {
    const [rollDice] = useMutation(ROLL_DICE_MUTATION);

    const handleRollDice = async () => {
        try {
            await rollDice({
                variables: {
                    gameId,
                    playerId,
                },
            });
        } catch (error) {
            console.error('Error rolling dice:', error);
        }
    };

    return (
        <div>
            <button onClick={handleRollDice}>Roll Dice</button>
        </div>
    );
};

export default Dice;