import {gql} from '@apollo/client';

export const GAME_UPDATED_SUBSCRIPTION = gql`
    subscription GameUpdated {
        gameUpdated(gameId: "550e8400-e29b-41d4-a716-446655440000") {
            players {
                playerId
                color
                balance
                position
                ownedProperties {
                    propertyName
                    cost
                    rent
                    upgradable
                }
            }
            gameState
            currentPlayerIndex
            gameId
        }
    }

`;

export const ROLL_DICE_MUTATION = gql`
    mutation RollDice($gameId: ID!, $playerId: ID!) {
        rollDice(gameId: $gameId, playerId: $playerId) {
            gameId
            players {
                playerId
                color
                balance
                position
            }
            currentPlayerIndex
            gameState
        }
    }
`;

export const GET_FIND_BY_ID = gql`
    query FindGameById {
        findGameById(id: "550e8400-e29b-41d4-a716-446655440000") {
            gameId
            currentPlayerIndex
            gameState
            players {
                playerId
                color
                balance
                position
                ownedProperties {
                    propertyName
                    cost
                    rent
                    upgradable
                }
            }
        }
    }
`