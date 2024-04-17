package ru.lvmlabs.neuronum.familia.selenium.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import ru.lvmlabs.neuronum.sharedmodules.selenium.domain.Browser;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@Slf4j
public class LoginPage extends Page {
    public LoginPage(Browser browser, String loginUrl) {
        super(browser, loginUrl);
        browser.get(loginUrl);

        log.trace("LoginPage has been initialized");
    }

    public void inputEmail(String email) {
        WebElement emailInput = browser.Wait().until(presenceOfElementLocated(cssSelector(
                "input[class='b-auth__emailinput b-auth__emailinput--js_init']"
        )));

        emailInput.clear();
        emailInput.sendKeys(email);

        log.trace("Email {} has been entered", email);
    }

    public void inputPassword(String password) {
        WebElement passwordInput = browser.Wait().until(presenceOfElementLocated(cssSelector(
                "input[class='b-auth__passwordinput b-auth__passwordinput--js_init']"
        )));

        passwordInput.clear();
        passwordInput.sendKeys(password);

        log.trace("Password {} has been entered", password);
    }

    public void login() {
        WebElement loginButton = browser.waitWhileNotClickable(cssSelector(
                "div[class='b-auth__button b-auth__button--js_init']"
        ));

        loginButton.click();

        // wait for login
        browser.waitWhileNotClickable(xpath("//span[text()='Поддержка']"));

        log.trace("Login completed successfully");
    }
}
