package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import srpm.entity.User;
import srpm.repository.UserRepository;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Logic kiểm tra đăng nhập
     * So khớp username và password từ Database
     * Theo use case: Login -> Verify password -> Display login error (nếu thất bại)
     * 
     * @param username - tên đăng nhập
     * @param password - mật khẩu
     * @return Optional<User> - thông tin người dùng nếu đăng nhập thành công
     */
    public Optional<User> login(String username, String password) {
        // Validate input parameters
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Bước 1: Tìm kiếm người dùng theo username từ database
        Optional<User> userOptional = userRepository.findByUsername(username.trim());
        
        // Bước 2: Nếu không tìm thấy người dùng, trả về Optional.empty()
        // Use case: Display login error (username không tồn tại)
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOptional.get();
        
        // Bước 3: Kiểm tra xem người dùng có bị khóa không
        // Use case: Display login error (tài khoản bị khóa)
        if (user.getActive() == null || !user.getActive()) {
            return Optional.empty();
        }
        
        // Bước 4: So khớp password
        // Use case: Verify password
        // Lưu ý: Nên sử dụng BCryptPasswordEncoder để mã hóa password trong production
        // Hiện tại đang so khớp trực tiếp (chỉ dùng cho demo)
        if (password.equals(user.getPassword())) {
            return Optional.of(user);
        }
        
        // Bước 5: Nếu password không khớp, trả về Optional.empty()
        // Use case: Display login error (password không đúng)
        return Optional.empty();
    }
    
    /**
     * Lấy thông tin người dùng theo ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Lấy thông tin người dùng theo username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Tạo người dùng mới
     */
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Cập nhật thông tin người dùng
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Xóa người dùng
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

