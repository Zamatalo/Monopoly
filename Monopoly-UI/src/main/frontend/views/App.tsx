import React, {useEffect, useRef} from 'react';
import {ApolloClient, ApolloProvider, InMemoryCache, useQuery, useSubscription} from '@apollo/client';
import {GraphQLWsLink} from '@apollo/client/link/subscriptions';
import {createClient} from 'graphql-ws';
import {initThreeJS, loadState} from 'Frontend/components/Main';
import {GAME_UPDATED_SUBSCRIPTION, GET_FIND_BY_ID} from 'Frontend/utils/queries';
import {GameDTO} from 'Frontend/components/GameDTO';
import {PlayerDTO} from 'Frontend/components/PlayerDTO';

const wsLink = new GraphQLWsLink(
    createClient({
        url: 'ws://localhost:8081/api/v1/graphql',
        connectionParams: {},
    })
);

const client = new ApolloClient({
    link: wsLink,
    cache: new InMemoryCache(),
});

function App() {
    const containerRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        if (containerRef.current) {
            initThreeJS(containerRef.current);
        }
    }, []);

    return (
        <ApolloProvider client={client}>
            <GameInitializer gameId="550e8400-e29b-41d4-a716-446655440000"/>
            <GameUpdates/>
            <div ref={containerRef} style={{width: '100%', height: '100%'}}/>
        </ApolloProvider>
    );
}

function GameInitializer({gameId}: { gameId: string }) {
    const {data, loading, error} = useQuery(GET_FIND_BY_ID, {
        variables: {id: gameId},
    });

    useEffect(() => {
        if (data) {
            console.log('Fetched initial game state:', data);

            const gameData = data.findGameById;

            const players = gameData.players.map(
                (player: any) =>
                    new PlayerDTO({
                        playerId: player.playerId,
                        color: player.color,
                        inJail: player.inJail,
                        balance: player.balance,
                        position: player.position,
                        ownedProperties: player.ownedProperties,
                    } as PlayerDTO)
            );

            const game = new GameDTO({
                gameId: gameData.gameId,
                gameState: gameData.gameState,
                players: players,
                currentPlayerIndex: gameData.currentPlayerIndex,
            } as GameDTO);

            loadState(game).then(() => console.log('Initial game state loaded'));
        }
    }, [data]);

    if (loading) return <p>Loading initial game state...</p>;
    if (error) return <p>Error fetching initial game state: {error.message}</p>;

    return null;
}

function GameUpdates() {
    const {data, error} = useSubscription(GAME_UPDATED_SUBSCRIPTION);

    useEffect(() => {
        if (data) {
            const gameData = data.gameUpdated;

            const players = gameData.players.map(
                (player: any) =>
                    new PlayerDTO({
                        playerId: player.playerId,
                        color: player.color,
                        inJail: player.inJail,
                        balance: player.balance,
                        position: player.position,
                        ownedProperties: player.ownedProperties,
                    } as PlayerDTO)
            );

            const newGame = new GameDTO({
                gameId: gameData.gameId,
                gameState: gameData.gameState,
                players: players,
                currentPlayerIndex: gameData.currentPlayerIndex
            } as GameDTO);
            console.log(gameData);
            loadState(newGame).then(() => console.log('Game state updated'));
        }
    }, [data]);

    if (error) return <p>Error: {error.message}</p>;

    return null;
}

export default App;