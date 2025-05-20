import {gql} from '@apollo/client';

/**
 * Get all active Games
 */
export const GET_ACTIVE_GAMES = gql`
    query GetActiveGames {
        getActiveGames {
            gameId
            currentPlayerIndex
            gameState
            createdTime
            players {
                playerId
                playerName
                color
                balance
                position
                inJail
            }
        }
    }
`;
/**
 * Get PlayerDTO from playerID
 */
export const GET_PLAYER = gql`
    query GetPlayer($playerId: ID!) {
        getPlayer(playerId: $playerId) {
            playerId
            playerName
            color
            balance
            position
            inJail
        }
    }

`

/**
 * Get GameDTO from gameId
 */
export const GET_FIND_BY_ID = gql`
    query FindGameById($gameId: ID!) {
        findGameById(gameId:$gameId ) {
            gameId
            currentPlayerIndex
            gameState
            players {
                playerId
                playerName
                color
                balance
                position
                ownedProperties {
                    displayName
                    cost
                    boardPosition
                    upgradable
                }
            }
        }
    }
`

export const GAME_UPDATED_SUBSCRIPTION = gql`
    subscription GameUpdated($gameId: ID!) {
        gameUpdated(gameId: $gameId) {
            gameId
            gameState
            currentPlayerIndex
            players {
                playerId
                color
                playerName
                balance
                position
                inJail
                ownedProperties {
                    displayName
                    cost
                    boardPosition
                    upgradable
                }
            }
        }
    }
`;

export const JOIN_GAME_MUTATION = gql`
    mutation JoinGame($gameId: ID!, $playerName: String!,$playerColor: PlayerColors!,$playerId:ID!) {
        joinToGame(gameId: $gameId, playerName: $playerName, playerColor: $playerColor,playerId:$playerId) {
            gameId
            gameState
            currentPlayerIndex
            createdTime
            players {
                playerId
                color
                playerName
                balance
                position
                inJail
                ownedProperties {
                    displayName
                    cost
                    boardPosition
                    upgradable
                }
            }
        }
    }
`;

export const CREATE_GAME_MUTATION = gql`
    mutation CreateGame {
        createNewGame {
            gameId
            gameState
            currentPlayerIndex
            createdTime
            players {
                playerId
                color
                playerName
                balance
                position
                inJail
                ownedProperties {
                    displayName
                    cost
                    boardPosition
                    upgradable
                }
            }
        }
    }
`;

export const GET_GAME_BY_PLAYER_ID = gql`
    query findGameByPlayerId($playerId: ID!) {
        findGameByPlayerId(playerId:$playerId) {
            gameId
            gameState
            currentPlayerIndex
            createdTime
            players {
                playerId
                color
                playerName
                balance
                position
                inJail
                ownedProperties {
                    displayName
                    cost
                    boardPosition
                    upgradable
                }
            }
        }
    }
`;

export const ROLL_DICE = gql`
    mutation RollDice($gameId: ID!, $playerId: ID!) {
        rollDice(gameId: $gameId, playerId: $playerId)
    }
`;

export const DICE_UPDATED_SUBSCRIPTION = gql`
    subscription DiceUpdated($gameId: ID!) {
        diceUpdated(gameId: $gameId) {
            pos
            rot
        }
    }
`

/**
 * Buy Property for Current Player for specific game
 */
export const BUY_PROPERTY_MUTATION = gql`
    mutation BuyPropertyForPlayer($gameId: ID!) {
        buyPropertyForPlayer(gameId: $gameId) {
            gameId
            currentPlayerIndex
            gameState
            createdTime
            players {
                playerId
                playerName
                color
                balance
                position
                inJail
                ownedProperties {
                    cost
                    upgradable
                    displayName
                    boardPosition
                }
            }
        }
    }
`

export const START_GAME = gql(`
    mutation StartGame($gameId: ID!) {
        startGame(gameId: $gameId) {
            gameId
            currentPlayerIndex
            gameState
            createdTime
            players {
                playerId
                playerName
                color
                balance
                position
                inJail
                ownedProperties {
                    displayName
                    boardPosition
                    cost
                    upgradable
                }
            }
        }
    }
`);

export const GET_AVAILABLE_ACTIONS = gql`
    query GetPossibleCurrentPlayerActions($gameId: ID!) {
        getPossibleCurrentPlayerActions(gameId:$gameId)
    }
`;