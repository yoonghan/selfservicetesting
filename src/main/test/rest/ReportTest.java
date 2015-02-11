package rest;

import static com.jayway.restassured.RestAssured.config;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.proxy;
import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class ReportTest {
	
	private RequestSpecification cookieSet=null;

	@Before
	public void setCookies(){
		proxy("localhost", 9000); 
		cookieSet = obtainCookie();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
	}
	
	public RequestSpecification obtainCookie(){
		
		config = config().redirect(redirectConfig().followRedirects(false).and().maxRedirects(0));
		
		Response response = get("/user/testprofile");
		String cookie = response.getCookie("PLAY_SESSION");
		
		Assert.assertEquals(303, response.getStatusCode()); //redirect
		
		return given().cookie("PLAY_SESSION", cookie);
	}
	
	@Test
	public void generateReport(){
		cookieSet.when().get("/report/gen").then()
		.body("success", is("OK"));
		
		System.out.println("Please check on reporting generated in your test folder!.");
	}
		
	@Test
	public void reportAllow(){
		cookieSet.when().get("/report/isAllow").then()
		.body("success", is("ok"));
		
	}
	
	@Test
	public void settingIns(){
		
		Response resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"email\":\"invalid@\""
						+ "}")
				.when().post("/report/setting");
				
				//Check minimum
				resp.then().body("errors", hasItems("Email error.email"));
				
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "}")
				.when().post("/report/setting");
				
				//Check minimum
				resp.then().body("error", is("Unexpected Request, what have you sent?"));
		
		cookieSet
		.contentType(ContentType.JSON)
		.body("{"
				+ "\"email\":\"mailyoonghan@gmail.com\""
				+ "}")
		.when().post("/report/setting")
		.then().body("success",is("OK"));
	}
}
