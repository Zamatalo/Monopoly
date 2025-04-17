import React, {StrictMode} from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import { ApolloProvider } from '@apollo/client';
import { apolloClient } from './apollo/client.ts';

import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
    <StrictMode>
        <ApolloProvider client={apolloClient}>
            <App />
        </ApolloProvider>
    </StrictMode>
);
