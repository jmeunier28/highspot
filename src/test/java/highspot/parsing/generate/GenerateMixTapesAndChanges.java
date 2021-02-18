package highspot.parsing.generate;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import highspot.jackson.Playlist;
import highspot.jackson.Song;
import highspot.jackson.UpdatedSong;
import highspot.jackson.User;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * class to generate giant JSON files to test with
 */
public class GenerateMixTapesAndChanges {
    private static final String CHANGES_FILE_NAME = "big-changes.json";
    private static final String MIXTAPE_FILE_NAME = "big-mixtape.json";
    private final static JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    // create some big json blobs to test with
    public static void main(final String[] args) {
        jsonFactory.setCodec(objectMapper);
        try {
            createBigMixtape();
            createBigChanges();
        } catch (Exception exe) {
            // bad
            exe.printStackTrace();
        }
    }

    private static void createBigMixtape() throws Exception {
        try (final JsonGenerator jsonGenerator = jsonFactory.createGenerator(stream, JsonEncoding.UTF8)) {
            final List<User> userList = Lists.newArrayList();
            final List<Playlist> playlistList = Lists.newArrayList();
            final List<Song> songList = Lists.newArrayList();
            for (int i = 0; i < 100000; i++) {
                userList.add(new User(String.valueOf(i + 2), String.format("the-best-name-%d", i)));
                playlistList.add(new Playlist(String.valueOf(i), String.valueOf(i + 2), new String[]{String.valueOf(i), String.valueOf(i + 3), String.valueOf(i + 6)}));
                songList.add(new Song(String.valueOf(i), String.format("artist-%d", i), String.format("title-%d", i)));
            }

            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("users", userList);
            jsonGenerator.writeObjectField("playlists", playlistList);
            jsonGenerator.writeObjectField("songs", songList);
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();
            try (OutputStream outputStream = new FileOutputStream(MIXTAPE_FILE_NAME)) {
                stream.writeTo(outputStream);
            }
        }
    }

    private static void createBigChanges() throws Exception {
        try (final JsonGenerator jsonGenerator = jsonFactory.createGenerator(stream, JsonEncoding.UTF8)) {
            final List<String> removeList = Lists.newArrayList();
            final List<Playlist> addPlaylist = Lists.newArrayList();
            final List<UpdatedSong> addSongList = Lists.newArrayList();
            for (int i = 0; i < 100000; i++) {
                if (i % 7 == 0) {
                    // remove every 7th song
                    removeList.add(String.valueOf(i));
                }

                if (i % 25 == 0) {
                    addPlaylist.add(new Playlist(String.valueOf(i + 100000), String.valueOf(i + 100000 + 2), new String[]{String.valueOf(i), String.valueOf(i + 3), String.valueOf(i + 6)}));
                }

                if (i % 100 == 0) {
                    addSongList.add(new UpdatedSong(String.valueOf(i), new String[]{String.valueOf(i + 8), String.valueOf(i + 5), String.valueOf(i + 1)}));
                }
            }

            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("remove", removeList);
            jsonGenerator.writeFieldName("add");
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("playlists", addPlaylist);
            jsonGenerator.writeObjectField("songs", addSongList);
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();
            try (OutputStream outputStream = new FileOutputStream(CHANGES_FILE_NAME)) {
                stream.writeTo(outputStream);
            }
        }
    }

}
