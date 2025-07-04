import React from 'react';
import '../../styles/gameNotification.css';

interface GameNotificationProps {
    message: string;
}

const GameNotification: React.FC<GameNotificationProps> = ({ message }) => {
    const formattedMessage = message
        .split('\n')
        .map((line, i) => (
            <React.Fragment key={i}>
                {line}
                <br />
            </React.Fragment>
        ));


    const extractImageName = (msg: string): string | null => {
        const chestMatch = msg.includes("Chest");
        const chanceMatch = msg.includes("Chance");

        if (chestMatch||chanceMatch) {
            return msg;
        }
        return null;
    };

    const imageName = extractImageName(message);
    const effectImage = imageName ? `/assets/specialEffects/${imageName}.png` : null;

    // console.log('Notification debug:', {
    //     message,
    //     imageName,
    //     effectImage
    // });

    return (
        <div className="notification">
            <div className="notification-content">
                {effectImage && imageName ? (
                    <img
                        src={effectImage}
                        alt={imageName}
                    />
                ) : (
                    <strong>{formattedMessage}</strong>
                )}
            </div>
        </div>
    );
};

export default GameNotification;