import { test, expect } from "@playwright/test";

test.describe("Dashboard", () => {
  test("dashboard is accessible from the header navigation", async ({ page }) => {
    await page.goto("/");
    await page.click("a[href='/dashboard']");
    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.locator("h1")).toContainText("Blood pressure dashboard");
  });

  test("dashboard shows enrolled participants after enrolment", async ({ page }) => {
    // Enrol a participant
    const nhs = "1112223339";
    const email = `dash_test_${Date.now()}@example.com`;

    await page.goto("/person");
    await page.fill("#nhsNumber", nhs);
    await page.fill("#firstName", "Bob");
    await page.fill("#lastName", "Jones");
    await page.fill("#email", email);
    await page.fill("#dobDay", "10");
    await page.fill("#dobMonth", "3");
    await page.fill("#dobYear", "1985");
    await page.check("#gender_Male");
    await page.click("button[type=submit]");

    await page.waitForURL("**/medical");
    await page.fill("#bpSystolic", "135");
    await page.fill("#bpDiastolic", "85");
    await page.selectOption("#treatment", "Placebo");
    await page.click("button[type=submit]");
    await page.waitForURL("**/thankyou");

    // Check dashboard
    await page.goto("/dashboard");
    const table = page.locator(".nhsuk-table");
    await expect(table).toBeVisible();
    await expect(table).toContainText("Bob");
    await expect(table).toContainText("135");
    await expect(table).toContainText("Placebo");
  });
});
