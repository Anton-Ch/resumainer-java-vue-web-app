# Feature Specification: Vue Auth Page

**Feature Branch**: `feat/003-vue-auth-page`

**Created**: 2026-06-02

**Status**: Draft

**Input**: User description: "create a specification for a Vue Auth Page. This will include both frontend and backend. Also, we need to start using Flyway for DB migration versioning, and the Postgres DB itself needs to be created in a Docker container."

## Clarifications

### Session 2026-06-02

- **Q**: Should this feature include placeholder pages for User Home and Admin Home?
  **A**: Yes — Feature 003 includes empty placeholder pages for User Home (for regular users) and Admin Home (for admin users). These pages serve as the landing destination after successful authentication and will be fully implemented in later features.
- **Q**: What loading state should auth forms show during API calls?
  **A**: Disable the submit button and show a spinner or "Processing..." text. Full-page overlay is unnecessary for simple forms.
- **Q**: Which auth events should be logged?
  **A**: Log all auth events at INFO level: successful registration, successful login, logout, and failed login attempts (with email, timestamp, and reason). Failed attempts also logged at WARN level.
- **Q**: Should login rate limiting be implemented?
  **A**: Yes. Lock the account for 15 minutes after 5 consecutive failed login attempts. Failed attempt counter resets after successful login or after the lockout period expires.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - First-time visitor registers a new account (Priority: P1)

As a first-time visitor who has viewed the Landing Page, I want to create an account by providing my email and password, so that I can access the application and start building my professional profile.

**Why this priority**: Registration is the entry gate to the entire application. Without it, visitors cannot use any feature that requires authentication. This is the first authenticated user touchpoint and must work reliably.

**Independent Test**: Can be fully tested by opening the registration page, submitting valid registration details, and confirming the account is created and the user is redirected to the appropriate home page. Delivers a complete account creation flow.

**Acceptance Scenarios**:

1. **Given** a visitor is on the Landing Page, **When** they click the "Register" or "Get started" button, **Then** the registration page opens with fields for email, password, and password confirmation.
2. **Given** the visitor is on the registration page, **When** they enter a valid email, a strong password, and matching password confirmation, **Then** the account is created successfully and the visitor is automatically signed in and redirected to User Home (or Admin Home if admin role is assigned).
3. **Given** the visitor submits the registration form, **When** the email address is already registered, **Then** the system displays a clear error message ("Email already registered") and the visitor can switch to Login.
4. **Given** the visitor submits the registration form, **When** the password is too weak (too short or lacks required characters), **Then** the system displays a validation error and the visitor can correct the password.
5. **Given** the visitor submits the registration form, **When** the password confirmation does not match the password, **Then** the system displays a mismatch error.
6. **Given** the visitor is on the registration page, **When** they click the "Already registered? Login now!" link, **Then** the page navigates to the login page.

---

### User Story 2 - Returning user logs in to the application (Priority: P1)

As a registered user, I want to log in with my email and password, so that I can access my profile, resume data, and generate new resumes.

**Why this priority**: Login is required for every returning user session. Without it, registered users cannot access the application. This is equal in priority to registration.

**Independent Test**: Can be fully tested by opening the login page, submitting valid credentials, and confirming the user is authenticated and redirected to the correct home page. Delivers a complete authentication flow.

**Acceptance Scenarios**:

1. **Given** a registered user is on the login page, **When** they enter their registered email and correct password, **Then** the system authenticates them and redirects to User Home.
2. **Given** a registered admin user is on the login page, **When** they enter their registered email and correct password, **Then** the system authenticates them and redirects to Admin Home.
3. **Given** a user submits the login form, **When** the email is not registered, **Then** the system displays a generic error message ("Invalid email or password") without revealing whether the email exists.
4. **Given** a user submits the login form, **When** the password is incorrect, **Then** the system displays the same generic error message.
5. **Given** a user submits the login form, **When** the account status is BLOCKED, **Then** the system displays a message ("Your account is inactive. Contact support for assistance.") and prevents login.
6. **Given** a visitor is on the login page, **When** they click the "Don't have an account? Register now!" link, **Then** the page navigates to the registration page.

---

### User Story 3 - Already authenticated user accesses auth pages (Priority: P2)

As an already signed-in user, I want to be automatically redirected to my appropriate home page when I visit the login or registration page, so that I do not accidentally create a duplicate account or see an irrelevant page.

**Why this priority**: This prevents user confusion and duplicate account attempts. Important for UX polish but lower priority than the core register/login flows.

**Independent Test**: Can be fully tested by logging in, then navigating to the login or register URL, and confirming the user is redirected to their home page instead. Delivers a safe, user-friendly auth experience.

**Acceptance Scenarios**:

1. **Given** a regular user is already logged in, **When** they navigate to the login page URL, **Then** they are automatically redirected to User Home.
2. **Given** a regular user is already logged in, **When** they navigate to the registration page URL, **Then** they are automatically redirected to User Home.
3. **Given** an admin user is already logged in, **When** they navigate to the login or registration page URL, **Then** they are automatically redirected to Admin Home.

---

### User Story 4 - User logs out of the application (Priority: P2)

As an authenticated user, I want to log out of the application, so that my session is securely ended and my data is protected on shared devices.

**Why this priority**: Logout is a standard security feature. Important for protecting user data but lower priority than the core login and registration flows.

**Independent Test**: Can be fully tested by clicking the logout button and confirming the session is ended, the user is redirected to the login or landing page, and attempting to access authenticated pages redirects back to login.

**Acceptance Scenarios**:

1. **Given** a logged-in user clicks the logout button, **When** the logout action completes, **Then** the user session is invalidated and the user is redirected to the login page (or Landing Page).
2. **Given** a logged-out user, **When** they try to access an authenticated page URL directly, **Then** they are redirected to the login page.

---

### User Story 5 - Visitor sees consistent bilingual auth pages (Priority: P3)

As a visitor, I want the registration and login pages to support English and Russian language switching, so that I can authenticate in my preferred language.

**Why this priority**: Bilingual support is a core product requirement. Auth pages must match the language selected on the Landing Page for a consistent experience.

**Independent Test**: Can be fully tested by selecting a language on the Landing Page or on the auth pages and verifying that all labels, placeholders, validation messages, and links switch to the selected language.

**Acceptance Scenarios**:

1. **Given** a visitor is on the login page in English, **When** they switch the language to Russian, **Then** all visible text (page title, labels, placeholders, links, validation messages) switches to Russian.
2. **Given** a visitor selected Russian on the Landing Page, **When** they navigate to the registration page, **Then** the auth page displays in Russian.
3. **Given** a visitor switches language on the auth page, **When** they navigate to another auth page, **Then** the language preference persists.

---

### User Story 6 - Authenticated user sees placeholder home page after login (Priority: P2)

As a newly registered or returning user, I want to see a functional placeholder home page after login that confirms my authentication status and provides navigation elements, so that I know I am successfully logged in and can access key application areas.

**Why this priority**: Without any destination after login, users would face a blank page or error. Placeholder pages provide the authenticated entry point that future features will build upon.

**Independent Test**: Can be fully tested by logging in and verifying that the correct role-based placeholder page (User Home for regular users, Admin Home for admin users) displays with expected structural elements. Delivers a complete post-auth landing experience.

**Acceptance Scenarios**:

1. **Given** a regular user has just registered or logged in, **When** they are redirected after authentication, **Then** they see the User Home placeholder page with: a page title "User Home", basic stats summary placeholder (total tokens sent, total tokens generated, total resumes created), an "Edit my profile" navigation button, a "Generate new resume" navigation button, a resume listing table placeholder showing empty state guidance "You haven't created any resumes yet", and a logout button in the header.
2. **Given** an admin user has just registered or logged in, **When** they are redirected after authentication, **Then** they see the Admin Home placeholder page with: a page title "Admin Home", basic stats summary placeholder (total users, total tokens sent, total tokens generated, total resumes created), navigation cards/links to Users, Resumes, and AI Models sections, and a logout button in the header.
3. **Given** any authenticated user, **When** they view their home page, **Then** the page includes a logo, language switcher, and the user's authenticated session indicator.
4. **Given** a user has no resumes yet, **When** they view the User Home resume table area, **Then** they see the empty state message: "You haven't created any resumes yet. Click 'Generate new resume' to get started."

---

### Edge Cases

- What happens when the visitor submits the registration form with an empty email or password? The system should show inline validation errors before submission.
- What happens when the server is unavailable during registration or login? The system should show a user-friendly error message allowing the user to retry.
- What happens when a user's account is soft-deleted? The system should prevent login with the same message as a BLOCKED account.
- What happens when the session expires while the user is on a form? The next server request should detect the expired session and redirect to login.
- What happens when multiple rapid login attempts are made with incorrect credentials? The system should not expose whether the email exists, should maintain consistent response time, and after 5 consecutive failed attempts the account is locked for 15 minutes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a registration page accessible from the Landing Page via "Register" and "Get started" / "Create your profile" buttons.
- **FR-002**: Registration form MUST include fields for email, password, and password confirmation.
- **FR-003**: Registration form MUST validate that email is required and in valid format.
- **FR-004**: Registration form MUST validate that password is required and meets minimum strength requirements.
- **FR-005**: Registration form MUST validate that password confirmation matches the password.
- **FR-006**: Upon successful registration, the system MUST create a user account, automatically sign in the user, and redirect to the appropriate home page (User Home for regular users, Admin Home for admins).
- **FR-007**: Upon successful registration, the system MUST create an initial empty profile record for the user (contact details placeholder).
- **FR-008**: If the email is already registered, the system MUST display an error message and NOT create a duplicate account.
- **FR-009**: The registration page MUST include a link to the login page for already-registered visitors.
- **FR-010**: System MUST provide a login page accessible from the Landing Page via "Login" button and from the registration page.
- **FR-011**: Login form MUST include fields for email and password.
- **FR-012**: Login form MUST validate that both email and password are provided.
- **FR-013**: Upon successful login, the system MUST redirect the user to User Home (for regular users) or Admin Home (for admin users).
- **FR-014**: If login credentials are invalid, the system MUST display a generic error message ("Invalid email or password") without revealing whether the email exists or the password is wrong.
- **FR-015**: If the user account status is BLOCKED or soft-deleted, the system MUST prevent login and display a message instructing the user to contact support.
- **FR-016**: The login page MUST include a link to the registration page.
- **FR-017**: If a user is already authenticated, accessing the login or registration page MUST automatically redirect them to their appropriate home page.
- **FR-018**: System MUST provide a logout action that invalidates the user session and redirects to the login page.
- **FR-019**: Unauthenticated users attempting to access authenticated pages MUST be redirected to the login page.
- **FR-020**: Registration and login pages MUST support bilingual text (English and Russian) with all visible text switching when the language is changed.
- **FR-021**: The language preference selected by the visitor MUST persist when navigating between the Landing Page and auth pages.
- **FR-022**: After successful authentication, regular users MUST be redirected to a User Home placeholder page with: page title, stats summary placeholders, "Edit my profile" button, "Generate new resume" button, empty resume table with empty-state guidance, logout button, logo, and language switcher.
- **FR-023**: After successful authentication, admin users MUST be redirected to an Admin Home placeholder page with: page title, stats summary placeholders, navigation cards/links to Users/Resumes/AI Models sections, logout button, logo, and language switcher.
- **FR-024**: The User Home placeholder MUST display empty-state text when no saved resumes exist: "You haven't created any resumes yet. Click 'Generate new resume' to get started."
- **FR-025**: The User Home and Admin Home placeholder pages MUST be Vue components that serve as structural shells for future features — navigation buttons and layout elements must be functional (buttons navigable), while stats and table data may show placeholder/default values.
- **FR-026**: During registration or login API calls, the submit button MUST be disabled and a loading indicator (spinner or "Processing..." text) MUST be displayed to prevent duplicate submissions and provide user feedback.
- **FR-027**: The system MUST log all auth events: successful registration (INFO), successful login (INFO), logout (INFO), and failed login attempts with email, timestamp, and reason (INFO + WARN for failed). Log entries must NOT contain passwords or session tokens.
- **FR-028**: The system MUST implement login rate limiting: after 5 consecutive failed login attempts for the same email, the account MUST be temporarily locked for 15 minutes. During lockout, login attempts must return a "Too many failed attempts. Try again later." message. The failed attempt counter resets after a successful login or after the lockout period expires.

### Key Entities *(include if feature involves data)*

- **User Account**: Registered user identity with email, BCrypt password hash, role (USER/ADMIN), status (ACTIVE/BLOCKED), and generation permission (ALLOWED/FORBIDDEN). Each user has a unique email for authentication.
- **User Session**: Authenticated user session that persists across page navigation and expires after a period of inactivity or on explicit logout.
- **Role**: User access level — USER for regular users, ADMIN for system administrators. Determines home page redirect destination after authentication.
- **Account Status**: Controls whether a user can authenticate. ACTIVE allows login, BLOCKED prevents login with a support-contact message.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A first-time visitor can complete registration in under 60 seconds (email entry, password creation, confirmation, submit).
- **SC-002**: A returning user can complete login in under 15 seconds (email entry, password entry, submit).
- **SC-003**: All validation error messages are displayed within 1 second of form submission, with no server-side generic errors replacing clear client-side validation.
- **SC-004**: An already-authenticated user navigating to the login or registration URL is immediately redirected to their home page without seeing the auth page content.
- **SC-005**: A user switching language on any auth page sees all visible text change to the selected language with no missing or untranslated text remaining visible.
- **SC-006**: After logout, the user session is invalidated and the user cannot access authenticated pages without re-authenticating.
- **SC-007**: After authentication, users see the correct role-based placeholder page (User Home or Admin Home) with all expected structural elements present (title, stats area, navigation buttons/links, logout, language switcher).
- **SC-008**: The User Home placeholder displays an appropriate empty-state message when no resumes exist, guiding the user to create their first resume.
- **SC-009**: After 5 consecutive failed login attempts, the account is locked for 15 minutes and further login attempts are rejected with a lockout message. Successful login or lockout expiry resets the counter.

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | Auth controller, service, and DAO follow layered architecture. No Spring Boot, JPA, or Hibernate. Maven CLI build must succeed. Flyway migrations for all database schema changes. PostgreSQL in Docker container. |
| **II. Testing Excellence** | JUnit 5 + Mockito tests for auth service and DAO. BCrypt password hashing tested. Invalid login attempts and duplicate email registration tested. Mock AI provider not needed for auth tests. JaCoCo coverage tracked. |
| **III. User Experience Consistency** | i18n via messages_en.properties/messages_ru.properties for auth pages. Dual validation (frontend + backend). PRG pattern for form submissions. No stack traces exposed. Consistent bilingual support. |
| **IV. Performance & Reliability** | PreparedStatement for all SQL queries in auth DAO. JDBC transaction management for registration (user creation + profile creation in one transaction). SQL-level pagination not needed for auth. UTF-8 encoding throughout. |
| **V. Security by Design** | Backend validation is authoritative. BCrypt for password hashing (never plaintext). No secrets in logs or builds. No email enumeration on login error. Session management with proper invalidation on logout. XSS sanitization for form inputs. CSRF protection on auth forms. |

**Technology Constraint Check** (per Constitution Technology Stack):
- [ ] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM)
- [ ] PostgreSQL with Flyway migrations
- [ ] Docker Compose for deployment (PostgreSQL container)
- [ ] Dev + Prod Spring profiles
- [ ] Vue 3 + Vite for frontend auth pages

## Assumptions

- The Landing Page (Feature 002) is already implemented and provides the entry points (Register, Login, Get started, Create your profile buttons) to the auth flow.
- Auth pages are built as Vue 3 components (frontend) with REST API calls to Spring MVC backend.
- Session-based authentication is used (server-side session, not JWT).
- Password strength requirements: minimum 8 characters, at least one uppercase letter, one lowercase letter, and one digit.
- Email is the unique identifier for authentication. Username is collected later in My Profile.
- A new registered user starts with role = USER, status = ACTIVE, permission = ALLOWED.
- PostgreSQL database is already running in Docker and accessible from the backend.
- Flyway migrations are versioned and repeatable; the first migration creates all auth-related tables (users, role, user_status, user_permission, language).
- The Landing Page (Thymeleaf) and auth pages (Vue) are separate but share the same domain. Navigation between them is handled by URL routing.
- Session timeout defaults to 30 minutes of inactivity.
- Security: passwords are BCrypt-hashed before storage; API responses never expose password hashes; session IDs are HTTP-only cookies.
