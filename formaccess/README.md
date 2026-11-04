# FormAccess – Authorized Information Collection System

A Google-Forms-style web app where **every form decides exactly who may fill it**
(department, year, role, account status). Having the link is not enough – the
server checks eligibility every time.

**Tech:** Java 21 · Spring Boot 3.3 · Spring Security · Spring Data JPA / Hibernate ·
MySQL 8 · Thymeleaf · Bootstrap 5 · Chart.js

---

## 1. What you need installed

| Tool | Version | Check with |
|---|---|---|
| Java JDK | **21** (17 also works – see note) | `java -version` |
| MySQL Server | **8.0.16 or newer** | `mysql --version` |
| IntelliJ IDEA Community (easiest) **or** Maven | any recent | – / `mvn -v` |
| Internet | needed once to download libraries, and for Bootstrap/Chart.js in the browser | – |

> **Only have Java 17?** Open `pom.xml` and change `<java.version>21</java.version>` to `17`. Nothing else changes.

---

## 2. Set up the database (one time, ~2 minutes)

Open a terminal **inside this `formaccess` folder** and run:

```bash
# Windows / macOS (asks for your MySQL root password)
mysql -u root -p < database/01_schema.sql
mysql -u root -p formaccess < database/02_sample_data.sql

# Ubuntu/Linux, if root uses socket login:
sudo mysql < database/01_schema.sql
sudo mysql formaccess < database/02_sample_data.sql
```

`01_schema.sql` creates the database `formaccess`, all 12 tables, and a MySQL user
`formaccess_app` / `FormAccess@2026` that the app logs in with.
`02_sample_data.sql` adds demo users, events, questions and answers.

> On Windows, if `mysql` is "not recognized", use the **MySQL Command Line Client** from the Start menu:
> log in, then type `source C:/path/to/formaccess/database/01_schema.sql;` and the same for `02_sample_data.sql`.

Optional checks: `03_test_queries.sql` (17 queries with expected results) and
`04_constraint_tests.sql` (proves the database rejects bad data – run with `--force`).

---

## 3. Run the application

### Option A – IntelliJ IDEA (recommended)
1. **File → Open** → choose this `formaccess` folder (the one containing `pom.xml`). Choose "Trust project".
2. Wait until the bottom bar finishes "Downloading/Indexing" (first time takes a few minutes).
3. Open `src/main/java/com/formaccess/FormAccessApplication.java` and click the green ▶ next to `main`.
4. When the console shows `Tomcat started on port 8080`, open **http://localhost:8080**

If IntelliJ asks for an SDK: **File → Project Structure → SDK → choose JDK 21** (or "Download JDK").

### Option B – terminal with Maven
```bash
mvn spring-boot:run
```
Then open **http://localhost:8080**. Stop with `Ctrl + C`.

### Run the unit tests
```bash
mvn test
```
(or right-click `src/test/java` → *Run 'All Tests'* in IntelliJ). 22 tests for the eligibility engine and answer validation.

---

## 4. Demo accounts (password for all: `Password@123`)

| Email | Role | Notes |
|---|---|---|
| `admin@formaccess.edu` | ADMIN | manages users, sees everything |
| `riya.sharma@college.edu` | CREATOR | owns *Hackathon Registration* and *DBMS Course Feedback* |
| `anil.kumar@college.edu` | CREATOR | owns a draft and a scheduled event |
| `arjun.mehta@college.edu` | PARTICIPANT | CSE, year 2 – eligible, already submitted |
| `sneha.iyer@college.edu` | PARTICIPANT | CSE, year 2 – eligible, **pending** for hackathon |
| `vikram.singh@college.edu` | PARTICIPANT | CSE, year 1 – **not** eligible for hackathon |
| `meera.joshi@college.edu` | PARTICIPANT | MECH, year 3 – not eligible for most forms |
| `dev.malhotra@college.edu` | PARTICIPANT | **deactivated** – cannot log in |

Direct link to the Hackathon form (try it as Sneha, then as Meera):
`http://localhost:8080/e/3f2b8c1e-6a4d-4e2f-9b1a-7c5d8e9f0a11`

---

## 5. Demonstration script (the full end-to-end workflow)

1. **Admin** logs in → *Overview* shows totals → *Users* → edit someone → change role to CREATOR. Deactivate/activate a user.
2. **Riya (creator)** logs in → *New event* → title, dates, Automatic deadline → *Create*.
3. On the event page → **Add question** → try different types (Rating, Checkboxes with options one-per-line, Integer with min/max …). Use ↑ ↓ to reorder.
4. **Who can participate?** → add `Department = CSE` (group 1), `Year ≥ 2` (group 1), then choose *New group (OR)* and add `Department = IT`.
   The *Eligible participants* number updates.
5. **Preview** → check the form → **Publish**. Copy the share link.
6. Log in as **Sneha** → *My forms* shows the event → fill and submit → confirmation page.
   Leave a required question empty or type too-short text: the browser warns, and the server checks again and shows the error under the question (it never trusts the browser).
7. Open the link again as Sneha → "Already submitted" (duplicates blocked).
8. Log in as **Meera** → open the same link → **Access denied** (link alone is not enough).
9. Back as **Riya** → submission count went up → *Pending participants* list → **Send reminder now**
   → the email appears in the **IntelliJ console** (mock email). Click again → people reminded recently are skipped.
10. **Close submissions** (or wait for the automatic deadline) → participants now see "Deadline passed".
11. **Analytics** → charts (timeline, departments, each question), averages, YES/NO counts.
12. In *What can participants see?* set e.g. *Total responses = PUBLIC_ALWAYS*, *Average = PUBLIC_AFTER_DEADLINE* → Save.
13. As a participant → *Results* → only those summaries are visible, never individual answers.

The closed **DBMS Course Feedback** event already has 5 responses, so its Analytics page is full of data straight away.

---

## 6. How the project works (for the viva)

### Architecture
```
Browser (HTML from Thymeleaf + Bootstrap + a little JavaScript, Chart.js)
   │  HTTP (session cookie + CSRF token)
Spring Security filter  → is the user logged in? does the role allow this URL?
   │
Controller   (controller/)   reads the request, calls a service, returns a page
   │
Service      (service/, eligibility/, statistics/)   ALL business rules live here
   │
Repository   (repository/)   Spring Data JPA interfaces → Hibernate writes the SQL
   │
MySQL        (database/01_schema.sql)
```
Pages are rendered on the server with **Thymeleaf** (simplest for a Java developer, CSRF works
automatically). One **REST API** (`GET /api/creator/events/{id}/stats`, class `StatsApiController`)
returns JSON that `static/js/analytics.js` turns into Chart.js graphs.

### Package guide
| Package | What is inside |
|---|---|
| `entity` | Java classes mapped to tables (`User` ↔ `users`, `Event` ↔ `events` …) |
| `entity.enums` | Fixed value lists: `Role`, `EventStatus`, `QuestionType`, `RuleOperator` … |
| `repository` | Database access interfaces; includes SQL/JPQL aggregation queries in `AnswerRepository` |
| `dto` | Form objects (what the user types) and view objects (what a page shows) |
| `security` | Login: `CustomUserDetailsService`, `AppUserPrincipal`, `CurrentUserService` |
| `config` | `SecurityConfig` – URL rules, login page, BCrypt |
| `service` | Events, form builder, rules, submissions, deadlines, reminders, dashboards |
| `eligibility` | **The rule engine** – `RuleEvaluator` (pure Java) + `EligibilityService` |
| `validation` | `AnswerValidator` – checks every answer on the server |
| `statistics` | `StatisticsService` (numbers & charts) + `SummaryService` (public/private) |
| `scheduler` | `@Scheduled` jobs: auto open/close every minute, automatic reminders every 10 min |
| `mail` | `EmailService` interface, `ConsoleEmailService` (mock), `SmtpEmailService` (real) |
| `exception` | Custom exceptions + `GlobalExceptionHandler` (shows errors as red alerts) |

### The eligibility engine (`eligibility/RuleEvaluator.java`)
Rules are stored as rows `(group_no, attribute, operator, value)` – creators **never type SQL**.
* conditions with the **same group number are joined by AND**
* **different groups are joined by OR** (this is called *Disjunctive Normal Form*)
* no rules → every active user; inactive users → never eligible
* attributes and operators come from fixed enums (a whitelist)

`EligibilityService.isEligible(user, event)` is called when a participant **opens** a form,
**submits** it, and **views results** (`SubmissionService.checkAccess`, `FormController`).

### What happens on submit (`SubmissionService.submit`)
1. account active? 2. eligible? 3. event OPEN? 4. before the deadline (checked by time, not only the scheduler)?
5. not a duplicate? 6. every answer valid (`AnswerValidator`)?
→ then the submission and all answers are saved in **one transaction** (`@Transactional`): all or nothing.
Duplicates are also blocked by the database: `UNIQUE (event_id, user_id, attempt_no)`.

### Security checklist
* Passwords hashed with **BCrypt** (`SecurityConfig.passwordEncoder`)
* Role rules per URL (`/admin/**` ADMIN, `/creator/**` CREATOR/ADMIN)
* Ownership checks: `EventService.getManagedEvent` (only the creator/admin), `SubmissionService.getOwnSubmission`
* CSRF tokens on every form (Spring Security default, added by `th:action`)
* SQL injection impossible: only JPA/parameterised queries, no string-built SQL
* Server-side validation of everything; public statistics return only totals
* Links use a random UUID (`public_token`), not guessable ids

### Time zone
All times are **IST (Asia/Kolkata)**. `FormAccessApplication.main` sets the JVM default, and
`application.properties` sets the JDBC/Hibernate time zone.

---

## 7. Real email (optional)
By default emails are printed in the console (`app.mail.mode=console`).
To really send them (e.g. Gmail with an *App Password*):
1. In `application.properties` set `app.mail.mode=smtp`
2. Set environment variables before starting: `MAIL_USERNAME=you@gmail.com`, `MAIL_PASSWORD=your-app-password`
   (in IntelliJ: Run → Edit Configurations → Environment variables). Credentials are never stored in code.

---

## 8. Troubleshooting

| Problem | Fix |
|---|---|
| `Access denied for user 'formaccess_app'@'localhost'` | Run `database/01_schema.sql` again as root. |
| `Unknown database 'formaccess'` / `Table ... doesn't exist` | You skipped step 2 – run both SQL scripts. |
| `Public Key Retrieval is not allowed` | Already handled in the URL (`allowPublicKeyRetrieval=true`); make sure you did not edit it. |
| `Port 8080 was already in use` | Change `server.port=8081` in `application.properties` (also `app.base-url`). |
| `release version 21 not supported` | Install JDK 21, or change `java.version` to 17 in `pom.xml`. |
| Page has no styling | The browser needs internet for Bootstrap/Chart.js (CDN). |
| Login works but numbers look odd for sample events | Sample dates are relative to when you ran `02_sample_data.sql`; run it again to refresh. |
| Want to start fresh | Run `01_schema.sql` then `02_sample_data.sql` again (it drops and recreates everything). |

---

## 9. Project structure
```
formaccess/
├── pom.xml                         Maven: libraries & build
├── database/                       01 schema · 02 sample data · 03 test queries · 04 constraint tests
└── src/
    ├── main/java/com/formaccess/   Java code (packages described above)
    ├── main/resources/
    │   ├── application.properties  database, time zone, mail settings
    │   ├── templates/              Thymeleaf HTML pages (admin/, creator/, participant/, form/, auth/, error/)
    │   └── static/                 css/app.css · js/app.js · js/analytics.js
    └── test/java/                  JUnit tests
```
