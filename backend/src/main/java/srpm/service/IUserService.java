package srpm.service;

import srpm.dto.request.UpdateLecturerRequest;
import srpm.dto.request.UpdateStudentRequest;
import srpm.dto.request.UpdateUserRequest;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;

import java.util.Optional;

public interface IUserService {

    Optional<User> login(String username, String password);

    User createUser(User user);

    User updateUser(User user);

    User updateUserInfo(Long userId, UpdateUserRequest request);

    Lecturer updateLecturerInfo(Long userId, UpdateLecturerRequest request);

    Student updateStudentInfo(Long userId, UpdateStudentRequest request);

    Optional<User> getUserById(Long id);

    void deleteUser(Long id);
}

