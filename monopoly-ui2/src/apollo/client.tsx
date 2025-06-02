import { ApolloClient, InMemoryCache, HttpLink, split } from '@apollo/client';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { getMainDefinition } from '@apollo/client/utilities';


// HTTP link for queries and mutations
const httpLink = new HttpLink({
    uri: 'http://localhost:8083/api/v1/graphql',
    // headers: {
    //     'Content-Type': 'application/json',
    // }
});

const wsLink = new GraphQLWsLink(
    createClient({
        url: 'ws://localhost:8083/api/v1/graphql',
        // connectionParams: {}, // No auth params needed
        // retryAttempts: 5,
        // shouldRetry: () => true,
        // connectionAckWaitTimeout: 5000, // 5 seconds
    })
);

// Split links based on operation type
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

export const client = new ApolloClient({
    link: splitLink,
    cache: new InMemoryCache(),
    defaultOptions: {
        watchQuery: {
            fetchPolicy: 'network-only',
        },
    }
});