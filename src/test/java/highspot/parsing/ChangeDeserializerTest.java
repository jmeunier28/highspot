package highspot.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import highspot.jackson.Changes;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ChangeDeserializerTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testDeserializeChangeFile() throws Exception {
        final byte[] resourceBytes = Resources.asByteSource(Resources.getResource(ChangeDeserializerTest.class, "/changes.json")).read();
        final InputStream inputStream = new ByteArrayInputStream(resourceBytes);
        final ObjectMapper objectMapper = new ObjectMapper();
        final Changes changes = objectMapper.readValue(inputStream, Changes.class);

        // assert that what we got back was the right thing.
        assertNotNull(changes);
        assertFalse(changes.getAddPlaylists().isEmpty());
        assertFalse(changes.getRemovePlaylists().isEmpty());
        assertFalse(changes.getAddSongs().isEmpty());
        assertEquals(2, changes.getRemovePlaylists().size());
        assertEquals(2, changes.getAddPlaylists().size());
        assertEquals(1, changes.getAddSongs().size());
        assertEquals(2, changes.getAddSongs().get(0).getSongIds().length);
    }
}
