import { ApolloClient, InMemoryCache, split, HttpLink } from '@apollo/client';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { getMainDefinition } from '@apollo/client/utilities';

const httpLink = new HttpLink({
    uri: 'http://localhost:8081/api/v1/graphql', // твой Spring Boot сервер
});

const wsLink = new GraphQLWsLink(createClient({
    url: 'ws://localhost:8081/api/v1/graphql', // WebSocket endpoint
}));

// Используем split для маршрутизации: query/mutation через HTTP, subscription через WS
const splitLink = split(
    ({ query }) => {
        const definition = getMainDefinition(query);
        return (
            definition.kind === 'OperationDefinition' &&
            definition.operation === 'subscription'
        );
    },
    wsLink,
    httpLink
);

export const apolloClient = new ApolloClient({
    link: splitLink,
    cache: new InMemoryCache(),
});
