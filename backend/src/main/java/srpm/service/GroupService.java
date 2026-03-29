package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.GroupRequest;
import srpm.model.Group;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.repository.GroupRepository;
import srpm.repository.LecturerRepository;
import srpm.repository.StudentRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public GroupService(
            GroupRepository groupRepository,
            LecturerRepository lecturerRepository,
            StudentRepository studentRepository
    ) {
        this.groupRepository = groupRepository;
        this.lecturerRepository = lecturerRepository;
        this.studentRepository = studentRepository;
    }



}

