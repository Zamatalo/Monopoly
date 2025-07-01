import React, {createContext, useContext, useState} from 'react';
import GameNotification from "./GameNotification";

///#TODO: notifications for other players, via websockets i guess?
interface NotificationContextType {
    showNotification: (message: string) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const [notification, setNotification] = useState<{
        message: string;
        show: boolean;
    }>({message: '', show: false});

    const showNotification = (message: string) => {
        setNotification({message, show: true});
        setTimeout(() => setNotification(prev => ({...prev, show: false})), 5000);
    };

    return (
        <NotificationContext.Provider value={{showNotification}}>
            {children}
            {notification.show && (
                <GameNotification
                    message={notification.message}
                    onClose={() => setNotification(prev => ({...prev, show: false}))}
                />
            )}
        </NotificationContext.Provider>
    );
};

export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotification must be used within a NotificationProvider');
    }
    return context;
};