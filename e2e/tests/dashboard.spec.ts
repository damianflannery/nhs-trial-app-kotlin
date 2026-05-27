import { test, expect } from "@playwright/test";

test.describe("Dashboard", () => {
  test("dashboard is accessible from the header navigation", async ({ page }) => {
    await page.goto("/");
    await page.click("a[href='/dashboard']");
    await expect(page).toHaveURL(/\/dashboard/);
    // Dashboard uses an h2 inside the chart card rather than a page h1
    await expect(page.locator("h2").first()).toContainText("Blood Pressure Plot");
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
    // Treatment is radio buttons
    await page.check('input[name="treatment"][value="Placebo"]');
    await page.click("button[type=submit]");
    await page.waitForURL("**/thankyou");

    // Check dashboard — data is inlined as JSON and reflected in the stats bar
    await page.goto("/dashboard");
    await expect(page.locator("#bpChart")).toBeVisible();
    await expect(page.locator(".db-stat-value")).not.toContainText("0");

    // Participant data is embedded in the page as window.__bpData JSON
    const content = await page.content();
    expect(content).toContain("Bob");
    expect(content).toContain("135");
    expect(content).toContain("Placebo");
  });
});
