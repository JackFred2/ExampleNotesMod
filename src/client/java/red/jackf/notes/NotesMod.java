package red.jackf.notes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.jackfredlib.client.api.toasts.ToastBuilder;
import red.jackf.jackfredlib.client.api.toasts.ToastFormat;
import red.jackf.jackfredlib.client.api.toasts.ToastIcon;
import red.jackf.jackfredlib.client.api.toasts.Toasts;

import java.util.List;

public class NotesMod implements ClientModInitializer {
    public static final String ID = "notesmod";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final KeyMapping OPEN_NOTES = new KeyMapping("notesmod.key.editNote", GLFW.GLFW_KEY_N, "category.notesmod");
    private String currentNote = "";
    private @Nullable Coordinate currentCoordinate = null;

    /**
     * Renders the note on-screen, filling a background so that it encompasses the whole note
     */
    private static void renderNote(GuiGraphics graphics, String contents) {
        List<String> lines = List.of(contents.split("\n"));
        Font font = Minecraft.getInstance().font;

        // Calculate the dimensions that cover our note's lines
        int maxWidth = lines.stream().mapToInt(font::width).max().orElse(0);
        int height = font.lineHeight * lines.size();

        final int x = 10;
        final int y = 10;

        graphics.fill(x - 3, y - 3, x + maxWidth + 3, y + height + 3, 0x40_000000);

        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(Minecraft.getInstance().font, lines.get(i), 10, 10 + font.lineHeight * i, 0xFF_FFFFFF, true);
        }
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_NOTES);

        // Load the note on game connect;
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Coordinate.getCurrent().ifPresent(coordinate -> {
                // Load a note using the given file safe ID
                currentNote = NotesIO.load(coordinate.id());

                // We save the Coordinate as we cannot grab it upon disconnect
                currentCoordinate = coordinate;

                // Send a toast saying we've loaded
                Toasts.INSTANCE.send(ToastBuilder.builder(ToastFormat.DARK, Component.translatable("notesmod.loaded", coordinate.userFriendlyName()))
                        .expiresAfter(2500L)
                        .progressShowsVisibleTime()
                        .withIcon(ToastIcon.modIcon(ID))
                        .build());
            });
        });

        // Save the note on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (currentCoordinate != null) {
                // Save the note using the file safe ID
                NotesIO.save(currentCoordinate.id(), currentNote);

                // Send a toast saying we've saved
                Toasts.INSTANCE.send(ToastBuilder.builder(ToastFormat.DARK, Component.translatable("notesmod.saved", currentCoordinate.userFriendlyName()))
                        .expiresAfter(2500L)
                        .progressShowsVisibleTime()
                        .withIcon(ToastIcon.modIcon(ID))
                        .build());
            }

            currentCoordinate = null;
            currentNote = "";
        });

        // Open the edit note GUI
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.screen == null && client.getOverlay() == null)
                while (OPEN_NOTES.consumeClick()) {
                    Minecraft.getInstance()
                            .setScreen(new EditNoteScreen(currentNote, newText -> this.currentNote = newText));
                }
        });

        // Draw the note
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!currentNote.isBlank()) {
                renderNote(drawContext, currentNote);
            }
        });
    }
}