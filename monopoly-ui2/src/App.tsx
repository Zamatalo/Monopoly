import {BrowserRouter, Navigate, Route, Routes} from 'react-router';
import {LobbyView} from './views/LobbyView';
import {GameView} from "./views/GameView";
import {ApolloProvider} from "@apollo/client";
import {client} from "./apollo/client";


function App() {
    return (
        <ApolloProvider client={client}>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<LobbyView/>}/>
                    <Route path="/game/:gameId" element={<GameView/>}/>
                    <Route path="*" element={<Navigate to="/"/>}/>
                </Routes>
            </BrowserRouter>
        </ApolloProvider>
    );
}

export default App;