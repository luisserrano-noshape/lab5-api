package cncs.academy.ess.service;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.model.User; // <--- NOVO IMPORT
import cncs.academy.ess.repository.TodoListsRepository;
import cncs.academy.ess.repository.UserRepository; // <--- NOVO IMPORT

import java.util.Collection;

public class TodoListsService {

    TodoListsRepository todoListsRepository;
    UserRepository userRepository;


    public TodoListsService(TodoListsRepository todoListsRepository, UserRepository userRepository) {
        this.todoListsRepository = todoListsRepository;
        this.userRepository = userRepository;
    }

    public TodoList createTodoListItem(String listName, int ownerId) {
        TodoList list = new TodoList(listName, ownerId);
        int listId = todoListsRepository.save(list);
        list.setId(listId);
        return list;
    }

    public TodoList getTodoList(int listId) {
        return todoListsRepository.findById(listId);
    }

    public Collection<TodoList> getAllTodoLists(int userId) {
        return todoListsRepository.findAllByUserId(userId);
    }


    public void shareList(int listId, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        todoListsRepository.shareList(listId, user.getId());
    }
}