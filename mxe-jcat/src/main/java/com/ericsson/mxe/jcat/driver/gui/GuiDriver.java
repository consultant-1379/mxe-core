package com.ericsson.mxe.jcat.driver.gui;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.ericsson.jcat.fw.utils.JcatLogDirectory;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GuiDriver implements Closeable {

    private static final String WEBDRIVER_GECKO_DRIVER_PROP_KEY = "webdriver.gecko.driver";
    private static final String WEBDRIVER_GECKO_DRIVER_DEFAULE_VALUE = "/usr/bin/geckodriver";
    private static final String SIDEBAR_NAVIGATION_BUTTON_ID = "AppBar-menu-toggle";
    private static final String LOGIN_BUTTON_ID = "submit";
    private static final String USERNAME_FIELD_ID = "username";
    private static final String PASSWORD_FIELD_ID = "password";
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiDriver.class);
    private String endpoint;
    private RemoteWebDriver driver;
    private BrowserType browserType;

    public enum BrowserType {
        FIREFOX, CHROME
    }

    public GuiDriver(String endpoint, BrowserType browserType) {
        this.endpoint = endpoint;
        this.browserType = browserType;
    }

    public boolean connect() {
        try {
            if (System.getProperty(WEBDRIVER_GECKO_DRIVER_PROP_KEY) == null) {
                System.setProperty(WEBDRIVER_GECKO_DRIVER_PROP_KEY, WEBDRIVER_GECKO_DRIVER_DEFAULE_VALUE);
            }
            if (browserType == null) {
                throw new IllegalArgumentException("Browser type must be set");
            }
            switch (browserType) {
                case FIREFOX:
                    FirefoxProfile profile = new FirefoxProfile();
                    profile.setAcceptUntrustedCertificates(true);
                    profile.setAssumeUntrustedCertificateIssuer(false);
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxOptions.addArguments("--headless");
                    firefoxOptions.setProfile(profile);
                    driver = new FirefoxDriver(firefoxOptions);
                    break;
                case CHROME:
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                    chromeOptions.addArguments("--headless");
                    driver = new ChromeDriver(chromeOptions);
                    break;
            }
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            driver.get(endpoint);
        } catch (Throwable e) {
            LOGGER.error("Failed to connect", e);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private WebElement findShadowRoot(List<String> selectorList) {
        WebElement root =
                (WebElement) driver.executeScript("return document.querySelector(arguments[0]);", selectorList.get(0));
        for (String selector : selectorList.subList(1, selectorList.size())) {
            root = (WebElement) driver.executeScript("return arguments[0].shadowRoot.querySelector(arguments[1]);",
                    root, selector);
        }
        return root;
    }

    public void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep interrupted", e);
        }
    }

    public void login(String username, String password) {
        Actions actions = new Actions(driver);
        WebElement usernameField = driver.findElement(By.id(USERNAME_FIELD_ID));
        actions.moveToElement(usernameField).click().build().perform();
        usernameField.sendKeys(username);
        WebElement passwordField = driver.findElement(By.id(PASSWORD_FIELD_ID));
        actions.moveToElement(passwordField).click().build().perform();
        passwordField.sendKeys(password);
        WebElement loginButton = driver.findElement(By.className(LOGIN_BUTTON_ID));
        actions.moveToElement(loginButton).click().build().perform();
    }

    public void logout() {
        sleep(1);
        findElementByIdInShadowroot(Arrays.asList("eui-container", "eui-container-system-bar"), "bt-user-icon").click();
        sleep(5);
        findShadowRoot(Arrays.asList("eui-container", "eui-container-layout-holder", "eui-system-panel",
                "eui-user-settings-panel", "eui-base-v0-button")).click();
    }

    public WebElement findElementByIdInShadowroot(List<String> shadowRootSelectors, String id) {
        WebElement root = findShadowRoot(shadowRootSelectors);
        return (WebElement) driver.executeScript("return arguments[0].shadowRoot.getElementById(arguments[1]);", root,
                id);
    }

    public WebElement findNavbarMenuItem(String menuText) {
        WebElement menu = findShadowRoot(Arrays.asList("eui-container", "eui-container-layout-holder", "eui-app-nav",
                "eui-menu-panel", "eui-base-v0-tree"));
        return (WebElement) driver.executeScript(
                "return Array.from(arguments[0].querySelectorAll(\"eui-base-v0-tree-item\")).find(function(element){return element.textContent === \"Model List\" });",
                menu);
    }

    public void clickById(String id) {
        driver.findElement(By.id(id)).click();
    }

    public void openModelPage() {
        findElementByIdInShadowroot(Arrays.asList("eui-container", "eui-container-layout-holder", "eui-app-bar"),
                SIDEBAR_NAVIGATION_BUTTON_ID).click();
        sleep(1);
        findNavbarMenuItem("Model List").click();
        findElementByIdInShadowroot(Arrays.asList("eui-container", "eui-container-layout-holder", "eui-app-bar"),
                SIDEBAR_NAVIGATION_BUTTON_ID).click();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void getSreenshot() {
        File SrcFile = driver.getScreenshotAs(OutputType.FILE);
        // Move image file to new destination
        String fileName = "screenshot.png";
        File DestFile = new File(JcatLogDirectory.getInstance().getJcatLogDirectory() + File.separator + fileName);
        // Copy file at destination
        try {
            FileUtils.copyFile(SrcFile, DestFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save file", e);
        }
    }

    public String getPageSource() {
        return driver.getPageSource();
    }
}
