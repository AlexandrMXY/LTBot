package backend.academy.scrapper;

import lombok.experimental.UtilityClass;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@UtilityClass
public class TestUtils {
    public static final String RESPONSES = "responses";

    public static String getResponseJson(String file) {
        InputStream jsonInputStream
            = TestUtils.class.getClassLoader().getResourceAsStream(RESPONSES + File.separator + file);
        String jsonString = convertInputStreamToString(jsonInputStream);
        return jsonString;
    }

    private String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
        String string = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return string;
    }
}
