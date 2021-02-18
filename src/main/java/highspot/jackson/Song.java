package highspot.jackson;

/**
 * POJO to represent the songs structure in mixtape.json, e.g.:
 *     {
 *       "id" : "1",
 *       "artist": "Camila Cabello",
 *       "title": "Never Be the Same"
 *     },
 */
public class Song {
    String id;
    String artist;
    String title;

    public Song() {}
    public Song(String id, String artist, String title) {
        this.id = id;
        this.artist = artist;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }
}
