package backend.academy.scrapper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.ErrorResponseException;

@UtilityClass
@Slf4j
public class RequestErrorHandlers {
    public static void logAndThrow(HttpRequest request, ClientHttpResponse response) throws IOException {
        String body = null;
        try {
            body = new BufferedReader(new InputStreamReader(response.getBody()))
                    .lines()
                    .collect(Collectors.joining());
        } catch (IOException exception) {
            log.atError().setMessage("IOException occurred").setCause(exception).log();
        }

        log.atError()
                .setMessage("Error response received")
                .addKeyValue("uri", request.getURI())
                .addKeyValue("method", request.getURI())
                .addKeyValue("code", response.getStatusCode())
                .addKeyValue("body", body)
                .log();

        throw new ErrorResponseException(response.getStatusCode());
    }
}
