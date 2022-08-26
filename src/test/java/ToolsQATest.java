import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import model.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import runner.BaseRunner;
import runner.EndPoints;

import java.util.List;

import static io.restassured.RestAssured.given;
import static runner.EndPoints.*;

public class ToolsQATest extends BaseRunner {

    @Test
    public void testGetResponse() {
        boolean responseIsReceived = new CreateUserClass(getTokenAPI())
                .responseReceived("username", "password");

        Assert.assertTrue(responseIsReceived);
    }

    @Test
    public void testCreateUserWithWrongPassword() {
        CreateUserGetJson response = new CreateUserClass(getTokenAPI())
                .getResponseCreateUser("username", "password");

        Assert.assertEquals(response.getCode(), "1300");
        Assert.assertEquals(response.getMessage(), "Passwords must have at least one non alphanumeric character," +
                " one digit ('0'-'9'), one uppercase ('A'-'Z'), one lowercase ('a'-'z')," +
                " one special character and Password must be eight characters or longer.");
    }

    @Test
    public void testGetStatusCode() {
        Response response = given()
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS);

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("HTTP/1.1 200 OK", response.getStatusLine());
    }

    @Test
    public void testGetHeaders() {
        Response response = given()
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS);

        Assert.assertEquals(response.header("Content-Type") , "application/json; charset=utf-8" );
        Assert.assertEquals(response.header("Content-Length") , "4514" );
        Assert.assertEquals(response.header("Server") , "nginx/1.17.10 (Ubuntu)" );
        Assert.assertEquals(response.header("Connection") , "keep-alive" );
        Assert.assertEquals(response.header("X-Powered-By") , "Express" );
        Assert.assertEquals(response.header("ETag") , "W/\"11a2-8zfX++QwcgaCjSU6F8JP9fUd1tY\"" );
    }

    @Test
    public void testGetBodyContainsBooks() {
        String response = given()
                .get(PAGE_BOOKSTORE_BOOKS)
                .getBody()
                .asString();

        Assert.assertTrue(response.contains("books"));
    }

    @Test
    public void testVerifyListTitle() {
        String[] actual = {"Git Pocket Guide", "Learning JavaScript Design Patterns", "Designing Evolvable Web APIs with ASP.NET",
                "Speaking JavaScript", "You Don't Know JS", "Programming JavaScript Applications", "Eloquent JavaScript, Second Edition", "Understanding ECMAScript 6"};

        List<String> title =  given()
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS)
                .jsonPath()
                .get("books.title");

        Assert.assertEquals(actual, title.toArray());
    }

    @Test
    public void testFailedCreateNewBook() {
         Response response = given().contentType(ContentType.JSON)
                 .body(new CreateNewBookPutJson("TQ128", "9781449325869"))
                 .post("/BookStoreV1BooksPost");

        Assert.assertEquals(302, response.getStatusCode());
    }

    @Test
    public void testUserExists() {
        CreateUserGetJson createUserGetJson = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new CreateUserPostJson("test_rest", "Testrest@123"))
                .post(PAGE_ACCOUNT_USER).getBody().as(CreateUserGetJson.class);

        Assert.assertEquals(createUserGetJson.getCode(), "1204");
        Assert.assertEquals(createUserGetJson.getMessage(), "User exists!");
    }

    @Test
    public void userRegistrationSuccessfulTest1() {
        RequestSpecification request = RestAssured.given();
        request.body(new CreateUserPostJson("test_rest", "rest@123"));
        Response response = request.post(PAGE_ACCOUNT_USER);
        ResponseBody body = response.getBody();
        JsonSuccessResponse responseBody = body.as(JsonSuccessResponse.class);
        Assert.assertEquals("1200", responseBody.getCode());
        Assert.assertEquals("UserName and Password required.", responseBody.getMessage());
    }

    @Test
    public void userRegistrationSuccessfulTest2() {
        RequestSpecification request = RestAssured.given();
        request.body(new CreateUserPostJson("test_rest", "rest@123"));
        Response response = request.post(PAGE_ACCOUNT_USER);
        ResponseBody body = response.getBody();

        if(response.getStatusCode() == 200) {
            JsonFailureResponse jsonFailureResponse = body.as(JsonFailureResponse.class);
            Assert.assertEquals("User already exists", jsonFailureResponse.getFaultId());
            Assert.assertEquals("FAULT_USER_ALREADY_EXISTS", jsonFailureResponse.getFault());
        } else if (response.getStatusCode() == 201) {
            JsonSuccessResponse jsonSuccessResponse = body.as(JsonSuccessResponse.class);
            Assert.assertEquals("OPERATION_SUCCESS", jsonSuccessResponse.getCode());
        }
    }
//    @Test
//    public void authenticationBasicsTest() {
//
//        Response response = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON)
//                .body(new CreateUserPostJson(BaseProperties.getProperties().getProperty("username"),
//                        BaseProperties.getProperties().getProperty("password"))
//                .post("/Account/v1/Authorized");
//        System.out.println("Status code: " + response.getStatusCode());
//        System.out.println("Status message " + response.body().asString());
//    }

    @Test
    public void addBookTest() {
        String isbn = "9781449325862";

        List<String> result = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON).auth()
                .oauth2(getTokenAPI()).body(String.format("{\"userId\": \"%s\",\n" +
                        "  \"collectionOfIsbns\": [\n" +
                        "    {\n" +
                        "      \"isbn\": \"%s\"\n" +
                        "    }\n" +
                        "  ]}", getUserId(), isbn)).post(PAGE_BOOKSTORE_BOOKS).getBody().jsonPath().get("books.isbn");

        Assert.assertEquals("9781449325862", result.get(0));
    }

    @Test(dependsOnMethods = "addBookTest")
    public void deleteBookTest() {
        Response response =  RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON).auth()
                .oauth2(getTokenAPI())
                .body(new CreateNewBookPutJson(getUserId(), "9781449325862"))
                .delete(PAGE_BOOKSTORE_BOOK);

        Assert.assertEquals(204, response.getStatusCode());
    }
}
