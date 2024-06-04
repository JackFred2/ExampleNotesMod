package red.jackf.notes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class EditNoteScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int PAD = 20;

    private final String currentText;
    private final Consumer<String> onUpdate;

    protected EditNoteScreen(String currentText, Consumer<String> onUpdate) {
        super(Text.translatable("notesmod.editingNote"));
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

        var editbox = new EditBoxWidget(MinecraftClient.getInstance().textRenderer, x, y, width, height, Text.empty(), Text.empty());
        editbox.setChangeListener(this.onUpdate);
        editbox.setText(currentText);

        this.addDrawableChild(editbox);

        this.setInitialFocus(editbox);
    }
}
