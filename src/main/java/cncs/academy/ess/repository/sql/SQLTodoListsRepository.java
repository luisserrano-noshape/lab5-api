package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLTodoListsRepository implements TodoListsRepository {

    private final BasicDataSource dataSource;

    public SQLTodoListsRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int save(TodoList list) {
        String sql = "INSERT INTO lists (name, owner_id) VALUES (?, ?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, list.getName());
            ps.setInt(2, list.getOwnerId());

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
    public void update(TodoList list) {
        String sql = "UPDATE lists SET name = ?, owner_id = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, list.getName());
            ps.setInt(2, list.getOwnerId());
            ps.setInt(3, list.getListId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TodoList findById(int id) {
        String sql = "SELECT * FROM lists WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TodoList list = new TodoList(
                            rs.getString("name"),
                            rs.getInt("owner_id")
                    );
                    list.setId(rs.getInt("id"));
                    return list;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<TodoList> findAll() {
        List<TodoList> lists = new ArrayList<>();
        String sql = "SELECT * FROM lists";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TodoList list = new TodoList(
                        rs.getString("name"),
                        rs.getInt("owner_id")
                );
                list.setId(rs.getInt("id"));
                lists.add(list);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lists;
    }

    @Override
    public List<TodoList> findAllByUserId(int userId) {
        List<TodoList> lists = new ArrayList<>();
        String sql = "SELECT * FROM lists WHERE owner_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TodoList list = new TodoList(
                            rs.getString("name"),
                            rs.getInt("owner_id")
                    );
                    list.setId(rs.getInt("id"));
                    lists.add(list);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lists;
    }

    public void shareList(int listId, int targetUserId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO list_shares (list_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING"
            );
            stmt.setInt(1, listId);
            stmt.setInt(2, targetUserId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to share list", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM lists WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}