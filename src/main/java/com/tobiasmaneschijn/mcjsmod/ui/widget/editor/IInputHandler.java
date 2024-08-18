package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

interface IInputHandler {
    boolean handleKeyPress(int keyCode, int scanCode, int modifiers);
    boolean handleCharTyped(char codePoint, int modifiers);
    boolean handleMouseClick(double mouseX, double mouseY, int button);
    boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY);
}
