import {gql} from '@apollo/client';

export const GET_ACTIVE_GAMES = gql`
    query GetActiveGames {
        getActiveGames {
            gameId
            currentPlayerIndex
            gameState
            createdTime
            players {
                playerId
                color
                balance
                position
                name
                inJail
            }
        }
    }
`;

export const GET_FIND_BY_ID = gql`
    query FindGameById($gameId: ID!) {
        findGameById(id:$gameId ) {
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

export const GAME_UPDATED_SUBSCRIPTION = gql`
    subscription GameUpdated($gameId: ID!) {
        gameUpdated(gameId: $gameId) {
            gameId
            gameState
            currentPlayerIndex
            players {
                playerId
                color
                name
                balance
                position
                inJail
                ownedProperties {
                    propertyName
                    cost
                    rent
                    upgradable
                }
            }
        }
    }
`;


export const JOIN_GAME_MUTATION = gql`
    mutation JoinGame($gameId: ID!, $playerName: String!,$playerColor: PlayerColors!) {
        joinToGame(gameId: $gameId, playerName: $playerName, playerColor: $playerColor) {
            gameId
            gameState
            currentPlayerIndex
            createdTime
            players {
                playerId
                color
                name
                balance
                position
                inJail
                ownedProperties {
                    propertyName
                    cost
                    rent
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
                name
                balance
                position
                inJail
                ownedProperties {
                    propertyName
                    cost
                    rent
                    upgradable
                }
            }
        }
    }
`;



