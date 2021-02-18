package highspot.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO to represent a playlist, which appears in both
 * the mixtape.json and changes.json structures
 */
public class Playlist {
    String id;
    @JsonProperty("user_id")
    String userId;
    @JsonProperty("song_ids")
    String[] songId;

    public Playlist() {}

    public Playlist(String id, String userId, String[] songId) {
        this.id = id;
        this.userId = userId;
        this.songId = songId;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String[] getSongId() {
        return songId;
    }
}
