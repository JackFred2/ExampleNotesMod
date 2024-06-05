package red.jackf.notes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class EditNoteScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int PAD = 20;

    private final String currentText;
    private final Consumer<String> onUpdate;

    protected EditNoteScreen(String currentText, Consumer<String> onUpdate) {
        super(Component.translatable("notesmod.editingNote"));
        this.currentText = currentText;
        this.onUpdate = onUpdate;
    }

    @Override
    protected void init() {
        super.init();

        final int left = (this.width - WIDTH) / 2;
        final int top = (this.height - HEIGHT) / 2;

        int x = left + PAD;
        int y = top + PAD;
        int width = WIDTH - 2 * PAD;
        int height = HEIGHT - 2 * PAD;

        var editbox = new MultiLineEditBox(Minecraft.getInstance().font, x, y, width, height, Component.empty(), Component.empty());
        editbox.setValueListener(this.onUpdate);
        editbox.setValue(currentText);

        this.addRenderableWidget(editbox);

        this.setInitialFocus(editbox);
    }
}
