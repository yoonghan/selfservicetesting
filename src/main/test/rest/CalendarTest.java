package rest;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.config.RedirectConfig.*;
import static org.hamcrest.Matchers.*;

public class CalendarTest {
	

	private final int MAX_TITLE_SIZE=30;
	private final int MAX_DESC_SIZE=300;
	
	private long currDate;
	private RequestSpecification cookieSet=null;
	
	@Before
	public void setCookies(){
		proxy("localhost", 9000); 
		cookieSet = obtainCookie();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		currDate = cal.getTimeInMillis();
	}
	
	public RequestSpecification obtainCookie(){
		
		config = config().redirect(redirectConfig().followRedirects(false).and().maxRedirects(0));
		
		Response response = get("/user/testprofile");
		String cookie = response.getCookie("PLAY_SESSION");
		
		Assert.assertEquals(303, response.getStatusCode()); //redirect
		
		return given().cookie("PLAY_SESSION", cookie);
	}
	
	public void deleteCalendar(){
		cookieSet
		.contentType(ContentType.JSON).body("{}").when().delete("/tools/calendarconf")
		.then().body("success",is("OK"));
	}
		
	@Test
	public void invalidCalendarSetup(){
		
		Response resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"\", "
						+ "\"desc\":\"\", "
						+ "\"fullDay\":true, "
						+ "\"reserveType\":\"\", "
						+ "\"timedEvents\":[], "
						+ "\"occurrence\":[], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":[]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().body("errors", hasItems(
				"Title requires minimum of  2 characters",
				"Title cannot be blank",
				"Description requires minimum of  2 characters",
				"Description cannot be blank",
				"Reserve Type requires minimum of  1 characters",
				"Reserve Type cannot be blank"));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\""+new String(new char[MAX_TITLE_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"desc\":\""+new String(new char[MAX_DESC_SIZE+1]).replace('\0', 'a')+"\", "
						+ "\"fullDay\":true, "
						+ "\"reserveType\":\"opt2\", "
						+ "\"timedEvents\":[], "
						+ "\"occurrence\":[], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().body("errors", hasItems(
				"Title requires maximum of  30 characters",
				"Description requires maximum of  300 characters"));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"ABCD\", "
						+ "\"desc\":\"ABCD\", "
						+ "\"fullDay\":true, "
						+ "\"reserveType\":\"opt2\", "
						+ "\"timedEvents\":[], "
						+ "\"occurrence\":[false,false,false,false,false,false,false], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().body("errors", hasItems(
				" Specific occurrence must not have empty dates"));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"ABCD\", "
						+ "\"desc\":\"ABCD\", "
						+ "\"fullDay\":true, "
						+ "\"reserveType\":\"opt1\", "
						+ "\"timedEvents\":[], "
						+ "\"occurrence\":[true,false,false,false,false,false,false], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().body("errors", hasItems(
				" Specific occurrence must not have empty dates"));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"ABCD\", "
						+ "\"desc\":\"ABCD\", "
						+ "\"fullDay\":false, "
						+ "\"reserveType\":\"opt2\", "
						+ "\"timedEvents\":[], "
						+ "\"occurrence\":[true,false,false,false,false,false,false], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().body("errors", hasItems(
				" Non full day must have timed events"));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"ABCD\", "
						+ "\"desc\":\"ABCD\", "
						+ "\"fullDay\":false, "
						+ "\"reserveType\":\"opt2\", "
						+ "\"timedEvents\":[{\"stime\":\"1700\", \"etime\":\"1700\", \"abookings\":1}], "
						+ "\"occurrence\":[true,false,false,false,false,false,false], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().body("errors", hasItems(
				" Some of the time set are invalid. Check end time against start time"));
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"ABCD\", "
						+ "\"desc\":\"ABCD\", "
						+ "\"fullDay\":false, "
						+ "\"reserveType\":\"opt2\", "
						+ "\"timedEvents\":[{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":100}], "
						+ "\"occurrence\":[true,false,false,false,false,false,false], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().statusCode(401);
		
		
		resp = cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"ABCD\", "
						+ "\"desc\":\"ABCD\", "
						+ "\"fullDay\":false, "
						+ "\"reserveType\":\"opt2\", "
						+ "\"timedEvents\":["
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1},"
						+ "{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1}"
						+ "], "
						+ "\"occurrence\":[true,false,false,false,false,false,false], "
						+ "\"reserveEvents\":[],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar");
		resp.then().body("errors", hasItems(
				" Maximum number of blackout dates(50), occurrences(50) and time(16) permitted allowed"));
		
	}
	
	@Test
	public void createOne(){
		deleteCalendar();
		cookieSet
				.contentType(ContentType.JSON)
				.body("{"
						+ "\"title\":\"ABCD\", "
						+ "\"desc\":\"ABCD\", "
						+ "\"fullDay\":false, "
						+ "\"reserveType\":\"opt1\", "
						+ "\"timedEvents\":[{\"stime\":\"1700\", \"etime\":\"1730\", \"abookings\":1}], "
						+ "\"occurrence\":[false], "
						+ "\"reserveEvents\":["+currDate+"],"
						+ "\"blackoutEvents\":["+currDate+"]"
						+ "}")
				.when().put("/tools/calendar")
				.then().body("success",is("OK"));
		

		String response = checkCookie();
		
		try{
			Thread.sleep(1000);
		}catch(Exception e){
			
		}
		
		obtainCookie()
		.contentType(ContentType.JSON)
		.body(response)
		.when().post("/tools/calendarconf")
		.then().body("success",is("OK"));
	}
	
	@Test
	public void checkReservationList(){
		cookieSet.get("/tools/reservationlist").then().statusCode(200);
		cookieSet.get("/tools/usersinreservation/54a4a63072014e30c18e6d05").then().statusCode(200); //invalid information
	}
		
	public String checkCookie(){
		try{
			Thread.sleep(2000);
		}catch(Exception e){
			
		}
		//obtain cookie again as body has been set.
		Response resp=obtainCookie().get("/tools/calendarconf/0/0");
		resp.then().body("title", contains("ABCD"));
		
		return resp.body().asString();
	}
	
}
