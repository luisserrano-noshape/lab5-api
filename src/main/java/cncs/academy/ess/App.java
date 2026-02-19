package cncs.academy.ess;

import cncs.academy.ess.controller.AuthorizationMiddleware;
import cncs.academy.ess.controller.TodoController;
import cncs.academy.ess.controller.TodoListController;
import cncs.academy.ess.controller.UserController;
import cncs.academy.ess.repository.memory.InMemoryTodoRepository;
import cncs.academy.ess.repository.memory.InMemoryTodoListsRepository;
import cncs.academy.ess.repository.sql.SQLUserRepository;
import cncs.academy.ess.repository.sql.SQLTodoRepository;
import cncs.academy.ess.repository.sql.SQLTodoListsRepository;

import cncs.academy.ess.service.TodoListsService;
import cncs.academy.ess.service.TodoUserService;
import cncs.academy.ess.service.TodoService;
import io.javalin.Javalin;
import org.apache.commons.dbcp2.BasicDataSource;
import io.javalin.community.ssl.SslPlugin;


import java.security.NoSuchAlgorithmException;

public class App {
    public static void main(String[] args) throws NoSuchAlgorithmException {

      /*  SslPlugin plugin = new SslPlugin(conf -> {
            conf.pemFromPath("/home/luis/Downloads/tasklist-phase2/cert.pem", "/home/luis/Downloads/tasklist-phase2/key.pem");
        });

        Javalin.create(javalinConfig -> {
            javalinConfig.registerPlugin(plugin);
        }).start();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(8443);*/

        Javalin app = Javalin.create(config -> {

            config.registerPlugin(new SslPlugin(ssl -> {

                ssl.pemFromPath(
                        "cert.pem",
                        "key.pem"
                );
                ssl.sniHostCheck = false;
                ssl.insecurePort = 8080;  // http://localhost:8080
                ssl.securePort = 8443;    // https://localhost:8443
            }));


            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });

        }).start(); //

        // Initialize routes for user management
        //InMemoryUserRepository userRepository = new InMemoryUserRepository();
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        String connectURI = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", "localhost", "5432", "postgres", "postgres", "changeit");
        ds.setUrl(connectURI);

        SQLUserRepository userRepository = new SQLUserRepository(ds);
        TodoUserService userService = new TodoUserService(userRepository);
        UserController userController = new UserController(userService);

       /* InMemoryTodoListsRepository listsRepository = new InMemoryTodoListsRepository();
        TodoListsService toDoListService = new TodoListsService(listsRepository);
        TodoListController todoListController = new TodoListController(toDoListService);*/

        SQLTodoListsRepository listsRepository = new SQLTodoListsRepository(ds);
        TodoListsService todoListsService = new TodoListsService(listsRepository, userRepository);
        TodoListController todoListController = new TodoListController(todoListsService);

      /*  InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        TodoService todoService = new TodoService(todoRepository, listsRepository);
        TodoController todoController = new TodoController(todoService, toDoListService);*/

        SQLTodoRepository todoRepository = new SQLTodoRepository(ds);
        TodoService todoService = new TodoService(todoRepository, listsRepository);
        TodoController todoController = new TodoController(todoService, todoListsService);

        AuthorizationMiddleware authMiddleware = new AuthorizationMiddleware(userRepository);

        // CORS
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
        // Authorization middleware
        app.before(authMiddleware::handle);

        // User management
        app.post("/user", userController::createUser);
        app.get("/user/{userId}", userController::getUser);
        app.delete("/user/{userId}", userController::deleteUser);
        app.post("/login", userController::loginUser);

        // "To do" lists management
        /* POST /todolist
          {
              "listName": "Shopping list"
          }
         */
        app.post("/todolist", todoListController::createTodoList);
        app.get("/todolist", todoListController::getAllTodoLists);
        app.get("/todolist/{listId}", todoListController::getTodoList);
        app.post("/todolist/{list-id}/share", todoListController::shareList);

        // "To do" list items management
        /* POST /todo/item
          {
              "description": "Buy milk",
              "listId": 1
          }
         */
        app.post("/todo/item", todoController::createTodoItem);
        /* GET /todo/1/tasks */
        app.get("/todo/{listId}/tasks", todoController::getAllTodoItems);
        /* GET /todo/1/tasks/1 */
        app.get("/todo/{listId}/tasks/{taskId}", todoController::getTodoItem);
        /* DELETE /todo/1/tasks/1 */
        app.delete("/todo/{listId}/tasks/{taskId}", todoController::deleteTodoItem);

        fillDummyData(userService, todoListsService, todoService);
    }

    private static void fillDummyData(
           TodoUserService userService,
           TodoListsService toDoListService,
           TodoService todoService) throws NoSuchAlgorithmException {
        /*
        userService.addUser("user1", "password1");
        userService.addUser("user2", "password2");
        toDoListService.createTodoListItem("Shopping list", 1);
        toDoListService.createTodoListItem( "Other", 1);
        todoService.createTodoItem("Bread", 1);
        todoService.createTodoItem("Milk", 1);
        todoService.createTodoItem("Eggs", 1);
        todoService.createTodoItem("Cheese", 1);
        todoService.createTodoItem("Butter", 1);*/
    }
}
