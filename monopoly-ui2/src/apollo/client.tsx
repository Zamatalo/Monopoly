import {ApolloClient, InMemoryCache} from '@apollo/client';
import {GraphQLWsLink} from '@apollo/client/link/subscriptions';
import {createClient} from 'graphql-ws';

const wsLink = new GraphQLWsLink(
    createClient({
        url: 'ws://localhost:8081/api/v1/graphql',
        connectionParams: {},
    })
);

export const client = new ApolloClient({
    link: wsLink,
    cache: new InMemoryCache(),
    defaultOptions: {
        watchQuery: {
            fetchPolicy: 'cache-and-network',
        },
    },
});


