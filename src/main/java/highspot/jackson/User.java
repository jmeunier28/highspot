package highspot.jackson;

/**
 * POJO to represent the users structure in mixtape.json, e.g.:
 *     {
 *       "id" : "1",
 *       "name" : "Albin Jaye"
 *     },
 */
public class User {
    String id;
    String name;

    public User() {}
    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
