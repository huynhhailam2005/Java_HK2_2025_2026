package srpm.service;

import srpm.dto.request.AdminRequest;
import srpm.dto.request.LecturerRequest;
import srpm.dto.request.StudentRequest;
import srpm.model.User;

import java.util.List;

public interface IAdminService {

    List<User> getManagedUsers(String roleFilter);

    User getManagedUserById(Long id);

    User updateManagedUser(Long id, AdminRequest request);

    void deleteManagedUser(Long id);

    User createStudent(StudentRequest request);

    User updateStudent(Long id, StudentRequest request);

    User createLecturer(LecturerRequest request);

    User updateLecturer(Long id, LecturerRequest request);

    Object toAdminResponse(User user);
}

