package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.model.User;
import srpm.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) return Optional.empty();
        User user = userRepository.findByUsernameOrEmail(username.trim(), username.trim()).orElse(null);
        if (user != null && password.equals(user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
        if (userRepository.existsByEmail(user.getEmail())) throw new IllegalArgumentException("Email đã tồn tại");
        if (user.getID() == null || user.getID().isBlank()) {
            user.setID(UUID.randomUUID().toString());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}