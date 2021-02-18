package highspot;

import highspot.parsing.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final Parser parser = new Parser();
    private static StringBuilder buffer = new StringBuilder();
    private static String aux = "";
    private static String mixtapeJson = null;
    private static String changesJson  = null;
    private static BufferedReader fileReader;

    public static void main(final String[] args) {

        // Assume that input to program is these two files in this order
        // Some extra error handling could be added here e.g.
        try {
            buffer.setLength(0); // clear builder
            fileReader = new BufferedReader(new FileReader(args[0]));
            while ((aux = fileReader.readLine()) != null) {
                buffer.append(aux);
            }
            changesJson = buffer.toString();

            buffer.setLength(0); // clear builder
            fileReader = new BufferedReader(new FileReader(args[1]));
            while ((aux = fileReader.readLine()) != null) {
                buffer.append(aux);
            }
            mixtapeJson = buffer.toString();
        } catch (Exception exe) {
            LOGGER.error("ERROR: attempting to get data from user input", exe.getCause());
            System.exit(1);
        }

        try {
            assert changesJson != null;
            parser.parseAndStoreChanges(new ByteArrayInputStream(changesJson.getBytes()));
            assert mixtapeJson != null;
            parser.parseAndUpdate(new ByteArrayInputStream(mixtapeJson.getBytes()));
        } catch (Exception exe) {
            LOGGER.error("ERROR: attempting to parse and apply changes to user data", exe.getCause());
            System.exit(1);
        }

    }
}
