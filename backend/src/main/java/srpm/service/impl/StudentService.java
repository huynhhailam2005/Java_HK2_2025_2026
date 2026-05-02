package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.StudentSearchDto;
import srpm.repository.IStudentRepository;
import srpm.service.IStudentService;

import java.util.List;

@Service
@Transactional
public class StudentService implements IStudentService {

    private final IStudentRepository IStudentRepository;

    @Autowired
    public StudentService(IStudentRepository IStudentRepository) {
        this.IStudentRepository = IStudentRepository;
    }

    @Override
    public List<StudentSearchDto> searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String searchQuery = query.trim().toLowerCase();

        return IStudentRepository.findAll().stream()
                .filter(s -> s.getUsername().toLowerCase().contains(searchQuery)
                        || s.getStudentCode().toLowerCase().contains(searchQuery)
                        || (s.getEmail() != null && s.getEmail().toLowerCase().contains(searchQuery)))
                .map(s -> new StudentSearchDto(
                        s.getId(),
                        s.getUsername(),
                        s.getStudentCode(),
                        s.getEmail(),
                        s.getGithubUsername(),
                        s.getJiraAccountId()
                ))
                .limit(20)
                .toList();
    }

    @Override
    public List<StudentSearchDto> getAllStudents() {
        return IStudentRepository.findAll().stream()
                .map(s -> new StudentSearchDto(
                        s.getId(),
                        s.getUsername(),
                        s.getStudentCode(),
                        s.getEmail(),
                        s.getGithubUsername(),
                        s.getJiraAccountId()
                ))
                .toList();
    }
}

