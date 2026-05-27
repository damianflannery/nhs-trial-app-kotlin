# Project Comparison: Java EE vs Kotlin/Ktor

A side-by-side comparison of the two implementations of the NHS Clinical Trial Registration application.

| | [nhs-trial-app](https://github.com/damianflannery/nhs-trial-app) | [nhs-trial-app-kotlin](https://github.com/damianflannery/nhs-trial-app-kotlin) |
|---|---|---|
| **Language** | Java 17 | Kotlin 2.1.0 |
| **Runtime** | Tomcat 10.1 (Jakarta EE 10) | Ktor 3.0.3 (Netty, embedded) |
| **Build** | Maven 3.9 | Gradle 8.11.1 |
| **Packaging** | WAR (deployed to Tomcat) | Fat JAR (Shadow plugin) |
| **Database access** | Raw JDBC via HikariCP | Exposed ORM DSL + HikariCP |
| **Migrations** | Flyway 10 | Flyway 10 |
| **Database** | PostgreSQL 16 | PostgreSQL 16 |
| **Templating** | JSP + JSTL 3 | kotlinx.html DSL |
| **Frontend interaction** | Traditional form POST (full page reload) | HTMX 2.0.3 (AJAX form submission, partial HTML swap) |
| **Sessions** | Jakarta `HttpSession` (cookie-backed, server-managed) | Ktor `SessionStorageMemory` with custom JSON serializer |
| **Routing** | Servlet annotations (`@WebServlet`) | Ktor `Route` DSL |
| **Validation** | Custom `PersonValidator` / `MedicalValidator` classes | Custom `PersonValidator` / `MedicalValidator` (same logic, idiomatic Kotlin) |
| **NHS number validation** | Modulus 11 check digit | 10-digit format only (Modulus 11 removed) |
| **Error handling** | Servlet `sendError()` + error JSP | Ktor `StatusPages` plugin |
| **Dependency injection** | Manual (servlet `init()` + context listener) | Manual (constructor parameters wired in `Application.kt`) |
| **Concurrency model** | Blocking threads (Servlet thread pool) | Coroutines (`suspend` functions, `Dispatchers.IO` for DB) |
| **Unit tests** | JUnit 5, AssertJ, Mockito | JUnit 5, Ktor `testApplication` |
| **Route/integration tests** | TestContainers (real PostgreSQL) | In-memory fakes (`FakePersonRepository`, `FakeTrialService`) — no database required |
| **HTML assertions in tests** | Not present | Jsoup |
| **JS unit tests** | Karma + Jasmine | Not applicable (no custom JS) |
| **E2E tests** | Playwright | Playwright |
| **NHS Design System assets** | frontend-maven-plugin + npm | node-gradle plugin + npm |
| **Static assets** | Served by Tomcat from `webapp/nhsuk/` | Served by Ktor from `resources/static/nhsuk/` |
| **Containerisation** | `Dockerfile.tomcat` (single stage) | `Dockerfile` (three-stage: node → jdk → jre) |
| **Dev database** | Docker Compose (port 5432) | Docker Compose (port 5435) |
| **CI** | GitHub Actions | GitHub Actions |
| **Lines of code (approx.)** | ~1,800 (Java + JSP) | ~1,200 (Kotlin) |

---

## Key architectural differences

### Request handling

The Java project uses the Servlet API: each route is a `@WebServlet` class with `doGet()` and `doPost()` methods. Tomcat manages the lifecycle and thread pool.

The Kotlin project uses Ktor's coroutine-based routing DSL. Each route is a suspending lambda. Blocking database calls are dispatched to `Dispatchers.IO` via `newSuspendedTransaction`, keeping the Netty event loop free.

### Templating

The Java project renders HTML via JSP files with JSTL tags. The template engine runs server-side but is a separate file type from the business logic.

The Kotlin project uses the kotlinx.html DSL — HTML is constructed directly in Kotlin code as a type-safe builder. There are no separate template files; layout, components, and page content are all Kotlin functions.

### HTMX vs full page reloads

The Java project submits forms via standard HTML POST, which causes a full page reload on every submission (including validation errors).

The Kotlin project uses HTMX: forms are submitted as AJAX requests. On success, the server returns an `HX-Redirect` header and HTMX navigates the browser. On validation failure, the server returns a partial HTML fragment (just the form) and HTMX swaps it in place — no full reload.

### Testing strategy

The Java project tests the data access layer using TestContainers, which spins up a real PostgreSQL container for each test run. This gives high confidence but requires Docker and adds significant test execution time.

The Kotlin project uses hand-written fakes (`FakePersonRepository`, `FakeTrialService`) injected via constructor parameters. Route tests run entirely in-memory with `testApplication` — no Docker, no database, sub-second execution. The trade-off is that the fake implementations must be kept in sync with the real ones manually.

### Sessions and PII

Both projects store only a session identifier in the client cookie and keep session data server-side, ensuring NHS numbers never appear in browser storage or logs.

The Java project uses the standard Jakarta `HttpSession`, managed by Tomcat.

The Kotlin project uses Ktor's `SessionStorageMemory` with a custom kotlinx.serialization JSON serializer so the `PersonSessionData` object (which includes the NHS number) is stored in the server's memory map, not serialised into the cookie.

### Packaging and deployment

The Java project produces a WAR file that must be deployed into a running Tomcat instance. The runtime and application are separate.

The Kotlin project produces a self-contained fat JAR (via the Shadow plugin) that embeds Netty. It can be run with a plain `java -jar` command — no application server required. This simplifies the Dockerfile and removes the Tomcat layer entirely.

---

## Where Kotlin has a meaningful advantage

### 1. Data classes eliminate model boilerplate

Java requires a no-arg constructor, eight getters, eight setters, and a hand-written `toString()` to represent a simple form object. Kotlin's `data class` generates all of it — including `equals()`, `hashCode()`, `copy()`, and `toString()` — from a single declaration.

**Java — `Person.java` (~80 lines):**
```java
public class Person {
  private String nhsNumber; // PII — do not log
  private String firstName;
  private String lastName;
  private String email;
  private LocalDate dob;
  private String gender;

  public Person() {}

  public String getNhsNumber() { return nhsNumber; }
  public void setNhsNumber(String v) { this.nhsNumber = v; }
  public String getFirstName()  { return firstName; }
  public void setFirstName(String v)  { this.firstName = v; }
  // ... 8 more getters/setters ...

  @Override
  public String toString() {
    return "Person{id=" + id + ", name=" + firstName + " " + lastName + "}";
  }
}
```

**Kotlin — `Person.kt` (8 lines):**
```kotlin
data class PersonSessionData(
    val nhsNumber: String,   // PII — never logged
    val firstName: String,
    val lastName: String,
    val email: String,
    val dob: String,
    val gender: String,
)
```

### 2. Validation reads as business rules, not control flow

The Java validator mutates a `Person` output parameter and uses early returns inside separate private methods for each field. The Kotlin version uses a `when` expression and a `buildValidation` DSL that collects all errors in a single pass, making the rules read more like a specification.

**Java — field validated by a separate private method with mutation:**
```java
private void validateDob(String day, String month, String year,
                         ValidationResult result, Person person) {
    boolean dayMissing   = day.isEmpty();
    boolean monthMissing = month.isEmpty();
    boolean yearMissing  = year.isEmpty();

    if (dayMissing && monthMissing && yearMissing) {
        result.addError("dob", "Enter a date of birth"); return;
    }
    if (dayMissing)   { result.addError("dob", "Date of birth must include a day");   return; }
    if (monthMissing) { result.addError("dob", "Date of birth must include a month"); return; }
    if (yearMissing)  { result.addError("dob", "Date of birth must include a year");  return; }

    LocalDate dob;
    try {
        dob = LocalDate.of(Integer.parseInt(year),
                           Integer.parseInt(month),
                           Integer.parseInt(day));
    } catch (NumberFormatException | DateTimeException e) {
        result.addError("dob", "Enter a real date of birth"); return;
    }
    if (!dob.isBefore(LocalDate.now()))
        { result.addError("dob", "Date of birth must be in the past"); return; }
    if (!dob.isBefore(LocalDate.now().minusYears(18)))
        { result.addError("dob", "Participant must be at least 18 years old"); return; }

    person.setDob(dob);
}
```

**Kotlin — same logic as inline `when` expression, no mutation:**
```kotlin
when {
    dayMissing && monthMissing && yearMissing -> error("dob", "Enter a date of birth")
    dayMissing   -> error("dob", "Date of birth must include a day")
    monthMissing -> error("dob", "Date of birth must include a month")
    yearMissing  -> error("dob", "Date of birth must include a year")
    else -> {
        val dob = tryParseDate(form.dobDay, form.dobMonth, form.dobYear)
        val today = LocalDate.now()
        when {
            dob == null          -> error("dob", "Enter a real date of birth")
            !dob.isBefore(today) -> error("dob", "Date of birth must be in the past")
            !dob.isBefore(today.minusYears(18)) ->
                error("dob", "Participant must be at least 18 years old")
        }
    }
}
```

### 3. Destructuring replaces mutable output parameters

The Java validator populates a `Person` object passed in by the caller — a mutable output parameter — because Java methods can only return one value. Kotlin returns a `Pair` which is destructured at the call site.

**Java:**
```java
Person person = new Person();
ValidationResult result = validator.validate(params, person);
// person is only valid if !result.hasErrors()
if (!result.hasErrors()) {
    session.setAttribute("pendingPerson", person);
}
```

**Kotlin:**
```kotlin
val (result, person) = validatePerson(form)
// person is null when result.hasErrors — enforced by the type system
if (!result.hasErrors) {
    call.sessions.set(EnrolmentSession(pendingPerson = person))
}
```

### 4. Null safety is enforced by the compiler

In the Java validator, `request.getParameter()` can return `null`. The code guards against this with a private `trim()` helper that converts `null` to `""`. If that helper were ever forgotten, a `NullPointerException` would surface at runtime.

In Kotlin, `String?` (nullable) and `String` (non-nullable) are distinct types. A nullable value cannot be passed where a non-nullable is expected without an explicit null check — the compiler refuses to compile the code.

---

## Code comparison: JSP + Servlet vs kotlinx.html + Ktor

### Form rendering — one input field

Rendering a single NHS number field with a hint, inline error, and correct `aria-describedby` takes **28 lines of JSP** with nested JSTL conditionals and escaped string expressions:

**JSP (`person.jsp`):**
```jsp
<div class="nhsuk-form-group
     <c:if test='${not empty errors["nhsNumber"]}'>nhsuk-form-group--error</c:if>">
  <label class="nhsuk-label nhsuk-label--m" for="nhsNumber">NHS number</label>
  <div class="nhsuk-hint" id="nhsNumber-hint">
    This is a 10 digit number (like 999 123 4567) that you can find on an
    NHS letter, prescription or in the NHS App
  </div>
  <c:if test="${not empty errors['nhsNumber']}">
    <span class="nhsuk-error-message" id="nhsNumber-error">
      <span class="nhsuk-u-visually-hidden">Error: </span>
      <c:out value="${errors['nhsNumber']}"/>
    </span>
  </c:if>
  <input class="nhsuk-input nhsuk-input--width-10
         <c:if test='${not empty errors[\"nhsNumber\"]}'>nhsuk-input--error</c:if>"
         id="nhsNumber" name="nhsNumber" type="text"
         inputmode="numeric" autocomplete="off"
         aria-describedby="nhsNumber-hint
           <c:if test='${not empty errors[\"nhsNumber\"]}'>nhsNumber-error</c:if>"
         value="<c:out value='${fields["nhsNumber"]}'/>">
</div>
```

The same field in Kotlin is **7 lines** — a named-parameter function call. The component helper handles error state, `aria-describedby`, and CSS class toggling once, reused everywhere:

**Kotlin (`PersonHtml.kt`):**
```kotlin
nhsTextInput(
    id = "nhsNumber",
    label = "NHS number",
    value = form.nhsNumber,
    error = errors["nhsNumber"],
    hint = "This is a 10 digit number (like 999 123 4567) that you can find on an NHS letter, prescription or in the NHS App",
    autocomplete = "off",
)
```

### Route handler — POST with validation

The full POST handler in the Servlet is spread across `doPost()` plus a private `extractParams()` helper, uses request attributes to pass data to the JSP, and calls `forward()` vs `sendRedirect()` depending on the outcome.

**Java (`PersonServlet.java`):**
```java
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    Map<String, String> params = extractParams(request);
    Person person = new Person();
    ValidationResult result = validator.validate(params, person);

    if (!result.hasErrors()) {
        if (personDao.existsByNhsNumber(person.getNhsNumber())) {
            result.addError("nhsNumber", "A participant with this NHS number is already registered");
        }
        if (personDao.existsByEmail(person.getEmail())) {
            result.addError("email", "A participant with this email address is already registered");
        }
    }

    if (result.hasErrors()) {
        request.setAttribute("errors", result.getErrors());
        request.setAttribute("fields", params);
        request.getRequestDispatcher("/WEB-INF/views/person.jsp").forward(request, response);
        return;
    }

    HttpSession session = request.getSession();
    session.setAttribute(SESSION_PERSON, person);
    response.sendRedirect(request.getContextPath() + "/medical");
}
```

The Kotlin version is a single suspending lambda. It uses Ktor's typed session API, checks the `HX-Request` header to handle both HTMX and plain-browser requests from the same handler, and returns structured HTML via the kotlinx.html DSL rather than forwarding to a separate file:

**Kotlin (`PersonRoutes.kt`):**
```kotlin
post("/person") {
    val form = call.receiveParameters().toPersonForm()
    val (result, person) = validatePerson(form)

    if (!result.hasErrors) {
        if (personRepository.existsByNhsNumber(person!!.nhsNumber))
            result.errors["nhsNumber"] = "A participant with this NHS number is already registered"
        if (personRepository.existsByEmail(person.email))
            result.errors["email"] = "A participant with this email address is already registered"
    }

    if (result.hasErrors) {
        val isHtmx = call.request.headers["HX-Request"] == "true"
        call.respondHtml {
            if (isHtmx) body { personFormContent(form, result.errors) }
            else nhsPage("Participant details") { personFormContent(form, result.errors) }
        }
        return@post
    }

    call.sessions.set(EnrolmentSession(pendingPerson = person))
    if (call.request.headers["HX-Request"] == "true") {
        call.response.headers.append("HX-Redirect", "/medical")
        call.respond(HttpStatusCode.OK)
    } else {
        call.respondRedirect("/medical")
    }
}
```

### HTMX: progressive enhancement without JavaScript

The Java project requires a full page reload on every form submission. A separate `validation.js` file provides client-side pre-validation and was tested with Karma/Jasmine.

The Kotlin project adds HTMX with three HTML attributes on the `<form>` tag. No custom JavaScript was written — HTMX handles the AJAX submission, the partial swap on error, and the redirect on success automatically:

**Kotlin (`PersonHtml.kt`):**
```kotlin
form(action = "/person", method = FormMethod.post) {
    id = "person-form"
    attributes["hx-post"]     = "/person"   // submit via AJAX instead of full POST
    attributes["hx-target"]   = "#person-form" // swap only the form element on error
    attributes["hx-swap"]     = "outerHTML"
    attributes["hx-push-url"] = "true"       // keep browser history correct
    // ...
}
```

On success the server returns `HX-Redirect: /medical` and HTMX navigates the browser.  
On validation failure the server returns the form fragment alone and HTMX swaps it in — the page header, footer, and navigation never re-render.
