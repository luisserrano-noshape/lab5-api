package cncs.academy.ess;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.memory.InMemoryTodoListsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTodoListsRepositoryTest {

    private InMemoryTodoListsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTodoListsRepository();
    }

    @Test
    void saveAndFindById_ShouldReturnSavedList() {
        TodoList list = new TodoList("Compras", 1);
        int id = repository.save(list);

        assertTrue(id > 0);
        assertEquals(id, list.getListId());

        TodoList foundList = repository.findById(id);
        assertNotNull(foundList);
        assertEquals("Compras", foundList.getName());
    }

    @Test
    void findById_ShouldReturnNull_WhenListDoesNotExist() {
        TodoList result = repository.findById(999);
        assertNull(result);
    }

    @Test
    void findAll_ShouldReturnAllLists() {
        repository.save(new TodoList("Lista A", 1));
        repository.save(new TodoList("Lista B", 1));

        List<TodoList> result = repository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAllByUserId_ShouldReturnOnlyListsOwnedByUser() {
        repository.save(new TodoList("User1 List1", 1));
        repository.save(new TodoList("User1 List2", 1));
        repository.save(new TodoList("User2 List1", 2));

        List<TodoList> user1Lists = repository.findAllByUserId(1);
        assertEquals(2, user1Lists.size());

        List<TodoList> user2Lists = repository.findAllByUserId(2);
        assertEquals(1, user2Lists.size());
        assertEquals(2, user2Lists.get(0).getOwnerId());

        List<TodoList> user3Lists = repository.findAllByUserId(3);
        assertTrue(user3Lists.isEmpty());
    }

    @Test
    void update_ShouldUpdateExistingList() {
        TodoList list = new TodoList("Original", 1);
        int id = repository.save(list);

        TodoList listToUpdate = new TodoList("Modificado", 1);
        listToUpdate.setId(id);

        repository.update(listToUpdate);

        TodoList found = repository.findById(id);
        assertEquals("Modificado", found.getName());
    }

    @Test
    void deleteById_ShouldReturnTrue_WhenListExists() {
        TodoList list = new TodoList("To Delete", 1);
        int id = repository.save(list);

        boolean deleted = repository.deleteById(id);

        assertTrue(deleted);
        assertNull(repository.findById(id));
    }

    @Test
    void deleteById_ShouldReturnFalse_WhenListDoesNotExist() {
        boolean deleted = repository.deleteById(999);
        assertFalse(deleted);
    }

    @Test
    void shareList_ShouldDoNothing_ButRunSuccessfully() {
        assertDoesNotThrow(() -> repository.shareList(1, 2));
    }
}