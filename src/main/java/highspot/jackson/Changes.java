package highspot.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.List;

/**
 * Custom deserializer to easily grab the contents
 * of changes.json and stores them in memory as a POJO
 */
@JsonDeserialize(using = Changes.Deserializer.class )
public class Changes {

    final List<String> removePlaylists;
    final List<Playlist> addPlaylists;
    final List<UpdatedSong> addSongs;

    public Changes(final List<String> removePlaylists, final List<Playlist> addPlaylists, final List<UpdatedSong> addSongs) {
        this.removePlaylists = removePlaylists;
        this.addPlaylists = addPlaylists;
        this.addSongs = addSongs;
    }

    public List<String> getRemovePlaylists() {
        return removePlaylists;
    }

    public List<Playlist> getAddPlaylists() {
        return addPlaylists;
    }

    public List<UpdatedSong> getAddSongs() {
        return addSongs;
    }

    public static class Deserializer extends JsonDeserializer<Changes> {
        private static final TypeReference<List<Playlist>> ADD_PLAYLIST_LIST_TYPE = new TypeReference<>() {};
        private static final TypeReference<List<String>> REMOVE_PLAYLIST_LIST_TYPE = new TypeReference<>() {};
        private static final TypeReference<List<UpdatedSong>> ADD_SONG_LIST_TYPE = new TypeReference<>() {};

        @Override
        public Changes deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            // check to make sure we know where we are
            if (p.currentToken() != JsonToken.START_OBJECT) {
                throw new JsonParseException(p, "Couldn't deserialize Changes; JSON isn't formatted correctly");
            }
            iterateToNextArray(p);
            final List<String> removePlaylists = p.readValueAs(REMOVE_PLAYLIST_LIST_TYPE);
            iterateToNextArray(p);
            final List<Playlist> addPlaylists = p.readValueAs(ADD_PLAYLIST_LIST_TYPE);
            iterateToNextArray(p);
            final List<UpdatedSong> addSongIds = p.readValueAs(ADD_SONG_LIST_TYPE);

            return new Changes(removePlaylists, addPlaylists, addSongIds);
        }

        private void iterateToNextArray(JsonParser p) throws IOException {
            while (p.currentToken() != JsonToken.START_ARRAY) {
                p.nextToken();
            }
        }
    }
}
