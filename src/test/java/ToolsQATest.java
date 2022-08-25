import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
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
    public void getStatusCodTest() {
        RequestSpecification httpRequest = given();
        Response response = httpRequest.request(Method.GET, EndPoints.PAGE_BOOKSTORE_BOOKS);

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("HTTP/1.1 200 OK", response.getStatusLine());
    }

    @Test
    public void iteratingHeaders() {
        RequestSpecification httpRequest = given();
        Response response = httpRequest.get(EndPoints.PAGE_BOOKSTORE_BOOKS);

        Headers headers = response.headers();

        for(Header header : headers) {
//            System.out.println("Key: " + header.getName() + ", Value: " + header.getValue());
        }

        Assert.assertEquals(response.header("Content-Type") , "application/json; charset=utf-8" );
        Assert.assertEquals(response.header("Content-Length") , "4514" );
        Assert.assertEquals(response.header("Server") , "nginx/1.17.10 (Ubuntu)" );
        Assert.assertEquals(response.header("Connection") , "keep-alive" );
        Assert.assertEquals(response.header("X-Powered-By") , "Express" );
        Assert.assertEquals(response.header("ETag") , "W/\"11a2-8zfX++QwcgaCjSU6F8JP9fUd1tY\"" );
    }

    @Test
    public void getBodyBooksTest() {
        RequestSpecification requestSpecification = given().baseUri("https://demoqa.com/BookStore/v1/Books");
        RequestSpecification httpRequest = given().spec(requestSpecification);
        Response response = httpRequest.get("viewport");

        Assert.assertTrue(response.getBody().asString().contains("viewport"));
    }

    @Test
    public void verifyJsonResponseTest() {
        String[] actual = {"Git Pocket Guide", "Learning JavaScript Design Patterns", "Designing Evolvable Web APIs with ASP.NET",
                "Speaking JavaScript", "You Don't Know JS", "Programming JavaScript Applications", "Eloquent JavaScript, Second Edition", "Understanding ECMAScript 6"};

        RequestSpecification httpRequest = given();
        Response response = httpRequest.get(EndPoints.PAGE_BOOKSTORE_BOOKS);

        JsonPath jsonPath = response.jsonPath();
        List<String> title = jsonPath.get("books.title");

        Assert.assertEquals(actual, title.toArray());
    }
    @Test
    public void postCreateNewBookTest() {
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json")
                .body(new CreateNewBookPutJson("TQ128", "9781449325869"));
        Response response = request.post("/BookStoreV1BooksPost");

        Assert.assertEquals(302, response.getStatusCode());
    }

    @Test
    public void userRegistrationSuccessfulTest()
    {
        RequestSpecification requestSpecification = given().baseUri("https://demoqa.com/Account/v1");
        RequestSpecification request = RestAssured.given().spec(requestSpecification);

        request.body(new CreateUserPostJson("test_rest", "Testrest@123"));
        Response response = request.put("/User");

        Assert.assertEquals("HTTP/1.1 404 Not Found", response.getStatusLine());
        Assert.assertTrue(response.getBody().asString().contains("Error"));
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
    @Test
    public void authenticationBasicsTest() {

        Response response = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(new CreateUserPostJson("patriotby07", "7563P@triotby13756785"))
                .post("/Account/v1/Authorized");
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Status message " + response.body().asString());
    }

    @Test
    public void addBookTest() {
        String id = "cf27b539-6614-4f27-9bbc-8a2154520673";
        String isbn = "9781449325862";

        List<String> result = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON).auth()
                .oauth2(getTokenAPI()).body(String.format("{\"userId\": \"%s\",\n" +
                        "  \"collectionOfIsbns\": [\n" +
                        "    {\n" +
                        "      \"isbn\": \"%s\"\n" +
                        "    }\n" +
                        "  ]}", id, isbn)).post(PAGE_BOOKSTORE_BOOKS).getBody().jsonPath().get("books.isbn");

        Assert.assertEquals("9781449325862", result.get(0));
    }

    @Test(dependsOnMethods = "addBookTest")
    public void deleteBookTest() {
        Response response = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON).auth()
                .oauth2(getTokenAPI())
                .body(new CreateNewBookPutJson("cf27b539-6614-4f27-9bbc-8a2154520673", "9781449325862"))
                .delete(PAGE_BOOKSTORE_BOOK);

        Assert.assertEquals(204, response.getStatusCode());
    }
}
