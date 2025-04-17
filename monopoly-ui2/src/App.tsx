import { BrowserRouter, Routes, Route } from 'react-router-dom';
import {LobbyView} from './views/LobbyView';

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LobbyView />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
