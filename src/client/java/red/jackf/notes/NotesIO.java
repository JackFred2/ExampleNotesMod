package red.jackf.notes;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class NotesIO {
    // <game instance>/notesmod/
    private static final Path DIRECTORY = FabricLoader.getInstance().getGameDir().resolve(NotesMod.ID);

    private static Path getPath(String id) {
        return DIRECTORY.resolve(id + ".txt");
    }

    public static String load(String id) {
        Path path = getPath(id);

        if (Files.exists(path)) {
            try {
                return PathUtils.readString(path, StandardCharsets.UTF_8);
            } catch (IOException e) {
                NotesMod.LOGGER.error("Error loading {}", id, e);
            }
        }

        return "";
    }

    public static void save(String id, String contents) {
        Path path = getPath(id);

        if (contents.isBlank()) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                NotesMod.LOGGER.error("Error deleting {}", id, e);
            }
        } else {
            try {
                PathUtils.createParentDirectories(path);
                PathUtils.writeString(path, contents, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            } catch (IOException e) {
                NotesMod.LOGGER.error("Error saving {}", id, e);
            }
        }
    }
}
