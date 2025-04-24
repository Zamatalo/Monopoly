import React from 'react';
import '../../styles/ColorPickerDialog.css';
import {PlayerColor} from "../../components/utils/constants";

interface ColorPickerDialogProps {
    opened: boolean;
    onClose: () => void;
    onSelect: (color: PlayerColor) => void;
    takenColors: PlayerColor[];
}

const colorOptions: { color: PlayerColor; label: string; hex: string }[] = [
    { color: PlayerColor.PLAYER_RED, label: 'Red', hex: '#e74c3c' },
    { color: PlayerColor.PLAYER_BLUE, label: 'Blue', hex: '#3498db' },
    { color: PlayerColor.PLAYER_GREEN, label: 'Green', hex: '#2ecc71' },
    { color: PlayerColor.PLAYER_YELLOW, label: 'Yellow', hex: '#f1c40f' },
];

export const ColorPickerDialog: React.FC<ColorPickerDialogProps> = ({ opened, onClose, onSelect, takenColors }) => {
    if (!opened) return null;

    return (
        <div className="color-dialog-backdrop">
            <div className="color-dialog">
                <h2>Select your color</h2>
                <div className="color-options">
                    {colorOptions.map(({ color, label, hex }) => (
                        <button
                            key={color}
                            className="color-button"
                            style={{
                                backgroundColor: hex,
                                opacity: takenColors.includes(color) ? 0.4 : 1,
                                cursor: takenColors.includes(color) ? 'not-allowed' : 'pointer',
                            }}
                            disabled={takenColors.includes(color)}
                            onClick={() => onSelect(color)}
                        >
                            {label}
                        </button>
                    ))}
                </div>
                <button className="close-button" onClick={onClose}>
                    Cancel
                </button>
            </div>
        </div>
    );
};
