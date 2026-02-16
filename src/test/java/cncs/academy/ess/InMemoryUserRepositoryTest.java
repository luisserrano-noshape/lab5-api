package cncs.academy.ess;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {
    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void saveAndFindById_ShouldReturnSavedUser() {
        User user = new User("luis", "password");
        int id = repository.save(user);
        User savedUser = repository.findById(id);
        assertEquals(user, savedUser);
    }

    @Test
    void findById_ShouldReturnNull_WhenUserDoesNotExist() {

        User result = repository.findById(999);
        assertNull(result, "Deve retornar null se o ID não existir");
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {

        User user = new User("luis", "password");
        repository.save(user);

        User result = repository.findByUsername("luis");

        assertNotNull(result, "O utilizador devia ser encontrado");
        assertEquals("luis", result.getUsername());
    }

    @Test
    void findByUsername_ShouldReturnNull_WhenUserDoesNotExist() {

        User result = repository.findByUsername("umutilizadorquenaoexiste");
        assertNull(result, "Deve retornar null se o username não existir");
    }
}