package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.Todo;
import cncs.academy.ess.repository.TodoRepository;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLTodoRepository implements TodoRepository {

    private final BasicDataSource dataSource;

    public SQLTodoRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int save(Todo todo) {
        String sql = "INSERT INTO todos (description, list_id, completed) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, todo.getDescription());
            ps.setInt(2, todo.getListId());
            ps.setBoolean(3, todo.isCompleted()); // Assumindo que tens este getter

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Todo findById(int id) {
        String sql = "SELECT * FROM todos WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Todo todo = new Todo(
                            rs.getString("description"),
                            rs.getInt("list_id")
                    );
                    todo.setId(rs.getInt("id"));
                    // Podes precisar de setar 'completed' aqui também
                    return todo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Todo> findAllByListId(int listId) {
        List<Todo> todos = new ArrayList<>();
        String sql = "SELECT * FROM todos WHERE list_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, listId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Todo todo = new Todo(
                            rs.getString("description"),
                            rs.getInt("list_id")
                    );
                    todo.setId(rs.getInt("id"));
                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todos;
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM todos WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void update(Todo todo) {
        String sql = "UPDATE todos SET description = ?, list_id = ?, completed = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, todo.getDescription());
            ps.setInt(2, todo.getListId());
            ps.setBoolean(3, todo.isCompleted());
            ps.setInt(4, todo.getId()); // O ID é usado no WHERE para saber qual atualizar

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public List<Todo> findAll() {
        List<Todo> todos = new ArrayList<>();
        String sql = "SELECT * FROM todos";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Todo todo = new Todo(
                        rs.getString("description"),
                        rs.getInt("list_id")
                );
                todo.setId(rs.getInt("id"));
                todos.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todos;
    }
}