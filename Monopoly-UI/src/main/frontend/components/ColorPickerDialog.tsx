import React from 'react';
import {Dialog} from '@vaadin/react-components/Dialog.js';
import {Button} from '@vaadin/react-components/Button.js';
import {HorizontalLayout} from '@vaadin/react-components/HorizontalLayout.js';
import {PlayerColor} from 'Frontend/utils/constants';

interface ColorPickerDialogProps {
    opened: boolean;
    onClose: () => void;
    onSelect: (color: PlayerColor) => void;
    takenColors: PlayerColor[]; // уже занятые цвета
}

const colorOptions: { color: PlayerColor; label: string; hex: string }[] = [
    { color: PlayerColor.PLAYER_RED, label: "Red", hex: "#e74c3c" },
    { color: PlayerColor.PLAYER_BLUE, label: "Blue", hex: "#3498db" },
    { color: PlayerColor.PLAYER_GREEN, label: "Green", hex: "#2ecc71" },
    { color: PlayerColor.PLAYER_YELLOW, label: "Yellow", hex: "#f1c40f" },
];

const ColorPickerDialog: React.FC<ColorPickerDialogProps> = ({ opened, onClose, onSelect, takenColors }) => {
    return (
        <Dialog opened={opened} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
            <h3>Select your color</h3>
            <HorizontalLayout theme="spacing padding" style={{ justifyContent: 'center' }}>
                {colorOptions.map(({ color, label, hex }) => (
                    <Button
                        key={color}
                        style={{
                            backgroundColor: hex,
                            color: 'white',
                            borderRadius: '12px',
                            margin: '0 0.5rem',
                            cursor: takenColors.includes(color) ? 'not-allowed' : 'pointer',
                            opacity: takenColors.includes(color) ? 0.5 : 1,
                        }}
                        disabled={takenColors.includes(color)}
                        onClick={() => onSelect(color)}
                    >
                        {label}
                    </Button>
                ))}
            </HorizontalLayout>
        </Dialog>
    );
};

export default ColorPickerDialog;
