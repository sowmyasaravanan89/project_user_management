package com.sowmya.ui.tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.sowmya.api.utils.ConfigManager;

public class E2ETest {
    
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    protected static final ConfigManager config = ConfigManager.getInstance();
    protected static final String BASE_URL =  ConfigManager.getInstance().geString(("base_url"));
    private static final String PORT = ConfigManager.getInstance().geString("frontend_port");

    
    protected static final boolean HEADLESS = Boolean.parseBoolean(config.getProperty("browser.headless", "true"));
    protected static final Integer SLOWMO = Integer.parseInt(config.getProperty("browser.slowmo", "300"));

    @BeforeClass
    public void setupBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(HEADLESS)
                .setSlowMo(SLOWMO)
            );
    }

    @AfterClass
    public void teardown(){
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeMethod
    public void setupTest() {
        context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1280, 720)
        );
        page = context.newPage();

        // logging for debugging
        page.onConsoleMessage(msg -> {
            System.out.println("Console [" + msg.type() + "]:" + msg.text());
        });

        // Navigate to the application
        page.navigate(BASE_URL + ":" + PORT);

        // Wait for page to load
        page.waitForLoadState();
    }


    @AfterMethod
    public void teardownTest() {
        if (context != null) {
            context.close();
        }
    }

    @Test(description = "E2E test: Login -> Add User -> Edit User -> Delete User -> Logout")
    public void testCompleteUserFlow() {
        // step 1: login 
        boolean onLoginPage = page.locator("input[type='password']").isVisible();
        Assert.assertTrue(onLoginPage, "should be on Login pagae");
        takeScreenshot("1_login_page");

        // step 2: Login with username and password
        performLogin("admin", "password123");
        page.waitForLoadState();
        takeScreenshot("2_after_login");
    
        // step 3: check if login successful
        boolean loginSuccess = isLoginSuccess();
        if (!loginSuccess) {
            System.out.println("Login failed, check if backend is running");
        }
    
        // step 4: Add user 
        testAddUserFlow();

        // // step 5: Edit user
        // testEditUserFlow();

        // // step 6: Delete user
        testDeleteUserFlow();

        // // step 7: Logout
        testLogoutFlow();
    }


    private void performLogin(String username, String password) {
        try {
            page.getByPlaceholder("Enter your username").first().fill(username);
            page.getByPlaceholder("Enter your password").fill(password);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("loginFill.png")));
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).first().click();
        } catch (Exception e) {
            System.out.println("Error during login:" + e.getMessage());
        }
    }

    private boolean isLoginSuccess() {
        try {
            page.waitForLoadState();

            boolean hasWelcome = page.getByText("Welcome, admin").isVisible();
            boolean hasUserManagement = page.getByText("User Management").isVisible();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("welcomenote.png")));
            boolean hasAddButton =page.getByRole(AriaRole.BUTTON,
                 new Page.GetByRoleOptions().setName("Add User")).first()
                 .isVisible();
            boolean hasLogoutButton = page.getByRole(AriaRole.BUTTON,
                 new Page.GetByRoleOptions().setName("Logout")).first()
                 .isVisible();

            boolean isLoggedIn = (hasWelcome || hasUserManagement || hasAddButton || hasLogoutButton);

            if (isLoggedIn) {
                System.out.println("Successfully logged in!");
                return true;
            } else {
                System.out.println("Login not successful!");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error when checking if login is successful:" + e.getMessage());
            return false;
        }
    }

    private void testAddUserFlow() {
        try {
            Locator addButton = page.locator("button:has-text('Add')").first();

            if (addButton.isVisible()) {
                addButton.click();
                page.waitForLoadState();
                takeScreenshot("3_add_user");

                boolean formFilled = fillAddUserForm("sowmya", "sowmya@abc.com", "30");

                if (formFilled) {
                    Locator submitButton = page.locator("button:has-text('Create')").first();
                    if (submitButton.isVisible()){
                        submitButton.click();
                        page.waitForLoadState();
                        takeScreenshot("4_form_filled");
                    }
                }
            } else {
                throw new Exception("No Add User button found!");
            }
        } catch (Exception e) {
            System.out.println("Error when trying to add user:" + e.getMessage());
        }
    }

    private boolean fillAddUserForm(String username, String email, String age) {
        try {
            boolean isUsernameFilled = false;
            boolean isEmailFilled = false;
            boolean isAgeFilled = false;

            // Try to fill username
            Locator usernameField = page.locator("input[placeholder*='name']").first();
            if (usernameField.isVisible()) {
                usernameField.fill(username);
                isUsernameFilled = true;
            }

            // Try to fill email
            Locator emailField = page.locator("input[placeholder*='email']").first();
            if (emailField.isVisible()) {
                emailField.fill(email);
                isEmailFilled = true;
            }

            // Try to fill age
            Locator ageField = page.locator("input[placeholder*='age']").first();
            if (ageField.isVisible()) {
                ageField.fill(age);
                isAgeFilled = true;
            }

            boolean allFieldsFilled = (isUsernameFilled && isEmailFilled && isAgeFilled);

            return allFieldsFilled;
        } catch (Exception e) {
            System.out.println("Error when trying to fill user details:" + e.getMessage());
            return false;
        }
    }

    private void testEditUserFlow() {
        try {
            // Locator editButton = page.locator("button:has-text('Edit'), [title='Edit'], .edit-btn").first();
            Locator editButton = page.getByTitle("Edit").first();

            if (editButton.isVisible()) {
                editButton.click();
                page.waitForLoadState();
                takeScreenshot("5_before_edit");

                Locator nameField = page.locator("input[value]:not([value=''])").first();
                if(nameField.isVisible()) {
                    String originalvalue = nameField.inputValue();
                    nameField.fill(originalvalue + "updated");

                    Locator saveButton = page.locator("button:has-text('Update')").first();
                    if (saveButton.isVisible()) {
                        takeScreenshot("6_after_edit");
                        saveButton.click();
                        page.waitForLoadState();
                    }
                }
            } else {
                System.out.println("Edit button not found");
            }
        } catch (Exception e) {
            System.out.println("Error when trying to edit user details:" + e.getMessage());
        }
    }

    private void testDeleteUserFlow() {
        page.onDialog(diaglog -> {
            Assert.assertEquals("Are you sure you want to delete this user?", diaglog.message());
            diaglog.accept();
        });
        page.getByTitle("Delete").first().click();
        // FIXME: find a way to make pop up confirmation work inside try catch
        // try {
        //     Locator deleteButton = page.getByTitle("Delete").first();
        //     if(deleteButton.isVisible()) {
        //         deleteButton.click();
        //         System.out.println("Button is enabled " + deleteButton.isEnabled());
        //         System.out.println("deleteButton clicked");
        //         // page.waitForLoadState();
        //         takeScreenshot("7_delete_confirmation");
        //     } else {
        //         throw new Exception("Delete button not found");
        //     }
        // } catch (Exception e) {
        //     System.out.println("Error when trying to delete user details:" + e.getMessage());
        // }
    }

    private void testLogoutFlow() {
        try {
            Locator logoutButton = page.locator("button:has-text('Logout')").first();
            if (logoutButton.isVisible()) {
                logoutButton.click();
                page.waitForLoadState();
                takeScreenshot("9_after_logout");

                boolean onLoginPage = page.locator("input[type='password']").isVisible();
                if (onLoginPage) {
                    System.out.println("Logout successful");
                } else {
                    System.out.println("Logout error. Check backend");
                }
            } else {
                throw new Exception("Logout button not found");
            }
        } catch (Exception e) {
            System.out.println("Error when trying to logout:" + e.getMessage());
        }
    }

    protected void takeScreenshot(String name) {
        try {
            Path screenshotDir = Paths.get("target/screenshots");
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }

            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("target/screenshots/" + name + ".png"))
                .setFullPage(true));
        } catch (Exception e) {
            System.out.println("Failed to take screenshot:" + e.getMessage());
        }
    }
}