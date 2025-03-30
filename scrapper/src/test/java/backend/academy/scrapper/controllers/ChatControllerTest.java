package backend.academy.scrapper.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;

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
        verify(userService).registerUser(eq(111L));
    }

    @Test
    public void registerChat_requestReceived_shouldThrowIfAlreadyExists() {
        doThrow(new AlreadyExistsException()).when(userService).registerUser(anyLong());

        assertThrows(AlreadyExistsException.class, () -> chatController.registerChat(111));
    }

    @Test
    public void deleteChat_requestReceived_shouldSuccessIfUserServiceSuccess() {
        var response = chatController.deleteChat(111);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        verify(userService).deleteUser(eq(111L));
    }

    @Test
    public void deleteChat_requestReceived_shouldThrowIfNotFound() {
        doThrow(new NotFoundException()).when(userService).deleteUser(anyLong());

        assertThrows(NotFoundException.class, () -> chatController.deleteChat(111));
    }
}
