package core;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.io.CountingInputStream;

public class GoogleSearchValidationTest {

	WebDriver driver = new FirefoxDriver();// Create a new instance of the FireFox driver
	private String baseUrl = "https://www.google.com";

	private String kwd = System.getProperty("kwd");	// Command line argument  
	private String expectedAmount = System.getProperty("expectedAmount");// Command line argument expected total amount
	private String linkToValidate = System.getProperty("linkToValidate");// Command line argument link for validation
	private final static int STATUSCODE = 200;// Status code of page
	private int errorCounter = 0; // Counter of failed steps
	
	// Rule allows to continue test after assertion failure on some of the steps		
	@Rule
    public  ErrorCollector collector = new ErrorCollector();
	
		
	@Before
	public void setUp() throws Exception {
		// Go to www.google.com 
		driver.get(baseUrl); 
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		driver.manage().deleteAllCookies();
		driver.manage().window().maximize();
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		driver = null;
	}
	
	//@Ignore
	@Test
	public void test() throws IOException {
		
				
		// Find the text input element by its name
        WebElement element = driver.findElement(By.name("q"));
        element.clear();
        // Enter keyword to search for
        element.sendKeys(kwd);
        // Submit the form
        element.submit();
        // Get the result statistics
		String text = driver.findElement(By.id("resultStats")).getText();
		// Split words in array of strings
		String[] words = text.split("\\s");
		// Remove commas form the result statistics
		String result = words[1].replace(",", "");
		String returns = ""; 
		String linkValidation = "";
		long pageSize = 0;
	
		try{
			// Assert that result statistics equals to expected total amount
			assertEquals(expectedAmount, result) ;
			returns = "01. Total amount of returns " + result + " matches with expected " + expectedAmount + "\r\n";
		}
		catch (Throwable t){
			collector.addError(t);
			errorCounter++;
			returns = "01. Total amount of returns " + result + " doesn't match with expected " + expectedAmount + "\r\n";
		}	
		
		// Find all links on the page and click on the link provided for validation if that link is on the result page
		List<WebElement> links = driver.findElements(By.xpath(".//*[@id='rso']//*/li[/*]/div//*//a"));
		int validLinkNumber = 0;
		for (WebElement link : links){
			if(link.getAttribute("href").equals(linkToValidate)){
				validLinkNumber = links.indexOf(link) + 1;
				link.click();
				break;
			}
		}
		// Connect to the url provided for validation
		URL url = new URL(linkToValidate);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.connect();
		
		// Verify that a provided link is valid
		try{
			assertEquals(linkToValidate, driver.getCurrentUrl());
			linkValidation = "02. Link (#" + validLinkNumber + ") is valid ";
							
			// Verify Web Page status code (Must be 200)
			try{
				assertEquals(STATUSCODE, connection.getResponseCode());
				CountingInputStream counter = new CountingInputStream(connection.getInputStream());
			    String output = IOUtils.toString(counter);
			    // Get the size of page in KB
			    pageSize = counter.getCount()/1000;
				linkValidation = linkValidation + " satus " + connection.getResponseCode() + "\r\n" + "03. Page title - " 
				+ driver.getTitle() + "\r\n" + "04. Page size - " + pageSize + " KB \r\n" ;
			}
			catch (Throwable t){
				collector.addError(t);
				linkValidation = "02. Link is not valid satus " + connection.getResponseCode() + "\r\n";
				errorCounter++;
				
			}
					
		}
		catch (Throwable t){
			collector.addError(t);
			linkValidation = "02. Link is not on the result page status " + connection.getResponseCode() + "\r\n";
			errorCounter++;
		}
		
		connection.disconnect();
		
		// Write testing results in a report file 
		try {
			String header = "# ===================================================================" + "\r\n"
					+ "# Username:      [Elena Smirnova]" + "\r\n" + "# Email:         [smielena24@gmail.com]" + "\r\n" +
					"# Date:          [11/04/2014 15:00:00]" + "\r\n" + "#" + "\r\n" + "# OS:            [Windows 8]" +
					"\r\n" + "# Java version:  [1.7.0_55]" + "\r\n" + "#" + "\r\n" + "# Script name:   [Google_Search_Validation]" +
					"\r\n" + "# Description:   [Google Search and link validation]" + "\r\n" + "# Output file:   [report_01.txt]"
					+ "\r\n" + "# ===================================================================" + "\r\n";

			File file = new File("./src/test/reports/report_01.txt");

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(header); // append header to the report file
			if(errorCounter == 0){
				bw.append("Result: Pass \r\n");// append Pass: to the report file  if there aren't failures in the test
			}
			else{
				bw.append("Result: Fail \r\n");// append Fail: to the report file  if there are failures in the test
			}
			bw.append(returns);
			bw.append(linkValidation);
			bw.append("# ===================================================================");
			bw.close();
		}	

		catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
