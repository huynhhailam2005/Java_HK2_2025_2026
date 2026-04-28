package com.example.srpm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class LecturerQueryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void findLecturerId() {
        String sql = """
            SELECT u.user_id, u.username, u.email, l.lecturer_code
            FROM users u
            JOIN lecturers l ON u.user_id = l.user_id
            WHERE u.username = 'lecturer2' OR u.email = 'lecturer2@gmail.com'
            """;

        var result = jdbcTemplate.queryForList(sql);

        if (!result.isEmpty()) {
            var row = result.get(0);
            System.out.println("Lecturer ID: " + row.get("user_id"));
            System.out.println("Username: " + row.get("username"));
            System.out.println("Email: " + row.get("email"));
            System.out.println("Lecturer Code: " + row.get("lecturer_code"));
        } else {
            System.out.println("Lecturer not found");
        }
    }
}
