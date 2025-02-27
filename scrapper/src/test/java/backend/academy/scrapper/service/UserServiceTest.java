package backend.academy.scrapper.service;

import backend.academy.scrapper.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserService userService;


    @Test
    public void registerUserOrThrow_userExists_shouldThrow() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUserOrThrow(0, new RuntimeException()));
    }

    @Test
    public void registerUserOrThrow_userNotExists_shouldNoThrow() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        userService.registerUserOrThrow(0, new RuntimeException());
    }

    @Test
    public void deleteUserOrThrow_userExists_shouldNoThrow() {
        when(userRepository.existsById(eq(0L))).thenReturn(true);

        userService.deleteUserOrThrow(0, new RuntimeException());
    }

    @Test
    public void deleteUserOrThrow_userNotExists_shouldThrow() {
        when(userRepository.existsById(eq(0L))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUserOrThrow(0, new RuntimeException()));
    }
}
