import { test, expect } from "@playwright/test";

// Valid test NHS numbers (Modulus 11 compliant)
const VALID_NHS = "9434765919";
const VALID_NHS_2 = "1112223339";

async function fillPersonForm(
  page: Parameters<typeof test>[1] extends (args: { page: infer P }) => unknown ? P : never,
  opts: { nhsNumber?: string; email?: string } = {}
) {
  const nhs = opts.nhsNumber ?? VALID_NHS;
  const email = opts.email ?? `${nhs}@example.com`;

  await page.fill("#nhsNumber", nhs);
  await page.fill("#firstName", "Jane");
  await page.fill("#lastName", "Doe");
  await page.fill("#email", email);
  await page.fill("#dobDay", "15");
  await page.fill("#dobMonth", "6");
  await page.fill("#dobYear", "1990");
  await page.check("#gender_Female");
}

async function fillMedicalForm(page: Parameters<typeof test>[1] extends (args: { page: infer P }) => unknown ? P : never) {
  await page.fill("#bpSystolic", "120");
  await page.fill("#bpDiastolic", "80");
  await page.selectOption("#treatment", "Treatment A");
}

test.describe("Enrolment flow", () => {
  test("completes two-step enrolment and shows thank you page", async ({ page }) => {
    await page.goto("/person");
    await expect(page.locator("h1")).toContainText("Participant details");

    await fillPersonForm(page, { nhsNumber: VALID_NHS_2, email: "e2e_complete@example.com" });
    await page.click("button[type=submit]");

    await page.waitForURL("**/medical");
    await expect(page.locator("h1")).toContainText("Clinical measurements");

    await fillMedicalForm(page);
    await page.click("button[type=submit]");

    await page.waitForURL("**/thankyou");
    await expect(page.locator(".nhsuk-panel__title")).toContainText("Registration complete");
  });

  test("shows error summary when NHS number is blank", async ({ page }) => {
    await page.goto("/person");
    await page.fill("#firstName", "Jane");
    await page.fill("#lastName", "Doe");
    await page.fill("#email", "noemail@example.com");
    await page.fill("#dobDay", "15");
    await page.fill("#dobMonth", "6");
    await page.fill("#dobYear", "1990");
    await page.check("#gender_Female");
    await page.click("button[type=submit]");

    await expect(page.locator(".nhsuk-error-summary")).toBeVisible();
    await expect(page.locator(".nhsuk-error-summary")).toContainText("NHS number");
  });

  test("shows error for invalid NHS Modulus 11 number", async ({ page }) => {
    await page.goto("/person");
    await fillPersonForm(page, { nhsNumber: "9434765910" }); // bad check digit
    await page.click("button[type=submit]");

    await expect(page.locator("#nhsNumber-error")).toBeVisible();
    await expect(page.locator("#nhsNumber-error")).toContainText("valid NHS number");
  });

  test("shows inline error for each missing field", async ({ page }) => {
    await page.goto("/person");
    await page.click("button[type=submit]");

    await expect(page.locator(".nhsuk-error-summary")).toBeVisible();
    for (const field of ["nhsNumber", "firstName", "lastName", "email", "dob", "gender"]) {
      await expect(page.locator(`#${field}-error, [id^="${field}-error"]`).first()).toBeVisible();
    }
  });

  test("shows error for duplicate NHS number", async ({ page }) => {
    // Enrol once
    await page.goto("/person");
    await fillPersonForm(page, {
      nhsNumber: "9434765919",
      email: "first_enrol@example.com",
    });
    await page.click("button[type=submit]");
    await page.waitForURL("**/medical");
    await fillMedicalForm(page);
    await page.click("button[type=submit]");
    await page.waitForURL("**/thankyou");

    // Try to enrol same NHS number again
    await page.goto("/person");
    await fillPersonForm(page, {
      nhsNumber: "9434765919",
      email: "second_attempt@example.com",
    });
    await page.click("button[type=submit]");

    await expect(page.locator(".nhsuk-error-summary")).toBeVisible();
    await expect(page.locator(".nhsuk-error-summary")).toContainText("already registered");
  });

  test("back link on medical step returns to person form", async ({ page }) => {
    await page.goto("/person");
    await fillPersonForm(page);
    await page.click("button[type=submit]");
    await page.waitForURL("**/medical");

    await page.click("a[href='/person']");
    await expect(page).toHaveURL(/\/person/);
  });

  test("navigating directly to /medical without session redirects to /person", async ({ page }) => {
    await page.goto("/medical");
    await expect(page).toHaveURL(/\/person/);
  });

  test("shows error for underage participant", async ({ page }) => {
    await page.goto("/person");
    await fillPersonForm(page);
    await page.fill("#dobYear", new Date().getFullYear().toString()); // current year → under 18
    await page.click("button[type=submit]");

    await expect(page.locator(".nhsuk-error-summary")).toBeVisible();
    await expect(page.locator(".nhsuk-error-summary")).toContainText("18 years");
  });
});
