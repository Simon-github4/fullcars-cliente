package data.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoginService {

    public enum Role {
        ADMIN, USER
    }

    public static class User {
        private final String username;
        private final String password; 
        private final Role role;

        public User(String username, String password, Role role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public Role getRole() { return role; }
    }

    private final Map<String, User> users = new HashMap<>();

    public LoginService() {
        // Usuarios en memoria
        users.put("walterlucas", new User("walterlucas", "fullcontra", Role.ADMIN));
        users.put("fullcars", new User("fullcars", "1234", Role.USER));
    }

    public Optional<User> login(String username, String password) {
        User user = users.get(username);

        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean isUser(User user) {
        return user.getRole() == Role.USER;
    }
}
