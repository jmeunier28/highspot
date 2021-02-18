package highspot.parsing;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import highspot.jackson.Changes;
import highspot.jackson.Playlist;
import highspot.jackson.Song;
import highspot.jackson.UpdatedSong;
import highspot.jackson.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);
    private final JsonFactory jsonFactory;
    private final ObjectMapper objectMapper;
    private final ByteArrayOutputStream stream;
    private Changes changes = null;
    private static final TypeReference<List<Playlist>> PLAYLIST_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<User>> USER_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Song>> SONG_LIST_TYPE = new TypeReference<>() {
    };
    private static String OUTPUT_FILE_NAME = "output.json";

    public Parser() {
        this.jsonFactory = new JsonFactory();
        this.objectMapper = new ObjectMapper();
        this.jsonFactory.setCodec(this.objectMapper);
        this.stream = new ByteArrayOutputStream();
    }

    // Used for the purpose of testing only.
    public Parser(final ByteArrayOutputStream stream) {
        this.jsonFactory = new JsonFactory();
        this.objectMapper = new ObjectMapper();
        this.jsonFactory.setCodec(this.objectMapper);
        this.stream = stream;
    }

    @VisibleForTesting
    public Changes getChanges() {
        return changes;
    }

    /**
     * Takes input from changes.json and de-serializes to a {@link Changes} POJO
     *
     * @param in
     * @throws Exception
     */
    public void parseAndStoreChanges(InputStream in) throws IOException, JsonParseException {
        final ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info("Deserialize-ing changes file");
        changes = objectMapper.readValue(in, Changes.class);
        LOGGER.info("Desired changes loaded into memory...");
    }

    /**
     * Take mixtape.json input and apply all changes as dictated by changes.json
     *
     * @param in
     * @throws Exception
     */
    public void parseAndUpdate(InputStream in) throws IOException, JsonParseException, RuntimeException {
        if (changes == null) {
            LOGGER.error("ERROR: Changes object in null; Exiting");
            throw new RuntimeException("ERROR: No changes have been recorded at this time, program exiting");
        }
        LOGGER.info("Attempting to parse and update mixtape.json");
        // Uses a jackson stream parser in order to de-serialize + write to the output
        // stream at the same time. Unfortunately, this still requires reading + de-serializing
        // the entire mixtape.json file even though we only want to apply changes to a portion of it.
        try (JsonParser parser = jsonFactory.createParser(in);
             JsonGenerator jsonGenerator = jsonFactory.createGenerator(stream, JsonEncoding.UTF8)) {
            parser.nextToken();
            assertCurrentToken(parser, JsonToken.START_OBJECT, String.format("ERROR: malformed JSON; expecting \'{\', but saw %s", parser.getCurrentToken()));
            jsonGenerator.writeStartObject(); // start writing output
            while (!parser.isClosed()) {
                parser.nextToken();
                final String name = parser.getCurrentName();
                parser.nextToken();
                if (name == null) {
                    continue;
                }
                switch (name) {
                    case "users":
                        final List<User> users = parser.readValueAs(USER_LIST_TYPE);
                        jsonGenerator.writeObjectField("users", users);
                        break;
                    case "playlists":
                        final List<Playlist> updatedPlaylist = applyBatchChanges(parser);
                        jsonGenerator.writeObjectField("playlists", updatedPlaylist);
                        break;
                    case "songs":
                        final List<Song> songs = parser.readValueAs(SONG_LIST_TYPE);
                        jsonGenerator.writeObjectField("songs", songs);
                        break;
                    default:
                        throw new JsonParseException(parser, "Reached unexpected part of mixtape.json.. bailing");
                }
            }
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();

            LOGGER.info("Writing applied changes to output.json");
            try (OutputStream outputStream = new FileOutputStream(OUTPUT_FILE_NAME)) {
                stream.writeTo(outputStream);
            }
        }
    }

    /**
     * Method to apply batch changes, which were taken from the changes.json file and stored in a Changes object
     * Returns a List of updated Playlist POJOs, which will be re-serialized back to json
     *
     * @param parser
     * @return a List<{@link Playlist}>
     * @throws Exception
     */
    @VisibleForTesting
    protected List<Playlist> applyBatchChanges(JsonParser parser) throws IOException {
        final List<Playlist> currState = parser.readValueAs(PLAYLIST_LIST_TYPE);
        ImmutableList.Builder<Playlist> playlistBuilder = new ImmutableList.Builder<>();
        for (Playlist playlist : currState) {
            // first apply removals by skipping them
            if (changes.getRemovePlaylists().contains(playlist.getId())) {
                continue;
            }
            // second create additions
            for (UpdatedSong songs : changes.getAddSongs()) {
                if (songs.getId().equals(playlist.getId())) {
                    // Jackson makes you choose whether to make deserialization code
                    // easier by just making stuff arrays or using type references to get
                    // real Lists back... I chose to stick with the arrays here since these were nested.
                    // makes this code a little gross... :(
                    final String[] currSongList = playlist.getSongId();
                    final String[] newSongs = songs.getSongIds();
                    final String[] concatSongs = new String[currSongList.length + newSongs.length];
                    for (int i = 0; i < currSongList.length; i++) {
                        concatSongs[i] = currSongList[i];
                    }
                    for (int i = 0; i < newSongs.length; i++) {
                        concatSongs[i + currSongList.length] = newSongs[i];
                    }
                    playlistBuilder.add(new Playlist(playlist.getId(), playlist.getUserId(), concatSongs));
                }
            }
        }
        // add any new playlists
        playlistBuilder.addAll(changes.getAddPlaylists());
        return playlistBuilder.build();
    }

    /**
     * Convenience method for throwing JsonParseExceptions
     *
     * @param parser
     * @param expected
     * @param message
     * @throws JsonParseException
     */
    private static void assertCurrentToken(final JsonParser parser, final JsonToken expected, final String message) throws JsonParseException {
        final JsonToken current = parser.getCurrentToken();
        if (current != expected) {
            LOGGER.error(String.format("ERROR: %s got %s", message, current, parser.getCurrentLocation()));
            throw new JsonParseException(parser, message + " got " + current, parser.getCurrentLocation());
        }
    }
}
