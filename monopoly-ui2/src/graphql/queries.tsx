import {gql} from '@apollo/client';

/**
 * Get all active Games
 */
export const GET_ACTIVE_GAMES = gql`
    query GetAllGames {
        getAllGames {
            gameId
            currentPlayerIndex
            gameState
            createdTime
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
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
            inJail_Turns
            isBot
            playerState
            playerActions
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
            createdTime
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
                ownedProperties {
                    displayName
                    boardPosition
                    cost
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
            currentPlayerIndex
            gameState
            createdTime
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
                ownedProperties {
                    displayName
                    boardPosition
                    cost
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
            currentPlayerIndex
            gameState
            createdTime
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
                ownedProperties {
                    displayName
                    boardPosition
                    cost
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
            currentPlayerIndex
            gameState
            createdTime
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
                ownedProperties {
                    displayName
                    boardPosition
                    cost
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
            currentPlayerIndex
            gameState
            createdTime
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
                ownedProperties {
                    displayName
                    boardPosition
                    cost
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
            gameId
            pos_dice1 {
                x
                y
                z
            }
            rot_dice1 {
                x
                y
                z
                w
            }
            pos_dice2 {
                x
                y
                z
            }
            rot_dice2 {
                x
                y
                z
                w
            }
        }
    }
`

/**
 * Buy Property for Current Player for specific game
 */
export const BUY_PROPERTY_MUTATION = gql`
    mutation BuyPropertyForPlayer($gameId: ID!,$playerId: ID!) {
        buyPropertyForPlayer(gameId: $gameId,playerId: $playerId) {
            displayName
            isOwned
            boardPosition
            upgradable
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
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
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

export const ADD_BOT = gql`
    mutation AddBotToGame($gameId: ID!) {
        addBotToGame(gameId: $gameId) {
            gameId
            currentPlayerIndex
            gameState
            createdTime
            gameActions
            players {
                playerId
                playerName
                color
                balance
                position
                inJail_Turns
                isBot
                playerState
                playerActions
                ownedProperties {
                    displayName
                    boardPosition
                    isOwned
                    cost
                    upgradable
                }
            }
        }
    }
`
export const END_TURN = gql`
    mutation EndTurn($gameId: ID!,$playerId: ID!) {
        endTurn(gameId: $gameId, playerId: $playerId) {
            gameId
        }
    }
`
export const SPECIAL_TILE = gql`
    mutation ResolveSpecialTile($gameId: ID!,$playerId: ID!) {
        resolveSpecialTile(gameId: $gameId, playerId: $playerId){
            text
            effect
            amount
        }
    }
`