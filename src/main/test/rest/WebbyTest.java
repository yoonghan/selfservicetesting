package rest;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class WebbyTest{
	
	@Test
	public void testMenu(){
		get("http://localhost:8000/cache/json/menulist.json")
		.then()
		.assertThat()
		.body("display", hasItems("About Us","Architecture","Blog"));
	}
	
	@Test
	public void testImages(){
		get("http://localhost:9000/rest/image/intro")
		.then()
		.assertThat()
		.body("backgrounds.fade", hasItems(1000));
	}
}
