import { test, expect } from "@playwright/test";

test.describe("Dashboard", () => {
  test("dashboard is accessible from the header navigation", async ({ page }) => {
    // Navigate to the home page first so the nav link is rendered, then click it.
    // Use page.goto for the actual navigation rather than relying on HTMX boost:
    // if the route returns 500 (e.g. DB not ready), HTMX swallows the error silently
    // and waitForURL would time out with the browser still on the original URL.
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/dashboard/);
    // Dashboard uses an h2 inside the chart card rather than a page h1
    await expect(page.locator("h2").first()).toContainText("Blood Pressure Plot");
  });

  test("dashboard shows enrolled participants after enrolment", async ({ page }) => {
    // Enrol a participant.
    // Use a NHS number that is NOT shared with any enrolment.spec.ts test to avoid
    // a "duplicate NHS number" collision when the test files run sequentially
    // (dashboard.spec.ts executes first because files are sorted alphabetically).
    const nhs = "4444333322";
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
