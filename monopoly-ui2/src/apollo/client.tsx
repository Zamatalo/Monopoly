import {ApolloClient, HttpLink, InMemoryCache, split} from '@apollo/client';
import {GraphQLWsLink} from '@apollo/client/link/subscriptions';
import {createClient} from 'graphql-ws';
import {getMainDefinition} from '@apollo/client/utilities';

// @ts-ignore
const gatewayHost = (typeof process !== 'undefined' && process.env?.REDIS_HOST) ? process.env.REDIS_HOST : 'localhost';

const httpLink = new HttpLink({
    uri: "http://" + gatewayHost + ":8083/api/v1/graphql",
    // headers: {
    //     'Content-Type': 'application/json',
    // }
});

const wsLink = new GraphQLWsLink(
    createClient({
        url: "ws://" + gatewayHost + ":8083/api/v1/graphql",
        // connectionParams: {}, // No auth params needed
        // retryAttempts: 5,
        // shouldRetry: () => true,
        // connectionAckWaitTimeout: 5000, // 5 seconds
    })
);

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