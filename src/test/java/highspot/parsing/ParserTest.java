package highspot.parsing;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import highspot.jackson.Changes;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParserTest {

    private Parser parser;
    private ByteArrayOutputStream stream;

    @Before
    public void setUp() {
        stream = new ByteArrayOutputStream();
        // pass a ByteArrayOutputStream in so I can inspect
        // the contents after we parse + apply batch update
        parser = new Parser(stream);
    }

    @Test
    public void testParseAndStoreChanges() throws Exception {
        ingestChanges();
        final Changes changes = parser.getChanges();
        assertNotNull(changes);
        assertEquals(2, changes.getRemovePlaylists().size());
        assertEquals(2, changes.getAddPlaylists().size());
        assertEquals(1, changes.getAddSongs().size());
        assertEquals(2, changes.getAddSongs().get(0).getSongIds().length);
    }

    @Test
    public void testParseAndUpdate() throws Exception {
        ingestChanges();
        ingestMixtape();
        assertNotNull(parser.getChanges());
        final byte[] expectedOutput = Resources.asByteSource(Resources.getResource(ParserTest.class, "/expected-output.json")).read();
        assertEquals(new String(stream.toByteArray(), Charsets.UTF_8), new String(expectedOutput, Charsets.UTF_8));
    }


    @Test(expected = RuntimeException.class)
    public void noChangesThrowsException() throws Exception {
        ingestMixtape();
    }


    @Test(expected = JsonParseException.class)
    public void malformedJson() throws Exception {
        ingestChanges();
        final byte[] resourceBytes = Resources.asByteSource(Resources.getResource(ParserTest.class, "/bad-mixtape.json")).read();
        final InputStream inputStream = new ByteArrayInputStream(resourceBytes);
        parser.parseAndUpdate(inputStream);
    }

    private void ingestChanges() throws Exception {
        final byte[] resourceBytes = Resources.asByteSource(Resources.getResource(ParserTest.class, "/changes.json")).read();
        final InputStream inputStream = new ByteArrayInputStream(resourceBytes);
        parser.parseAndStoreChanges(inputStream);
    }

    private void ingestMixtape() throws Exception {
        final byte[] resourceBytes = Resources.asByteSource(Resources.getResource(ParserTest.class, "/mixtape.json")).read();
        final InputStream inputStream = new ByteArrayInputStream(resourceBytes);
        parser.parseAndUpdate(inputStream);
    }

}