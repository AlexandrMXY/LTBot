package backend.academy.scrapper.controllers;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ChatControllerTest {
    @Mock
    public UserService userService;
    @InjectMocks
    public ChatController chatController;

    @Test
    public void registerChat_requestReceived_shouldSuccessIfUserServiceSuccess() {
        var response = chatController.registerChat(111);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        verify(userService).registerUserOrThrow(eq(111L), any());
    }

    @Test
    public void registerChat_requestReceived_shouldThrowIfAlreadyExists() {
        doAnswer((invocation) -> {
            throw (Exception) invocation.getArgument(1);
        }).when(userService).registerUserOrThrow(anyLong(), any());

        assertThrows(InvalidRequestException.class, () -> chatController.registerChat(111));
    }

    @Test
    public void deleteChat_requestReceived_shouldSuccessIfUserServiceSuccess() {
        var response = chatController.deleteChat(111);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        verify(userService).deleteUserOrThrow(eq(111L), any());
    }

    @Test
    public void deleteChat_requestReceived_shouldThrowIfNotFound() {
        doAnswer((invocation) -> {
            throw (Exception) invocation.getArgument(1);
        }).when(userService).deleteUserOrThrow(anyLong(), any());

        assertThrows(NotFoundException.class, () -> chatController.deleteChat(111));
    }
}
