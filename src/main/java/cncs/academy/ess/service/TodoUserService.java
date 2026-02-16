package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TodoUserService {
    private final UserRepository repository;

    public TodoUserService(UserRepository userRepository) {
        this.repository = userRepository;
    }

    public User addUser(String username, String password, String role) throws NoSuchAlgorithmException {

        String hashedPassword = PassUtil.hashPass(password);

        User user = new User(username, hashedPassword, role);
        int id = repository.save(user);
        user.setId(id);
        return user;
    }
    public User getUser(int id) {
        return repository.findById(id);
    }

    public void deleteUser(int id) {
        repository.deleteById(id);
    }

    public String login(String username, String password) throws NoSuchAlgorithmException {
        User user = repository.findByUsername(username);
        if (user == null) {
            return null;
        }
        if (PassUtil.validatePassword(password, user.getPassword())) {
            //return createAuthToken(user);
            return JwtUtil.generateToken(username);
        }
        return null;
    }

    private String createAuthToken(User user) {
        return "Bearer " + user.getUsername();
    }
}
