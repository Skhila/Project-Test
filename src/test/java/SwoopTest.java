import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.*;

public class SwoopTest{
//    Setup Driver
    public WebDriver driver;
    public Actions action;
    public JavascriptExecutor js;

    @BeforeTest
    @Parameters("browser")
    public void setup(String browser) throws Exception{
        if(browser.equalsIgnoreCase("chrome")){
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();

        }else if (browser.equalsIgnoreCase("edge")){
            WebDriverManager.edgedriver().setup();
            driver = new EdgeDriver();
        }else {
            throw new Exception("Browser is not correct");
        }

        driver.manage().window().maximize();
        action = new Actions(driver);
        js = (JavascriptExecutor) driver;
    }

//    Helper Function
    public void performScrollAndClick(WebElement target){
        js.executeScript("arguments[0].scrollIntoView(true);" +
                "arguments[0].click()", target);
    }

//    The test itself
    @Test
    public void swoopTest(){
//        Navigate to Swoop Website and accept cookies
        driver.get("https://www.swoop.ge");

////        Locate and click on the "კინო" button
        String movieBtnExpectedText = "კინო";
        WebElement movieButton = driver.findElement(By.xpath("//li[contains(@class,'MoreCategories')]/a[contains(normalize-space(), "+movieBtnExpectedText+")]"));
        action.moveToElement(movieButton).click().build().perform();

//        Locate the first movie deal which has EastPoint cinema and click buy
        List<WebElement> movieElements = driver.findElements(By.xpath("//div[@class='movies-deal']"));
        int movieElementsCount = movieElements.size();
        String cinemaName = "კავეა ისთ ფოინთი";
        //    Iterate through all movies and find the first movie with EastPoint seance. If a movie doesn't
        //    have east EastPoint, move back to the previous page and check the next movie.
        //    Stop when the valid movie is found.
        for(int i = 1; i <= movieElementsCount; i++){
            WebElement currentMovie = driver.findElement(By.xpath("//div[@class='movies-deal']["+i+"]"));

            action.moveToElement(currentMovie).build().perform();
            WebElement buyButton = currentMovie.findElement(By.cssSelector("div.cinema-hover a:nth-child(2)"));
            new WebDriverWait(driver, 3).until(ExpectedConditions.elementToBeClickable(buyButton)).click();

            try{
                WebElement eastPointElement = driver.findElement(By.xpath("//li/a[text()='" + cinemaName + "']"));
                performScrollAndClick(eastPointElement);
                System.out.println("Found a movie with EastPoint seances 🥳");
                break;
            } catch (NoSuchElementException e){
                driver.navigate().back();
                System.out.println("Current movie doesn't have seances in EastPoint cinema!!!");
            }
        }

//        Locate the last date and click
        WebElement lastDate = driver.findElement(By.cssSelector("div[aria-hidden=false] div ul li:last-child a"));
        performScrollAndClick(lastDate);

//        Check and ensure that only Cavea EastPoint options were returned
        List<WebElement> caveaSeances = driver.findElements(By.cssSelector("div[class*='seanse-details'][aria-expanded=true][aria-hidden=false] a"));
        Iterator<WebElement> iterator = caveaSeances.iterator();

        //    Iterate through all present seances and ensure that all of them are located in Cavea EastPoint cinema.
        while (iterator.hasNext()) {
            WebElement seance = iterator.next();
            if (seance.getText().trim().isEmpty()) {
                iterator.remove();
            } else {
                try {
                    String cinemaActualName = seance.findElement(By.cssSelector("p.cinema-title")).getText();
                    Assert.assertEquals(cinemaName, cinemaActualName);
                } catch (AssertionError ae) {
                    System.out.println("Seance out of Cavea Eastpoint detected!!!");
                    break;
                }
            }
        }

//        Locate the last seance and click
        WebElement lastSeance = caveaSeances.get(caveaSeances.size()-1);

        //    Get full info about the chosen seance for further checking
        String movieNameBefore = driver.findElement(By.cssSelector("div.info p.name")).getText();
        String seanceDateBefore = lastDate.getText().split(" ")[0];
        String seanceTimeBefore = lastSeance.getText().split("\n")[0];
        String seanceDateTimeBefore = seanceDateBefore + seanceTimeBefore;
        String cinemaTitleBefore = lastSeance.findElement(By.cssSelector("p.cinema-title")).getText();

        //    Click the last seance
        js.executeScript("arguments[0].click()", lastSeance);

//        Locate the popup window and switch to it
        Set<String> windowHandles = driver.getWindowHandles();
        List<String> windowHandlesList = new ArrayList<>(windowHandles);
        String popupWindow = windowHandlesList.get(0);
        driver.switchTo().window(popupWindow);

//        Locate info on the popup window and check if it is correct
        String allInfoSelector = "div.right-content div.content-header";
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(allInfoSelector)));
        WebElement allInfo = driver.findElement(By.cssSelector(allInfoSelector));
        //    Get popup info
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(allInfo));
        String movieNameAfter = allInfo.findElement(By.cssSelector("p.movie-title")).getText();
        String cinemaTitleAfter = allInfo.findElement(By.cssSelector("p:nth-child(2)")).getText();
        String[] seanceDateTimeAfterArr = allInfo.findElement(By.cssSelector("p:nth-child(3)")).getText().split(" ");
        String seanceDateTimeAfter = seanceDateTimeAfterArr[0] + seanceDateTimeAfterArr[seanceDateTimeAfterArr.length - 1];

//        Check if the popup info is equal to the info from chosen seance.
        try{
            Assert.assertEquals(movieNameAfter, movieNameBefore);
            Assert.assertEquals(cinemaTitleAfter, cinemaTitleBefore);
            Assert.assertEquals(seanceDateTimeAfter, seanceDateTimeBefore);
            System.out.println("Information checking completed successfully 🥳");
        }catch (AssertionError ae){
            System.out.println("Invalid Seance!!!");
        }

//        Locate and choose random vacant place
        List<WebElement> vacantPlaces = driver.findElements(By.cssSelector("div.seat.free"));
        int numberOfVacantPlaces = vacantPlaces.size();
        //   Choose random vacant place
        Random random = new Random();
        int randomVacantPlaceNumber = random.nextInt(numberOfVacantPlaces);
        WebElement randomVacantPlace = vacantPlaces.get(randomVacantPlaceNumber);
        performScrollAndClick(randomVacantPlace);

//        Switch to new popup sidebar (Login/Registration)
        windowHandles = driver.getWindowHandles();
        windowHandlesList = new ArrayList<>(windowHandles);
        popupWindow = windowHandlesList.get(0);
        driver.switchTo().window(popupWindow);

//        Click the registration button (Registration form will appear)
        WebElement registerButton = driver.findElement(By.cssSelector("p.register"));
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(registerButton)).click();

//        Let's Register
//        Locate all the form elements
        WebElement registerContainer = driver.findElement(By.cssSelector("div#register-content-1[aria-hidden=false]"));
        WebElement firstNameField = registerContainer.findElement(By.cssSelector("div.dashbord-mail input[name='FirstName']"));
        WebElement lastNameField = registerContainer.findElement(By.cssSelector("div.dashbord-mail input[name='LastName']"));
        WebElement emailField = registerContainer.findElement(By.cssSelector("div.dashbord-mail input[name='Email']"));
        WebElement phoneField = registerContainer.findElement(By.cssSelector("div.dashbord-mail input[name='Phone']"));
        WebElement dateBirthField = registerContainer.findElement(By.cssSelector("div.dashbord-mail input[name='DateBirth']"));
        Select genderSelector = new Select(driver.findElement(By.cssSelector("div.dashbord-mail select[name='Gender']")));
        WebElement passwordField = registerContainer.findElement(By.cssSelector("div.dashbord-mail input[name='Password']"));
        WebElement confirmPasswordField = registerContainer.findElement(By.cssSelector("div.dashbord-mail input[name='ConfirmPassword']"));
        WebElement agreeToTermsCheckbox = registerContainer.findElement(By.cssSelector("div.confidential-politic input[name='IsAgreedTerms']"));
        WebElement finishRegistrationButton = registerContainer.findElement(By.cssSelector("div.dashbord-registration input[value='რეგისტრაცია']"));

//        Complete Registration (Type in invalid mail on purpose)
//        Fill name, surname, email and phone.
        firstNameField.sendKeys("Bacho");
        lastNameField.sendKeys("Skhiladze");
        emailField.sendKeys("InvalidEmail");
        phoneField.sendKeys("555123456");

//        Choose date of birth
        String birthDate = "2003-03-11";
        js.executeScript("arguments[0].valueAsDate= new Date(arguments[1]);", dateBirthField, birthDate);

//        Choose a Gender
        genderSelector.selectByVisibleText("კაცი");

//        Set and confirm password
        String password = "password";
        passwordField.sendKeys(password);
        confirmPasswordField.sendKeys(password);

//        Agree to Terms and Conditions
        performScrollAndClick(agreeToTermsCheckbox);

//        Finish registration
        performScrollAndClick(finishRegistrationButton);

//        Check if the email error message appeared
        String actualErrorMessage = registerContainer.findElement(By.cssSelector("p#physicalInfoMassage")).getText();
        String expectedErrorMessage = "მეილის ფორმატი არასწორია!";
        try {
            Assert.assertEquals(actualErrorMessage, expectedErrorMessage, "Wrong Error Message!!!");
            System.out.println("Expected Error Message Appeared 🥳");
        }catch (AssertionError ae){
            System.out.println("Wrong Error Message Appeared!!!");
        }finally {
            //    Finish the test peacefully 😉
            System.out.println("That's all for this project 🥳👌");
        }
    }

//    Quit Driver
    @AfterTest
    public void tearDown() {
        driver.quit();
    }
}
