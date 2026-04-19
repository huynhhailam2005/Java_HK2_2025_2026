# 📋 SRPM Project - Kiến trúc & Workflow Chi Tiết

> Hệ thống Quản lý Dự án Sinh viên (Student Project Resource Management) - Tài liệu kiến trúc toàn diện

---

## 📑 Mục lục

1. [Tổng quan dự án](#tổng-quan-dự-án)
2. [Cấu trúc Backend](#cấu-trúc-backend)
3. [Mô tả từng Layer](#mô-tả-từng-layer)
4. [Workflow chính](#workflow-chính)
5. [API Endpoints](#api-endpoints)
6. [Database Schema](#database-schema)
7. [Flow Chi Tiết](#flow-chi-tiết)

---

## 🎯 Tổng quan dự án

### Mục đích
SRPM là hệ thống quản lý dự án cho sinh viên, kết nối với **Jira** (quản lý task) và **GitHub** (quản lý code).

### Các vai trò người dùng
- **Admin**: Quản lý toàn hệ thống, người dùng, cấu hình Jira/GitHub
- **Lecturer (GV)**: Tạo nhóm, tạo issue, đánh giá sinh viên
- **Student (SV)**: Tham gia nhóm, làm task, nộp bài

### Công nghệ sử dụng
- **Backend**: Spring Boot 3.x, JPA/Hibernate, PostgreSQL, JWT
- **Frontend**: React 18 + Vite + Tailwind CSS
- **External API**: Jira Cloud API, GitHub API
- **Security**: Spring Security, JWT Token, Role-based Access Control (RBAC)

---

## 🏗️ Cấu trúc Backend

```
backend/src/main/java/srpm/
│
├── SrpmApplication.java           # Entry point
│
├── config/
│   ├── GlobalExceptionHandler.java # Centralized exception handling
│   └── RestTemplateConfig.java     # HTTP client config
│
├── controller/                      # REST Endpoints (HTTP Layer)
│   ├── AuthController.java          # Login, Register
│   ├── UserController.java          # User CRUD
│   ├── GroupController.java         # Group management
│   ├── GroupAdminController.java    # Admin group management
│   ├── StudentController.java       # Student management
│   ├── LecturerController.java      # Lecturer management
│   ├── AdminController.java         # Admin users management
│   ├── TeamLeaderController.java    # Team leader assignment
│   ├── JiraIssueSyncController.java # Issue sync with Jira
│   ├── JiraGroupSyncController.java # Group sync with Jira
│   ├── GitHubCacheController.java   # GitHub cache management
│   ├── GitHubMappingController.java # GitHub username mapping
│   ├── ProgressReportController.java # Progress tracking
│   └── SubmissionController.java    # Submission management
│
├── service/                         # Business Logic Layer
│   ├── (interfaces)
│   │   ├── IUserService.java
│   │   ├── IGroupService.java
│   │   ├── IGroupAccessService.java
│   │   ├── IJiraIssueSyncService.java
│   │   ├── IJiraIssuePushService.java
│   │   ├── IJiraGroupSyncService.java
│   │   ├── IGitHubService.java
│   │   ├── ISubmissionService.java
│   │   ├── IProgressReportService.java
│   │   ├── IIssueService.java
│   │   ├── ITeamLeaderService.java
│   │   ├── IAdminService.java
│   │   └── IAuthorizationService.java
│   │
│   └── impl/
│       ├── UserService.java
│       ├── GroupService.java
│       ├── GroupAccessService.java
│       ├── JiraIssueSyncService.java
│       ├── JiraIssuePushService.java
│       ├── JiraGroupSyncService.java
│       ├── GitHubService.java
│       ├── GitHubCollaboratorsCache.java
│       ├── SubmissionService.java
│       ├── ProgressReportService.java
│       ├── IssueService.java
│       ├── TeamLeaderService.java
│       ├── AdminService.java
│       ├── AuthorizationService.java
│       └── IssueService.java
│
├── dao/                             # Data Access Layer (Manual JPA)
│   ├── UserDao.java
│   ├── GroupDao.java
│   ├── GroupMemberDao.java
│   ├── StudentDao.java
│   ├── LecturerDao.java
│   ├── IssueDao.java
│   ├── SubmissionDao.java
│   └── (và các DAO khác)
│
├── repository/                      # Repository Interfaces (Spring Data JPA)
│   ├── UserRepository.java
│   ├── GroupRepository.java
│   ├── GroupMemberRepository.java
│   ├── StudentRepository.java
│   ├── LecturerRepository.java
│   ├── IssueRepository.java
│   ├── SubmissionRepository.java
│   └── (và các interface khác)
│
├── model/                           # Entity Classes (Database Models)
│   ├── User.java                    # Base class for all users
│   ├── Admin.java                   # extends User
│   ├── Lecturer.java                # extends User
│   ├── Student.java                 # extends User
│   ├── Group.java                   # Project/Group entity
│   ├── GroupMember.java             # Member of group with role
│   ├── Issue.java                   # Task/Issue entity
│   ├── Submission.java              # Submission of task
│   ├── UserRole.java                # Enum: ADMIN, LECTURER, STUDENT
│   ├── GroupMemberRole.java         # Enum: TEAM_MEMBER, TEAM_LEADER
│   ├── IssueType.java               # Enum: TASK, BUG, STORY
│   ├── IssueStatus.java             # Enum: TODO, IN_PROGRESS, DONE, CANCELLED
│   ├── SyncStatus.java              # Enum: PENDING, SYNCED, ERROR
│   └── UserFactory.java             # Factory for creating polymorphic User objects
│
├── dto/                             # Data Transfer Objects (API Models)
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── GroupRequest.java
│   │   ├── AdminRequest.java
│   │   ├── StudentRequest.java
│   │   ├── LecturerRequest.java
│   │   ├── CreateIssueRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── UpdateStudentRequest.java
│   │   ├── UpdateLecturerRequest.java
│   │   ├── JiraIssueSyncRequest.java
│   │   └── (và các request khác)
│   │
│   └── response/
│       ├── ApiResponse.java         # Standard API response wrapper
│       ├── AuthResponse.java        # Login response with token
│       ├── GroupDto.java
│       ├── AdminResponse.java
│       ├── StudentDto.java
│       ├── LecturerDto.java
│       ├── GitHubMemberMappingDto.java
│       ├── JiraGroupDto.java
│       ├── JiraUserDto.java
│       ├── IssueDetailDto.java
│       └── (và các response khác)
│
├── exception/                       # Custom Exception Classes
│   ├── ValidationException.java     # 400 Bad Request
│   ├── UnauthorizedException.java   # 401 Unauthorized
│   ├── ForbiddenException.java      # 403 Forbidden
│   ├── ResourceNotFoundException.java # 404 Not Found
│   └── (và các exception khác)
│
├── security/                        # Security & Authentication
│   ├── JwtService.java              # JWT token generation & validation
│   ├── JwtAuthenticationFilter.java # JWT authentication filter
│   └── SecurityConfig.java          # Spring Security configuration
│
└── util/                            # Utility Classes
    ├── GitHubValidationUtil.java    # GitHub URL & username validation
    ├── ApiConstants.java            # API constants (GitHub, Jira URLs, etc.)
    └── (và các util khác)
```

---

## 📚 Mô tả từng Layer

### 1️⃣ **Controller Layer** (HTTP Entry Point)
**Vai trò**: Tiếp nhận HTTP request từ client, gọi service, trả về response

**Ví dụ**:
```java
@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final IGroupService groupService;
    
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse> getGroup(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group không tìm thấy"));
        return ResponseEntity.ok(new ApiResponse(true, "OK", group));
    }
}
```

**Responsibilities**:
- ✅ Validate @Valid annotations (request validation)
- ✅ Call service methods
- ✅ Return appropriate HTTP status codes
- ✅ Throw exceptions (handled by GlobalExceptionHandler)

**Không được làm**:
- ❌ Không gọi DAO trực tiếp
- ❌ Không viết business logic
- ❌ Không dùng try-catch (dùng exception throwing)

---

### 2️⃣ **Service Layer** (Business Logic)
**Vai trò**: Xử lý business logic, orchestration, transaction management

**Cấu trúc**:
- **Interface**: Định nghĩa contracts (IGroupService.java)
- **Implementation**: Thực thi logic (GroupService.java)

**Ví dụ**:
```java
@Service
@Transactional(readOnly = true)
public class GroupService implements IGroupService {
    private final GroupDao groupDao;
    
    @Transactional  // Write operation
    public Group createGroup(GroupRequest request) {
        // Validate input
        if (groupDao.existsByGroupCode(request.getGroupCode())) {
            throw new ValidationException("Group code đã tồn tại");
        }
        
        // Create entity
        Group group = new Group();
        group.setGroupCode(request.getGroupCode());
        group.setGroupName(request.getGroupName());
        
        // Save & return
        return groupDao.save(group);
    }
}
```

**Responsibilities**:
- ✅ Business logic validation
- ✅ Transaction management (@Transactional)
- ✅ Orchestrate multiple DAO/API calls
- ✅ Throw custom exceptions
- ✅ Logging

**Không được làm**:
- ❌ Không handle HTTP request/response trực tiếp
- ❌ Không throw HTTP exceptions

---

### 3️⃣ **DAO Layer** (Data Access)
**Vai trò**: Truy cập database bằng JPA EntityManager (không dùng Spring Data JPA)

**Ví dụ**:
```java
@Repository
public class GroupDao {
    @PersistenceContext
    private EntityManager entityManager;
    
    public Optional<Group> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Group.class, id));
    }
    
    public Group save(Group group) {
        if (group.getId() == null) {
            entityManager.persist(group);
        } else {
            group = entityManager.merge(group);
        }
        return group;
    }
}
```

**Responsibilities**:
- ✅ CRUD operations
- ✅ Custom queries
- ✅ Entity lifecycle management

**Không được làm**:
- ❌ Không có @Transactional (transaction ở service)
- ❌ Không gọi service (circular dependency)

---

### 4️⃣ **Model Layer** (Entities)
**Vai trò**: Định nghĩa database schema và relationships

**Ví dụ**:
```java
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String groupCode;
    private String groupName;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> groupMembers = new ArrayList<>();
}
```

---

### 5️⃣ **DTO Layer** (API Contracts)
**Vai trò**: Định nghĩa request/response schemas cho API

**Request DTO** (Thường dùng Bean Validation):
```java
public class GroupRequest {
    @NotBlank(message = "Group code không được để trống")
    private String groupCode;
    
    @NotBlank(message = "Group name không được để trống")
    private String groupName;
}
```

**Response DTO** (Thường dùng factory pattern):
```java
public class GroupDto {
    private Long id;
    private String groupCode;
    private String groupName;
    
    // Factory method
    public static GroupDto fromEntity(Group entity) {
        GroupDto dto = new GroupDto();
        dto.id = entity.getId();
        dto.groupCode = entity.getGroupCode();
        return dto;
    }
}
```

---

### 6️⃣ **Exception Layer** (Error Handling)
**Vai trò**: Tập trung xử lý lỗi qua GlobalExceptionHandler

**Exception Types**:
```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse(false, e.getMessage(), null));
}

@ExceptionHandler(ValidationException.class)
public ResponseEntity<ApiResponse> handleValidation(ValidationException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse(false, e.getMessage(), null));
}
```

**Custom Exception Classes**:
- `ValidationException` → HTTP 400
- `UnauthorizedException` → HTTP 401
- `ForbiddenException` → HTTP 403
- `ResourceNotFoundException` → HTTP 404

---

### 7️⃣ **Security Layer** (Authentication & Authorization)
**Vai trò**: JWT token management và permission checking

**JWT Flow**:
```
Client Request
    ↓
JwtAuthenticationFilter (kiểm tra token)
    ↓
JwtService.validateToken()
    ↓
Spring Security Context (set Authentication)
    ↓
AuthorizationService.canAccess() (kiểm tra permission)
    ↓
Controller method execution
```

---

## 🔄 Workflow chính

### 1. **Authentication Flow** (Đăng nhập)

```
POST /api/auth/login
├─ AuthController.login()
├─ UserService.login()
├─ PasswordEncoder.matches()
├─ JwtService.generateToken()
└─ Response: { token, user }
```

**Chi tiết**:
1. Client gửi `{ username, password }`
2. Service kiểm tra user tồn tại + password đúng
3. Generate JWT token
4. Return token + user info
5. Client lưu token ở localStorage
6. Client gửi token trong header: `Authorization: Bearer <token>`

---

### 2. **Group Management Flow** (Quản lý nhóm)

```
POST /api/groups (Lecturer)
├─ GroupController.createGroup()
├─ GroupService.createGroup()
│  ├─ Validate group code không trùng
│  ├─ Create Group entity
│  └─ GroupDao.save()
├─ JiraGroupSyncService.syncJiraGroupToLocalGroup()
│  ├─ Fetch users từ Jira Project
│  ├─ Create Student objects
│  └─ Sync GroupMembers
├─ GitHubService.getGroupMemberMappings()
│  ├─ Fetch collaborators từ GitHub
│  └─ Map với members
└─ Response: { groupId, name, members }
```

---

### 3. **Issue Management Flow** (Quản lý task)

```
POST /api/issues (Lecturer)
├─ JiraIssueSyncController.createIssue()
├─ JiraIssuePushService.createIssueOnJira()
│  ├─ Build Jira issue JSON
│  ├─ Call Jira API: POST /issue
│  └─ Get issueCode (e.g., "PROJECT-123")
├─ IssueService.createLocalIssue()
│  ├─ Create Issue entity
│  ├─ Set issueCode từ Jira
│  └─ IssueDao.save()
└─ Response: { issueId, issueCode, title }
```

---

### 4. **Submission Flow** (Nộp bài)

```
POST /api/submissions (Student)
├─ SubmissionController.submitForIssue()
├─ SubmissionService.submitForIssue()
│  ├─ Validate Issue status (TODO or IN_PROGRESS)
│  ├─ Validate Student được giao Issue
│  ├─ Create Submission entity
│  ├─ SubmissionDao.save()
│  ├─ Update Issue status → DONE
│  └─ JiraIssuePushService.updateJiraStatus()
│      ├─ Get Transition ID từ Jira
│      └─ Call POST /issue/{key}/transitions
└─ Response: { submissionId, issueId, content }
```

---

### 5. **GitHub Sync Flow** (Đồng bộ GitHub)

```
GET /api/github/collaborators/{owner}/{repo}
├─ GitHubMappingController.getCollaborators()
├─ GitHubService.getGroupMemberMappings()
│  ├─ Check cache
│  ├─ If expired: fetchGitHubCollaborators()
│  │  ├─ HTTP GET: /repos/{owner}/{repo}/collaborators
│  │  └─ GitHubCollaboratorsCache.put()
│  └─ Map members với collaborators
└─ Response: [{ memberId, githubUsername, role }]
```

---

### 6. **Progress Report Flow** (Báo cáo tiến độ)

```
GET /api/reports/groups/{groupId}
├─ ProgressReportController.getGroupProgressReport()
├─ AuthorizationService.canAccessGroup()
├─ ProgressReportService.getGroupProgressReport()
│  ├─ Get all Issues in group
│  ├─ Calculate stats: { total, done, inProgress, todo }
│  ├─ Get member performance
│  └─ Get GitHub commit stats
└─ Response: { groupStats, memberStats, githubStats }
```

---

## 🔌 API Endpoints

### **Authentication**
```
POST   /api/auth/login              # Login
POST   /api/auth/register           # Register
```

### **User Management**
```
GET    /api/users/{userId}          # Get user info
PUT    /api/users/{userId}          # Update user info
PUT    /api/users/{userId}/student  # Update student info
PUT    /api/users/{userId}/lecturer # Update lecturer info
```

### **Group Management**
```
POST   /api/groups                  # Create group (Lecturer)
GET    /api/groups/{groupId}        # Get group
PUT    /api/groups/{groupId}        # Update group
DELETE /api/groups/{groupId}        # Delete group
GET    /api/groups/members/{groupId} # Get members

POST   /api/admin/groups            # Create group (Admin)
GET    /api/admin/groups/{id}       # Admin get group
```

### **Issue Management**
```
POST   /api/issues                  # Create issue
GET    /api/issues/{issueId}        # Get issue
PUT    /api/issues/{issueId}        # Update issue
POST   /api/issues/{issueId}/sync   # Sync with Jira
```

### **Submission**
```
POST   /api/submissions             # Submit task
GET    /api/submissions/{submissionId} # Get submission
```

### **GitHub Integration**
```
GET    /api/github/collaborators/{owner}/{repo}
POST   /api/github/members/{memberId}/username
DELETE /api/github/members/{memberId}/username
```

### **Jira Integration**
```
POST   /api/jira/sync-group         # Sync group with Jira
GET    /api/jira/issues/{groupId}   # Get Jira issues
```

### **Reports**
```
GET    /api/reports/groups/{groupId} # Group progress
GET    /api/reports/members/{memberId} # Member performance
```

### **Admin**
```
GET    /api/admin/users             # List all users
PUT    /api/admin/users/{userId}    # Update user
DELETE /api/admin/users/{userId}    # Delete user
POST   /api/admin/github-cache/stats # Cache stats
DELETE /api/admin/github-cache/{owner}/{repo} # Clear cache
```

---

## 💾 Database Schema

### **Users Table** (Inheritance: SINGLE_TABLE)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    discriminator VARCHAR(31),  -- ADMIN, LECTURER, STUDENT
    username VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(255),
    user_role VARCHAR(50),      -- ADMIN, LECTURER, STUDENT
    jira_account_id VARCHAR(255),
    github_username VARCHAR(100),
    -- Lecturer fields
    lecturer_code VARCHAR(100),
    -- Student fields
    student_code VARCHAR(100)
);
```

### **Groups Table**
```sql
CREATE TABLE groups (
    id BIGINT PRIMARY KEY,
    group_code VARCHAR(100) UNIQUE,
    group_name VARCHAR(255),
    description TEXT,
    lecturer_id BIGINT,
    jira_url VARCHAR(255),
    jira_project_key VARCHAR(50),
    jira_admin_email VARCHAR(100),
    jira_api_token VARCHAR(255),
    github_repo_url VARCHAR(255),
    github_access_token VARCHAR(255),
    FOREIGN KEY (lecturer_id) REFERENCES users(id)
);
```

### **GroupMembers Table**
```sql
CREATE TABLE group_members (
    id BIGINT PRIMARY KEY,
    group_id BIGINT,
    student_id BIGINT,
    role VARCHAR(50),  -- TEAM_MEMBER, TEAM_LEADER
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);
```

### **Issues Table**
```sql
CREATE TABLE issues (
    id BIGINT PRIMARY KEY,
    group_id BIGINT,
    issue_code VARCHAR(50),  -- e.g., "PROJECT-123" (từ Jira)
    title VARCHAR(255),
    description TEXT,
    issue_type VARCHAR(50),  -- TASK, BUG, STORY
    status VARCHAR(50),      -- TODO, IN_PROGRESS, DONE, CANCELLED
    assigned_to BIGINT,      -- GroupMember ID
    sync_status VARCHAR(50), -- PENDING, SYNCED, ERROR
    is_deleted BOOLEAN,
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (assigned_to) REFERENCES group_members(id)
);
```

### **Submissions Table**
```sql
CREATE TABLE submissions (
    id BIGINT PRIMARY KEY,
    submission_code VARCHAR(50),
    issue_id BIGINT,
    submitted_by BIGINT,  -- GroupMember ID
    content TEXT,
    submitted_at TIMESTAMP,
    FOREIGN KEY (issue_id) REFERENCES issues(id),
    FOREIGN KEY (submitted_by) REFERENCES group_members(id)
);
```

---

## 🎬 Flow Chi Tiết - Use Cases

### **Use Case 1: Lecturer tạo nhóm và task**

```
1. Lecturer đăng nhập
   ├─ POST /api/auth/login
   └─ Get JWT token

2. Lecturer tạo nhóm
   ├─ POST /api/groups
   ├─ Frontend gửi: { groupCode, groupName, githubRepoUrl }
   ├─ Backend:
   │  ├─ GroupService validate groupCode unique
   │  ├─ Tạo Group entity
   │  ├─ Sync với Jira (lấy members)
   │  ├─ Sync với GitHub (lấy collaborators)
   │  └─ Return: groupId, members, collaborators
   └─ Frontend hiển thị danh sách members

3. Lecturer tạo task
   ├─ POST /api/issues
   ├─ Frontend gửi: { title, description, type, assignedTo }
   ├─ Backend:
   │  ├─ JiraIssuePushService call Jira API
   │  ├─ Tạo Issue trên Jira → nhận issueCode
   │  ├─ IssueService tạo Issue local (lưu issueCode)
   │  └─ Return: issueId, issueCode
   └─ Frontend hiển thị issue

4. Lecturer gán task cho sinh viên
   ├─ Hệ thống tự gán từ bước 3 (assignedTo)
   └─ Sinh viên nhận thông báo
```

---

### **Use Case 2: Student nộp bài**

```
1. Student nhìn thấy task
   ├─ GET /api/groups/{groupId}
   ├─ Frontend hiển thị danh sách issues
   └─ Student click vào issue để nộp

2. Student nộp bài
   ├─ POST /api/submissions
   ├─ Frontend gửi: { issueId, content (URL hoặc text) }
   ├─ Backend:
   │  ├─ SubmissionService validate
   │  │  ├─ Issue tồn tại & không deleted
   │  │  ├─ Issue status = TODO or IN_PROGRESS
   │  │  ├─ Student được giao issue này
   │  │  └─ Submission chưa tồn tại
   │  ├─ Tạo Submission entity
   │  ├─ Update Issue status → DONE
   │  ├─ Call Jira API để update status
   │  └─ Return: submissionId
   └─ Frontend hiển thị "Nộp thành công"

3. Lecturer xem báo cáo tiến độ
   ├─ GET /api/reports/groups/{groupId}
   ├─ Backend tính toán:
   │  ├─ Total issues
   │  ├─ Done issues
   │  ├─ Member performance
   │  └─ GitHub commit stats
   └─ Frontend hiển thị dashboard
```

---

### **Use Case 3: Admin quản lý hệ thống**

```
1. Admin xem danh sách users
   ├─ GET /api/admin/users
   ├─ Query: ?role=LECTURER&status=active
   └─ Response: [{ userId, username, email, role }]

2. Admin cập nhật user
   ├─ PUT /api/admin/users/{userId}
   ├─ Frontend gửi: { username, email, password, role }
   ├─ AdminService validate & cập nhật
   └─ Response: updated user

3. Admin xem GitHub cache stats
   ├─ GET /api/admin/github-cache/stats
   ├─ Response: { cacheSize, entries }
   └─ Admin có thể clear cache

4. Admin clear cache
   ├─ DELETE /api/admin/github-cache/{owner}/{repo}
   └─ Response: success
```

---

### **Use Case 4: Xử lý lỗi**

```
1. Client gửi invalid request
   ├─ POST /api/groups
   ├─ Body: { groupCode: null }
   │
   ├─ GlobalExceptionHandler.handleMethodArgumentNotValid()
   │  ├─ Validate fails (@NotBlank)
   │  ├─ Tạo error map: { "groupCode": "Group code không được để trống" }
   │  └─ Return HTTP 400 + error details
   │
   └─ Frontend xử lý: hiển thị error message

2. Client truy cập resource không tồn tại
   ├─ GET /api/groups/999
   │
   ├─ GroupService.getGroupById(999)
   │  ├─ GroupDao không tìm thấy
   │  └─ Throw ResourceNotFoundException
   │
   ├─ GlobalExceptionHandler.handleResourceNotFound()
   │  └─ Return HTTP 404 + "Group không tìm thấy"
   │
   └─ Frontend xử lý: hiển thị 404 page

3. Client không có permission
   ├─ GET /api/groups/{groupId}
   │
   ├─ AuthorizationService.canAccessGroup(groupId)
   │  ├─ User không phải Lecturer của group
   │  └─ Throw ForbiddenException
   │
   ├─ GlobalExceptionHandler.handleForbidden()
   │  └─ Return HTTP 403 + "Bạn không có quyền"
   │
   └─ Frontend xử lý: redirect to login hoặc 403 page
```

---

## 🔐 Security Architecture

### **Authentication Process**

```
1. Client gửi credentials
   ├─ POST /api/auth/login
   └─ Body: { username, password }

2. Backend xác thực
   ├─ UserService.login()
   ├─ Find user by username
   ├─ PasswordEncoder.matches(password, encoded)
   └─ If match → tìm tiếp User object

3. Generate JWT Token
   ├─ JwtService.generateToken(user)
   ├─ Payload: { userId, username, role, iat, exp }
   ├─ Secret key: từ application.properties
   └─ Return token

4. Client lưu token
   ├─ localStorage.setItem("token", token)
   └─ Tất cả request tiếp theo gửi token

5. Request tiếp theo
   ├─ Client gửi: Headers: { Authorization: Bearer <token> }
   ├─ JwtAuthenticationFilter kiểm tra
   │  ├─ Extract token từ header
   │  ├─ JwtService.validateToken()
   │  ├─ If valid → Spring Security context
   │  └─ If invalid → 401 Unauthorized
   └─ Request được process
```

### **Authorization (Role-based)**

```
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> deleteUser(@PathVariable Long userId) { }

@PreAuthorize("hasRole('LECTURER')")  
public ResponseEntity<?> createGroup(@RequestBody GroupRequest request) { }

// Custom authorization
public boolean canAccessGroup(Long groupId) {
    User currentUser = getCurrentUser();
    Group group = getGroup(groupId);
    
    if (currentUser.isAdmin()) return true;
    if (group.getLecturer().getId().equals(currentUser.getId())) return true;
    
    return false;
}
```

---

## 📊 Design Patterns Used

### **1. Factory Pattern**
```java
// UserFactory.createUser()
User user = UserFactory.createUser("STUDENT");  // → Student object
User user = UserFactory.createUser("LECTURER"); // → Lecturer object
```

### **2. Strategy Pattern**
```java
// GlobalExceptionHandler with different strategies
@ExceptionHandler(ResourceNotFoundException.class)
@ExceptionHandler(ValidationException.class)
@ExceptionHandler(ForbiddenException.class)
// Each handler is a different strategy
```

### **3. Template Method Pattern**
```java
// Helper methods in controller
private ResponseEntity<ApiResponse> okResponse(String msg, Object data) {
    return ResponseEntity.ok(new ApiResponse(true, msg, data));
}
// Used in multiple endpoints
```

### **4. Data Transfer Object (DTO)**
```java
// Separate API model from Entity
@PostMapping
public ResponseEntity<?> create(@Valid @RequestBody GroupRequest request) {
    // Request DTO
}
// → Service processes
// → Return GroupDto (Response DTO)
```

### **5. Dependency Injection**
```java
// Constructor-based DI
private final IGroupService groupService;
private final IGroupAccessService groupAccessService;

@Autowired
public GroupController(IGroupService groupService, 
                       IGroupAccessService groupAccessService) {
    this.groupService = groupService;
    this.groupAccessService = groupAccessService;
}
```

---

## 🎯 SOLID Principles Applied

| Principle | Implementation |
|-----------|-----------------|
| **S**RP | Controllers handle HTTP only, Services handle business logic |
| **O**CP | GlobalExceptionHandler extensible for new exception types |
| **L**SP | Interfaces ensure substitutable implementations |
| **I**SP | Focused interfaces (IGroupService, IGroupAccessService) |
| **D**IP | Controllers depend on interfaces, not implementations |

---

## 📝 Best Practices Implemented

✅ **Layered Architecture**: Clear separation of concerns  
✅ **Bean Validation**: @Valid on DTOs for automatic validation  
✅ **Exception Handling**: Centralized GlobalExceptionHandler  
✅ **Logging**: SLF4J with appropriate levels  
✅ **JWT Security**: Stateless authentication  
✅ **Transaction Management**: @Transactional on service methods  
✅ **Enum Types**: Type-safe alternatives to strings  
✅ **Factory Pattern**: Polymorphic object creation  
✅ **DTO Pattern**: API contracts separate from entities  
✅ **Caching**: GitHub collaborators cache with TTL

---

## 🚀 Deployment Checklist

- [ ] PostgreSQL database configured
- [ ] application.properties updated with DB credentials
- [ ] JWT secret key configured
- [ ] Jira API credentials verified
- [ ] GitHub API token configured
- [ ] Frontend API_URL points to backend
- [ ] CORS configuration correct
- [ ] All tests passing
- [ ] Code compiled without warnings
- [ ] Database migrations run

---

## 📞 Support

For issues or questions:
1. Check logs: `tail -f backend/logs/application.log`
2. Check Postman collection: `/docs/SRPM.postman_collection.json`
3. Contact team lead

---

**Last Updated**: April 17, 2026  
**Version**: 1.0  
**Maintained By**: Development Team

