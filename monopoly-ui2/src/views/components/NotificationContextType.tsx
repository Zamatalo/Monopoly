import React, { createContext, useContext, useState, useRef } from 'react';
import GameNotification from "./GameNotification";

interface NotificationContextType {
    showNotification: (message: string) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [notification, setNotification] = useState<{
        message: string;
        show: boolean;
    }>({ message: '', show: false });
    const timeoutRef = useRef<number | null>(null);

    const showNotification = (message: string) => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        setNotification({ message, show: true });

        timeoutRef.current = window.setTimeout(() => {
            setNotification(prev => ({ ...prev, show: false }));
            timeoutRef.current = null;
        }, 5000);
    };

    return (
        <NotificationContext.Provider value={{ showNotification }}>
            {children}
            {notification.show && (
                <GameNotification message={notification.message} />
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