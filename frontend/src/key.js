////////////////////////////////////////////////////////////////////////////////

export const ARROW_UP = '38';
export const ARROW_DOWN = '40';
export const ARROW_LEFT = '37';
export const ARROW_RIGHT = '39';

export function isArrowKey(keyCode) {
    return (keyCode == ARROW_UP ||
            keyCode == ARROW_DOWN ||
            keyCode == ARROW_LEFT ||
            keyCode == ARROW_RIGHT);
}
