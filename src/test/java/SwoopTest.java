import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

public class SwoopTest {
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

//    @BeforeTest
//    public void setup(){
//        WebDriverManager.chromedriver().setup();
//        driver = new ChromeDriver();
//        action = new Actions(driver);
//        js = (JavascriptExecutor) driver;
//
//        driver.manage().window().maximize();
//    }

    @Test
    public void swoopTest(){
//        Navigate to Swoop Website and accept cookies
        driver.get("https://www.swoop.ge");

////        Locate and click on the "კინო" button
//        WebElement movieButton = driver.findElement(By.xpath("//li[contains(@class,'MoreCategories')][1]/a"));
        String movieBtn = "კინო";
        WebElement movieButton = driver.findElement(By.xpath("//li[contains(@class,'MoreCategories')]/a[contains(normalize-space(), "+movieBtn+")]"));

        action.moveToElement(movieButton).click().build().perform();

//        Locate the first movie deal which has EastPoint cinema and click buy
        List<WebElement> movieElements = driver.findElements(By.xpath("//div[@class='movies-deal']"));
        int movieElementsCount = movieElements.size();
        WebElement firstMovieElement;
        String cinemaName = "კავეა ისთ ფოინთი";
        for(int i = 1; i <= movieElementsCount; i++){
            WebElement currentMovie = driver.findElement(By.xpath("//div[@class='movies-deal']["+i+"]"));

            action.moveToElement(currentMovie).build().perform();
            WebElement buyButton = currentMovie.findElement(By.cssSelector("div.cinema-hover a:nth-child(2)"));
            new WebDriverWait(driver, 3).until(ExpectedConditions.elementToBeClickable(buyButton)).click();

            try{
                WebElement eastPointElement = driver.findElement(By.xpath("//li/a[text()='" + cinemaName + "']"));
                js.executeScript("arguments[0].scrollIntoView(true);" +
                        "arguments[0].click()", eastPointElement);
                firstMovieElement = currentMovie;
                break;
            } catch (NoSuchElementException e){
                driver.navigate().back();
                System.out.println("Current movie doesn't have seances in EastPoint cinema!!!");

            }
        }

//        Locate the last date and click
        WebElement lastDate = driver.findElement(By.cssSelector("div[aria-hidden=false] div ul li:last-child a"));
        js.executeScript("arguments[0].scrollIntoView(true);" +
                            "arguments[0].click()", lastDate);

//        Check and ensure that only Cavea Eastpoint options were returned
        List<WebElement> caveaSeances = driver.findElements(By.cssSelector("div[class*='seanse-details'][aria-expanded=true][aria-hidden=false] a"));
        Iterator<WebElement> iterator = caveaSeances.iterator();

        while (iterator.hasNext()) {
            WebElement seance = iterator.next();

            if (seance.getText().trim().isEmpty()) {
                iterator.remove();
            } else {
                try {
                    String cinemaActualName = seance.findElement(By.cssSelector("p.cinema-title")).getText();
                    System.out.println(cinemaActualName);
                    Assert.assertEquals(cinemaName, cinemaActualName);
                } catch (AssertionError ae) {
                    System.out.println("Seance out of Cavea Eastpoint detected!!!");
                    break;
                }
            }
        }

//        Locate the last option and click
        WebElement lastSeance = caveaSeances.get(caveaSeances.size()-1);

        // Get info about séance for further checking
        String movieNameBefore = driver.findElement(By.cssSelector("div.info p.name")).getText();
        String seanceDateBefore = lastDate.getText().split(" ")[0];
        String seanceTimeBefore = lastSeance.getText().split("\n")[0];
        String seanceDateTimeBefore = seanceDateBefore + seanceTimeBefore;
        String cinemaTitleBefore = lastSeance.findElement(By.cssSelector("p.cinema-title")).getText();

        js.executeScript("arguments[0].click()", lastSeance);


//        Get info about movie for checking later

//        Locate the popup window and switch to it
        Set<String> windowHandles = driver.getWindowHandles();
        List<String> windowHandlesList = new ArrayList<>(windowHandles);
        String popupWindow = windowHandlesList.get(0);
        driver.switchTo().window(popupWindow);

//        Locate info and check if it is correct
        String allInfoSelector = "div.right-content div.content-header";
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(allInfoSelector)));
        WebElement allInfo = driver.findElement(By.cssSelector(allInfoSelector));


//        Get popup info to check
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(allInfo));
        String movieNameAfter = allInfo.findElement(By.cssSelector("p.movie-title")).getText();
        String cinemaTitleAfter = allInfo.findElement(By.cssSelector("p:nth-child(2)")).getText();
        String[] seanceDateTimeAfterArr = allInfo.findElement(By.cssSelector("p:nth-child(3)")).getText().split(" ");
        String seanceDateTimeAfter = seanceDateTimeAfterArr[0] + seanceDateTimeAfterArr[seanceDateTimeAfterArr.length - 1];

//        Check them
        try{
            Assert.assertEquals(movieNameAfter, movieNameBefore);
            Assert.assertEquals(cinemaTitleAfter, cinemaTitleBefore);
            Assert.assertEquals(seanceDateTimeAfter, seanceDateTimeBefore);
            System.out.println("Information checking completed successfully!!!");
        }catch (AssertionError ae){
            System.out.println("Invalid Information!!!");
        }

//        Locate and choose random vacant place
        List<WebElement> vacantPlaces = driver.findElements(By.cssSelector("div.seat.free"));
        int numberOfVacantPlaces = vacantPlaces.size();
        // Choose random vacant place
        Random random = new Random();
        int randomVacantPlaceNumber = random.nextInt(numberOfVacantPlaces);
        WebElement randomVacantPlace = vacantPlaces.get(randomVacantPlaceNumber);
        js.executeScript("arguments[0].scrollIntoView(true);" +
                "arguments[0].click()", randomVacantPlace);

//        Switch to new popup sidebar
        windowHandles = driver.getWindowHandles();
        windowHandlesList = new ArrayList<>(windowHandles);
        popupWindow = windowHandlesList.get(0);
        driver.switchTo().window(popupWindow);

        WebElement registerButton = driver.findElement(By.cssSelector("p.register"));
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(registerButton)).click();

//        Let's Register
//        Locate all the elements
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

        firstNameField.sendKeys("Bacho");
        js.executeScript("arguments[0].scrollIntoView(true);" +
                "arguments[0].click()", agreeToTermsCheckbox);
        js.executeScript("arguments[0].scrollIntoView(true);" +
                "arguments[0].click()", finishRegistrationButton);

    }


//    Quit Driver
//    @AfterTest
//    public void tearDown() {
//        driver.quit();
//    }
}
