package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    public void registerUserOrThrow_userExists_shouldThrow() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.registerUser(0));
    }

    @Test
    public void registerUserOrThrow_userNotExists_shouldNoThrow() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        userService.registerUser(0);
    }

    @Test
    public void deleteUserOrThrow_userExists_shouldNoThrow() {
        when(userRepository.existsById(eq(0L))).thenReturn(true);

        userService.deleteUser(0);
    }

    @Test
    public void deleteUserOrThrow_userNotExists_shouldThrow() {
        when(userRepository.existsById(eq(0L))).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(0));
    }
}
