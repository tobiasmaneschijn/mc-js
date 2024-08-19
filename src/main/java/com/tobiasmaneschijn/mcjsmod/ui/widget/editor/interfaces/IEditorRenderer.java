package com.tobiasmaneschijn.mcjsmod.ui.widget.editor.interfaces;

import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.TextEditor;
import net.minecraft.client.gui.GuiGraphics;

public interface IEditorRenderer {
    void render(GuiGraphics guiGraphics, TextEditor editor);
}
