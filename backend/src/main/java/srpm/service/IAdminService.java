package srpm.service;

import srpm.dto.request.AdminRequest;
import srpm.model.User;

import java.util.List;

public interface IAdminService {

    List<User> getManagedUsers(String roleFilter);

    User getManagedUserById(Long id);

    User updateManagedUser(Long id, AdminRequest request);

    void deleteManagedUser(Long id);
}

