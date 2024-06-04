package red.jackf.notes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
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

    private static final KeyBinding OPEN_NOTES = new KeyBinding("notesmod.key.editNote", GLFW.GLFW_KEY_N, "category.notesmod");
    private String currentNote = "";
    private @Nullable Coordinate currentCoordinate = null;

    @Override
    public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(OPEN_NOTES);

        // Load the note on game connect;
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Coordinate.getCurrent().ifPresent(coordinate -> {
                currentNote = NotesIO.load(coordinate.id());

                // we save the Coordinate for when we disconnect, as we cannot grab the one we were on at that point
                currentCoordinate = coordinate;

                // Send a toast saying we've loaded
                Toasts.INSTANCE.send(ToastBuilder.builder(ToastFormat.DARK, Text.translatable("notesmod.loaded", coordinate.userFriendlyName()))
                        .expiresAfter(2500L)
						.progressShowsVisibleTime()
                        .withIcon(ToastIcon.modIcon(ID))
                        .build());
            });
        });

        // Save the note on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (currentCoordinate != null) {
                NotesIO.save(currentCoordinate.id(), currentNote);

                // Send a toast saying we've saved
				Toasts.INSTANCE.send(ToastBuilder.builder(ToastFormat.DARK, Text.translatable("notesmod.saved", currentCoordinate.userFriendlyName()))
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
            if (client.currentScreen == null && client.getOverlay() == null)
                while (OPEN_NOTES.wasPressed()) {
                    MinecraftClient.getInstance().setScreen(new EditNoteScreen(currentNote, newText -> this.currentNote = newText));
                }
        });

        // Draw the note
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!currentNote.isBlank()) {
                renderNote(drawContext, currentNote);
            }
        });
    }

    private static void renderNote(DrawContext context, String contents) {
        List<String> lines = List.of(contents.split("\n"));
        TextRenderer text = MinecraftClient.getInstance().textRenderer;

        // Calculate the dimensions that cover our note's lines
        int maxWidth = lines.stream().mapToInt(text::getWidth).max().orElse(0);
        int height = text.fontHeight * lines.size();

        final int x = 10;
        final int y = 10;

        context.fill(x - 3, y - 3, x + maxWidth + 3, y + height + 3, 0x40_000000);

        for (int i = 0; i < lines.size(); i++) {
            context.drawText(MinecraftClient.getInstance().textRenderer, lines.get(i), 10, 10 + text.fontHeight * i, 0xFF_FFFFFF, true);
        }
    }
}