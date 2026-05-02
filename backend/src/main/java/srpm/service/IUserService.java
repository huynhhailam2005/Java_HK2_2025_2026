package srpm.service;

import srpm.dto.request.RegisterRequest;
import srpm.dto.request.UpdateLecturerRequest;
import srpm.dto.request.UpdateStudentRequest;
import srpm.dto.request.UpdateUserRequest;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;

import java.util.Optional;

public interface IUserService {

    Optional<User> login(String username, String password);

    User registerUser(RegisterRequest regRequest);

    User createUser(User user);

    User updateUserInfo(Long userId, UpdateUserRequest request);

    Lecturer updateLecturerInfo(Long userId, UpdateLecturerRequest request);

    Student updateStudentInfo(Long userId, UpdateStudentRequest request);

    void changePassword(Long userId, String oldPassword, String newPassword);

    Optional<User> getUserById(Long id);
}

