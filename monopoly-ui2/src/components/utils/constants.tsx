export const positions = [
    {x: 9.5, z: 9.5}, {x: 7, z: 9.5}, {x: 5.25, z: 9.5}, {x: 3.65, z: 9.5},
    {x: 1.85, z: 9.5}, {x: 0.0, z: 9.5}, {x: -1.85, z: 9.5}, {x: -3.65, z: 9.5},
    {x: -5.25, z: 9.5}, {x: -7, z: 9.5}, {x: -9.5, z: 9.5}, {x: -9.5, z: 7},
    {x: -9.5, z: 5.25}, {x: -9.5, z: 3.65}, {x: -9.5, z: 1.85}, {x: -9.5, z: 0.0},
    {x: -9.5, z: -1.85}, {x: -9.5, z: -3.65}, {x: -9.5, z: -5.25}, {x: -9.5, z: -7},
    {x: -9.5, z: -9.5}, {x: -7, z: -9.5}, {x: -5.25, z: -9.5}, {x: -3.65, z: -9.5},
    {x: -1.85, z: -9.5}, {x: 0.0, z: -9.5}, {x: 1.85, z: -9.5}, {x: 3.65, z: -9.5},
    {x: 5.25, z: -9.5}, {x: 7, z: -9.5}, {x: 9.5, z: -9.5}, {x: 9.5, z: -7},
    {x: 9.5, z: -5.25}, {x: 9.5, z: -3.65}, {x: 9.5, z: -1.85}, {x: 9.5, z: 0.0},
    {x: 9.5, z: 1.85}, {x: 9.5, z: 3.65}, {x: 9.5, z: 5.25}, {x: 9.5, z: 7}
];

export enum PlayerColor {
    PLAYER_RED = 'PLAYER_RED',
    PLAYER_GREEN = 'PLAYER_GREEN',
    PLAYER_BLUE = 'PLAYER_BLUE',
    PLAYER_YELLOW = 'PLAYER_YELLOW',
}

export const ColorHexMap: Record<any,any> = {
    PLAYER_RED: '#ff4d4d',
    PLAYER_BLUE: '#4d79ff',
    PLAYER_GREEN: '#4dff88',
    PLAYER_YELLOW: '#ffe44d',
};

export enum GameState {
    STARTED = 'STARTED',
    IN_PROGRESS = 'IN_PROGRESS',
    FINISHED = 'FINISHED',
}
export enum PlayerState {
    AWAITING_DECISION='AWAITING_DECISION',
    IDLE = 'IDLE',
}

export enum PlayerActions{
    ROLL_DICE= 'ROLL_DICE',
    BUY_PROPERTY=`BUY_PROPERTY`,
    END_TURN= 'END_TURN',
    START_GAME = `START_GAME`,
    SPECIAL_TILE = `SPECIAL_TILE`
}
export enum GameActions {
    START_GAME="START_GAME",
    END_GAME="END_GAME",
    CREATE_GAME="CREATE_GAME",
    JOIN_TO_GAME="JOIN_TO_GAME",
    ADD_BOT = "ADD_BOT"
}




