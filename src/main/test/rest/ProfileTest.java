package rest;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.config.RedirectConfig.*;
import static org.hamcrest.Matchers.*;

public class ProfileTest{
	
	private final int MAX_NAME_SIZE=30;
	private final int MAX_GENDER_SIZE=1;
	private final int MAX_CONTACT_SIZE=13;
	private final int MAX_COUNTRY_SIZE=3;
	private final int MAX_POSTALCD_SIZE=5;
	private final int MAX_ADDRESS_SIZE = 100;
	private final int MAX_EMAIL_SIZE = 100;
	private RequestSpecification cookieSet=null;
	
	@Before
	public void setCookies(){
		proxy("localhost", 9000); 
		cookieSet = obtainCookie();
	}
	
	public RequestSpecification obtainCookie(){
		
		config = config().redirect(redirectConfig().followRedirects(false).and().maxRedirects(0));
		
		Response response = get("/user/testprofile");
		String cookie = response.getCookie("PLAY_SESSION");
		
		Assert.assertEquals(303, response.getStatusCode()); //redirect
		
		return given().cookie("PLAY_SESSION", cookie);
	}
	
	@Test
	public void makeSureIsTestEnvironment(){
		cookieSet.when().get("/user/basicinfo").then()
		.body("firstName", is("Jaring"));
	}
	
	@Test
	public void submitInvalidInsertProfile(){
		Response resp = cookieSet
		.contentType(ContentType.JSON)
		.body("{"
				+ "\"firstName\":\"\", "
				+ "\"midName\":\"abc\", "
				+ "\"lastName\":\"\", "
				+ "\"gender\":\"\", "
				+ "\"country\":\"\", "
				+ "\"state\":\"\", "
				+ "\"postCode\":\"\", "
				+ "\"address\":\"\", "
				+ "\"email\":\"\", "
				+ "\"contactNo\":\"\""
				+ "}")
		.when().put("http://localhost:9000/user/profile");
		
		//Check minimum
		resp.then().body("errors", hasItems(
				"First Name requires minimum of  2 characters",
				"First Name cannot be blank",
				"Last Name requires minimum of  2 characters",
				"Last Name cannot be blank",
				"Gender requires minimum of  1 characters",
				"Gender cannot be blank",
				"Country requires minimum of  2 characters",
				"Country cannot be blank",
				"State requires minimum of  2 characters",
				"State cannot be blank"
				));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"firstName\":\""+new String(new char[MAX_NAME_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"midName\":\""+new String(new char[MAX_NAME_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"lastName\":\""+new String(new char[MAX_NAME_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"gender\":\""+new String(new char[MAX_GENDER_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"country\":\""+new String(new char[MAX_COUNTRY_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"state\":\"12345\", "
						+ "\"postCode\":\""+new String(new char[MAX_POSTALCD_SIZE+1]).replace('\0', '1')+"\", "
						+ "\"address\":\""+new String(new char[MAX_ADDRESS_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"email\":\""+new String(new char[MAX_EMAIL_SIZE+1]).replace('\0', '1')+"\", "
						+ "\"contactNo\":\""+new String(new char[MAX_CONTACT_SIZE+1]).replace('\0', 'a')+"\""
						+ "}")
				.when().put("http://localhost:9000/user/profile");

		//Check maximum - found bug that last obj cannot check maximum.
		resp.then().body("errors", hasItems(
				"First Name requires maximum of  30 characters",
				"Middle Name requires maximum of  30 characters",
				"Last Name requires maximum of  30 characters",
				"Country requires maximum of  3 characters",
				"Postcode is not a valid Postal Code",
				"Address requires maximum of  100 characters",
				"Email error.email",
				"Contact No requires maximum of  13 characters"
				));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"firstName\":\"123\", "
						+ "\"midName\":\"123\", "
						+ "\"lastName\":\"123\", "
						+ "\"gender\":\"123\", "
						+ "\"country\":\"123\", "
						+ "\"state\":\"12346\", "
						+ "\"postCode\":\"123456\", "
						+ "\"address\":\"someaddress\", "
						+ "\"email\":\"invalid@email\", "
						+ "\"contactNo\":\"abcded43\""
						+ "}")
				.when().put("http://localhost:9000/user/profile");
		
		//Check contact number
		resp.then().body("errors", hasItems(
				"Postcode is not a valid Postal Code",
				"Email error.email",
				"Contact No is invalid only numbers allowed"
				));
	}
	
	@Test
	public void submitInvalidUpdateProfile(){
		Response resp = cookieSet
		.contentType(ContentType.JSON)
		.body("{"
				+ "\"firstName\":\"\", "
				+ "\"midName\":\"abc\", "
				+ "\"lastName\":\"\", "
				+ "\"gender\":\"\", "
				+ "\"country\":\"\", "
				+ "\"state\":\"\", "
				+ "\"postCode\":\"\", "
				+ "\"address\":\"\", "
				+ "\"email\":\"\", "
				+ "\"contactNo\":\"\""
				+ "}")
		.when().post("http://localhost:9000/user/profile");
		
		//Check minimum
		resp.then().body("errors", hasItems(
				"First Name requires minimum of  2 characters",
				"First Name cannot be blank",
				"Last Name requires minimum of  2 characters",
				"Last Name cannot be blank",
				"Gender requires minimum of  1 characters",
				"Gender cannot be blank",
				"Country requires minimum of  2 characters",
				"Country cannot be blank",
				"State requires minimum of  2 characters",
				"State cannot be blank"
				));
	}
}
