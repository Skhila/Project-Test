import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
        action = new Actions(driver);
        js = (JavascriptExecutor) driver;
    }

//    @BeforeTest
//    public void setup(){
//        WebDriverManager.edgedriver().setup();
//        driver = new EdgeDriver();
//        action = new Actions(driver);
//        js = (JavascriptExecutor) driver;
//    }

    @Test
    public void swoopTest(){
//        Navigate to Swoop Website
        driver.get("https://www.swoop.ge");

////        Locate and click on the "კინო" button
//        WebElement movieButton = driver.findElement(By.xpath("//li[contains(@class,'MoreCategories')][1]/a"));
        String movieBtn = "კინო";
        WebElement movieButton = driver.findElement(By.xpath("//li[contains(@class,'MoreCategories')]/a[contains(text(), "+movieBtn+")]"));

        movieButton.click();

//        Locate the first movie deal and click buy
        WebElement firstMovieElement = driver.findElement(By.xpath("//div[@class='movies-deal'][1]/a"));

        action.moveToElement(firstMovieElement).perform();
        WebElement buyButton = firstMovieElement.findElement(By.xpath("//div[@class='cinema-hover']/a[2]"));
        buyButton.click();

//        Locate East-Point button and click
        String cinemaName = "კავეა ისთ ფოინთი";
        WebElement eastPointElement = driver.findElement(By.xpath("//li/a[text()='" + cinemaName + "']"));
        js.executeScript("arguments[0].scrollIntoView(true);" +
                "arguments[0].click()", eastPointElement);

//        Locate the last date and click
        WebElement lastDate = driver.findElement(By.cssSelector("div[aria-hidden=false] div ul li:last-child a"));
        js.executeScript("arguments[0].scrollIntoView(true);" +
                            "arguments[0].click()", lastDate);

//        Locate the last option and click
        WebElement lastSeance = driver.findElement(By.cssSelector("div[class*='seanse-details'][aria-expanded=true][aria-hidden=false]:last-child a"));

//        Get info about movie for checking later
        String movieNameBefore = driver.findElement(By.cssSelector("div.info p.name")).getText();
        String seanceDateBefore = lastDate.getText().split(" ")[0];
        String seanceTimeBefore = lastSeance.getText().split("\n")[0];
        String seanceDateTimeBefore = seanceDateBefore + seanceTimeBefore;
        String cinemaTitleBefore = lastSeance.findElement(By.cssSelector("p.cinema-title")).getText();

        js.executeScript("arguments[0].click()", lastSeance);

//        Locate the popup window and switch to it
        Set<String> windowHandles = driver.getWindowHandles();
        List<String> windowHandlesList = new ArrayList<>(windowHandles);
        String popupWindow = windowHandlesList.get(0);
        driver.switchTo().window(popupWindow);

//        Locate info and check if it is correct
        String allInfoSelector = "//*[@id=\"eventForm\"]/div/div[3]/div[1]";
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath(allInfoSelector)));
        WebElement allInfo = driver.findElement(By.xpath(allInfoSelector));


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
        randomVacantPlace.click();

//        Switch to new popup sidebar
        windowHandles = driver.getWindowHandles();
        windowHandlesList = new ArrayList<>(windowHandles);
        popupWindow = windowHandlesList.get(0);
        driver.switchTo().window(popupWindow);

        WebElement registerButton = driver.findElement(By.cssSelector("p.register"));
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(registerButton)).click();

    }


//    Quit Driver
    @AfterTest
    public void tearDown() {
        driver.quit();
    }
}
