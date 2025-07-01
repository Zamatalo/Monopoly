import React from 'react';
import '../../styles/gameNotification.css';


interface GameNotificationProps {
    message: string;
    onClose: () => void;
}

const GameNotification: React.FC<GameNotificationProps> = ({
                                                               message,
                                                               onClose,
                                                           }) => {
    const formattedMessage = message.split('\n').map((line, i) => (
        <React.Fragment key={i}>
            {line}
            <br />
        </React.Fragment>
    ));

    return (
        <div className={`notification`}>
            <div className="notification-content">
                {formattedMessage}
            </div>
        </div>
    );
};

export default GameNotification;