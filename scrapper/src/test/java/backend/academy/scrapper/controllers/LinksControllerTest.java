package backend.academy.scrapper.controllers;

import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.LinkResponse;
import backend.academy.api.model.ListLinksResponse;
import backend.academy.api.model.RemoveLinkRequest;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.service.LinksManagementService;
import backend.academy.scrapper.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinksControllerTest {
    @Mock
    public LinksManagementService linksManagementService;
    @InjectMocks
    public LinksController linksController;

    @Test
    public void getLinks_requestReceived_shouldReturnListOfLinks() {
        List<LinkDto> results = List.of(
            new LinkDto("0", List.of("A"), List.of("AA", "AAA"), 0L),
            new LinkDto("1", List.of("B"), List.of("BB", "BBB"), 1L),
            new LinkDto("2", List.of("C"), List.of("CC", "CCC"), 2L));

        when(linksManagementService.getLinks(anyLong())).thenReturn(results);

        assertEquals(
            new ListLinksResponse(List.of(
                new LinkResponse(0, "0", List.of("A"), List.of("AA", "AAA")),
                new LinkResponse(1, "1", List.of("B"), List.of("BB", "BBB")),
                new LinkResponse(2, "2", List.of("C"), List.of("CC", "CCC")))),
            linksController.getLinks(0)
        );
    }

    @Test
    public void addLink_requestReceived_shouldReturnLinkResponse() {
        when(linksManagementService.addLink(anyLong(), any())).thenAnswer((i) -> {
            var dto = i.getArgument(1, LinkDto.class);
            return new LinkDto(dto.link(), dto.tags(), dto.filters(), i.getArgument(0));
        });

        var response = linksController.addLinks(11,
            new AddLinkRequest("qwerty", List.of("A"), List.of("AA", "AAA")));

        assertEquals(new LinkResponse(11, "qwerty", List.of("A"), List.of("AA", "AAA")), response);
    }

    @Test
    public void deleteLink_requestReceived_shouldReturnLinkResponse() {
        when(linksManagementService.deleteLink(anyLong(), any())).thenAnswer((i) ->
            new LinkDto(i.getArgument(1), List.of("A"), List.of("AA", "AAA"), i.getArgument(0)));

        var response = linksController.deleteLinks(11,
            new RemoveLinkRequest("qwerty"));

        assertEquals(new LinkResponse(11, "qwerty", List.of("A"), List.of("AA", "AAA")), response);
    }

}
