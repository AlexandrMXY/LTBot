package backend.academy.scrapper;

import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;

@Component
public class MockClientsHolder {
    public MockRestServiceServer bot;
    public MockRestServiceServer github;
    public MockRestServiceServer stackoverflow;
}
