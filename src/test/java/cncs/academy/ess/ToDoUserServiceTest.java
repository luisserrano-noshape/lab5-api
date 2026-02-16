package cncs.academy.ess;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.service.PassUtil;
import cncs.academy.ess.service.TodoUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToDoUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoUserService userService;

    @Test
    void login_shouldReturnValidJWTTokenWhenCredentialsMatch() throws NoSuchAlgorithmException {

        String username = "testuser";
        String rawPassword = "password123";


        String hashedPassword = PassUtil.hashPass(rawPassword);


        User mockUser = new User(username, hashedPassword, "Base");

     //notaLS: intrução mockito- importante e interssasnte
        when(userRepository.findByUsername(username)).thenReturn(mockUser);


        String token = userService.login(username, rawPassword);

        assertNotNull(token, "O login não deve retornar null");


        String jwtContent = token;
        String[] parts = jwtContent.split("\\.");

        assertEquals(3, parts.length, "O JWT deve ter 3 partes (Header.Payload.Signature)");
        assertFalse(parts[0].isEmpty());
        assertFalse(parts[1].isEmpty());
        assertFalse(parts[2].isEmpty());
    }
}