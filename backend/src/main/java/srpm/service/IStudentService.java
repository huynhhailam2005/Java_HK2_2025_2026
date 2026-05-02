package srpm.service;

import srpm.dto.StudentSearchDto;

import java.util.List;

public interface IStudentService {
    List<StudentSearchDto> searchStudents(String query);

    List<StudentSearchDto> getAllStudents();
}

