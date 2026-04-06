package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.GroupRequest;
import srpm.model.Group;
import srpm.service.GroupService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/groups")
@CrossOrigin
public class AdminGroupController {

    private final GroupService groupService;

    @Autowired
    public AdminGroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable String id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody GroupRequest request) {
        Group group = groupService.createGroup(request);
        return ResponseEntity.ok(group);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(
            @PathVariable String id,
            @RequestBody GroupRequest request
    ) {
        return groupService.updateGroup(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable String id) {
        boolean deleted = groupService.deleteGroup(id);

        if (deleted)
            return ResponseEntity.ok("Xóa group thành công");

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<?> addStudent(
            @PathVariable String groupId,
            @PathVariable String studentId
    ) {
        return groupService.addStudent(groupId, studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<?> removeStudent(
            @PathVariable String groupId,
            @PathVariable String studentId
    ) {
        return groupService.removeStudent(groupId, studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}