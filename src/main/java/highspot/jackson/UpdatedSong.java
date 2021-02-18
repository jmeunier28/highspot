package highspot.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * UpdateSong refers to this section of the changes file
 *     "songs": [
 *       {
 *         "id": "3",
 *         "song_ids": [
 *           "1",
 *           "3"
 *         ]
 *       }
 *     ]
 */
public class UpdatedSong {
    String id; // id refers to the playlist Id that we want to add the songs to
    @JsonProperty("song_ids")
    String[] songIds; // songIds is a list of songs to add to the playlist

    public UpdatedSong() {}
    public UpdatedSong(String id, String[] songIds) {
        this.id = id;
        this.songIds = songIds;
    }

    public String getId() {
        return id;
    }

    public String[] getSongIds() {
        return songIds;
    }
}
