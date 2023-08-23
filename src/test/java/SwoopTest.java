import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SwoopTest {
//    Setup Driver
    WebDriver driver;
    Actions action;

//    @BeforeTest
//    @Parameters("browser")
//    public void setup(String browser) throws Exception{
//        if(browser.equalsIgnoreCase("chrome")){
//            WebDriverManager.chromedriver().setup();
//            driver = new ChromeDriver();
//        }else if (browser.equalsIgnoreCase("edge")){
//            WebDriverManager.edgedriver().setup();
//            driver = new EdgeDriver();
//        }else {
//            throw new Exception("Browser is not correct");
//        }
//    }

    @BeforeTest
    public void setup(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        action = new Actions(driver);
    }

    @Test
    public void swoopTest(){
//        Navigate to Swoop Website
        driver.get("https://www.swoop.ge/");

//        Locate and click on the "კინო" button
        WebElement movieButton = driver.findElement(By.xpath("//li[contains(@class,'MoreCategories')]/a[1]"));

        movieButton.click();

//        Locate the first movie deal and click buy
        WebElement firstMovieElement = driver.findElement(By.xpath("//div[@class='movies-deal'][1]/a"));

        action.moveToElement(firstMovieElement).perform();
        WebElement buyButton = firstMovieElement.findElement(By.xpath("//div[@class='cinema-hover']/a[2]"));
        buyButton.click();

    }


////    Quit Driver
//    @AfterTest
//    public void tearDown() {
//        driver.quit();
//    }
}
