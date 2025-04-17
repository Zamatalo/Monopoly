import {BrowserRouter, Route, Routes} from 'react-router-dom';
import {LobbyView} from './views/LobbyView';
import {GameView} from "./views/GameView";
import {ApolloProvider} from "@apollo/client";
import {client} from "./apollo/client";

function App() {
    return (
        <ApolloProvider client={client}>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LobbyView />} />
                <Route path="/game/:gameId" element={<GameView/>}/>
            </Routes>
        </BrowserRouter>
        </ApolloProvider>
    );
}

export default App;
