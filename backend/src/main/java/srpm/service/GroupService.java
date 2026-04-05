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

import java.util.HashSet;
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

    /** Lấy tất cả group */
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    /** Lấy group theo id */
    public Optional<Group> getGroupById(String id) {
        return groupRepository.findById(id);
    }

    /** Tạo group mới */
    @Transactional
    public Group createGroup(GroupRequest req) {
        Lecturer lecturer = lecturerRepository.findById(req.getLecturerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + req.getLecturerId()));

        Set<Student> students = new HashSet<>();
        if (req.getStudentIds() != null) {
            for (String sid : req.getStudentIds()) {
                Student s = studentRepository.findById(sid)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + sid));
                students.add(s);
            }
        }

        Group group = new Group(UUID.randomUUID().toString(), req.getTitle(),
                req.getDescription(), lecturer, students);
        return groupRepository.save(group);
    }

    /** Cập nhật group */
    @Transactional
    public Optional<Group> updateGroup(String id, GroupRequest req) {
        return groupRepository.findById(id).map(group -> {
            if (req.getTitle() != null) group.setTitle(req.getTitle());
            if (req.getDescription() != null) group.setDescription(req.getDescription());

            if (req.getLecturerId() != null) {
                Lecturer lecturer = lecturerRepository.findById(req.getLecturerId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + req.getLecturerId()));
                group.setLecturer(lecturer);
            }

            if (req.getStudentIds() != null) {
                Set<Student> students = new HashSet<>();
                for (String sid : req.getStudentIds()) {
                    Student s = studentRepository.findById(sid)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + sid));
                    students.add(s);
                }
                group.setStudents(students);
            }
            return groupRepository.save(group);
        });
    }

    /** Xoá group */
    @Transactional
    public boolean deleteGroup(String id) {
        if (!groupRepository.existsById(id)) return false;
        groupRepository.deleteById(id);
        return true;
    }

    /** Thêm sinh viên vào group */
    @Transactional
    public Optional<Group> addStudent(String groupId, String studentId) {
        return groupRepository.findById(groupId).map(group -> {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + studentId));
            group.getStudents().add(student);
            return groupRepository.save(group);
        });
    }

    /** Xoá sinh viên khỏi group */
    @Transactional
    public Optional<Group> removeStudent(String groupId, String studentId) {
        return groupRepository.findById(groupId).map(group -> {
            group.getStudents().removeIf(s -> s.getID().equals(studentId));
            return groupRepository.save(group);
        });
    }
}

