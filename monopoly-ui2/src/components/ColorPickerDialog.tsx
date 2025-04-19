// import React from 'react';
// import {PlayerColor} from "./utils/constants";
//
// interface ColorPickerDialogProps {
//     opened: boolean;
//     onClose: () => void;
//     onSelect: (color: PlayerColor) => void;
//     takenColors: PlayerColor[];
// }
//
// const colorOptions: { color: PlayerColor; label: string; hex: string }[] = [
//     { color: PlayerColor.PLAYER_RED, label: 'Red', hex: '#e74c3c' },
//     { color: PlayerColor.PLAYER_BLUE, label: 'Blue', hex: '#3498db' },
//     { color: PlayerColor.PLAYER_GREEN, label: 'Green', hex: '#2ecc71' },
//     { color: PlayerColor.PLAYER_YELLOW, label: 'Yellow', hex: '#f1c40f' },
// ];
//
// const ColorPickerDialog: React.FC<ColorPickerDialogProps> = ({
//                                                                  opened,
//                                                                  onClose,
//                                                                  onSelect,
//                                                                  takenColors,
//                                                              }) => {
//     return (
//         <Dialog open={opened} onClose={onClose}>
//             <DialogTitle>Select your color</DialogTitle>
//             <DialogContent>
//                 <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
//                     {colorOptions.map(({ color, label, hex }) => (
//                         <Button
//                             key={color}
//                             style={{
//                                 backgroundColor: hex,
//                                 color: 'white',
//                                 borderRadius: '12px',
//                                 cursor: takenColors.includes(color) ? 'not-allowed' : 'pointer',
//                                 opacity: takenColors.includes(color) ? 0.5 : 1,
//                                 padding: '10px 20px',
//                             }}
//                             disabled={takenColors.includes(color)}
//                             onClick={() => onSelect(color)}
//                         >
//                             <Typography variant="button">{label}</Typography>
//                         </Button>
//                     ))}
//                 </div>
//             </DialogContent>
//             <DialogActions>
//                 <Button onClick={onClose} color="primary">
//                     Close
//                 </Button>
//             </DialogActions>
//         </Dialog>
//     );
// };
//
// export default ColorPickerDialog;
