import {createServer} from 'http';
import {Server} from 'socket.io';


const httpServer = createServer();
const io = new Server(httpServer, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});


io.on('connection', (socket) => {
    console.log(`Client connected: ${socket.id}`);

    socket.on('throw', (msg, callback) => {
        if (callback) {
            callback("pong");
        }
    });
    socket.on('ping', (callback) => {
        if (callback) {
            callback("pong");
        }
    });
    socket.on('pong', (callback) => {
        if (callback) {
            callback("ping");
        }

    });
    socket.on('error', (err) => {
        console.error('Socket error:', err);
    });
});

httpServer.listen(3000, () => {
    console.log('Server running on http://localhost:3000');
});