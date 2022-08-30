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

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 200 OK");
    }

    @Test
    public void testGetHeaders() {
        Response response = given()
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS);

        Assert.assertEquals(response.header("Content-Type"), "application/json; charset=utf-8");
        Assert.assertEquals(response.header("Content-Length"), "4514");
        Assert.assertEquals(response.header("Server"), "nginx/1.17.10 (Ubuntu)");
        Assert.assertEquals(response.header("Connection"), "keep-alive");
        Assert.assertEquals(response.header("X-Powered-By"), "Express");
        Assert.assertEquals(response.header("ETag"), "W/\"11a2-8zfX++QwcgaCjSU6F8JP9fUd1tY\"");
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
        String[] expected = {"Git Pocket Guide", "Learning JavaScript Design Patterns", "Designing Evolvable Web APIs with ASP.NET",
                "Speaking JavaScript", "You Don't Know JS", "Programming JavaScript Applications", "Eloquent JavaScript, Second Edition", "Understanding ECMAScript 6"};

        List<String> title = given()
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS)
                .jsonPath()
                .get("books.title");

        Assert.assertEquals(title.toArray(), expected);
    }

    @Test
    public void testFailedCreateNewBook() {
        Response response = given().contentType(ContentType.JSON)
                .body(new CreateNewBookPutJson("TQ128", "9781449325869"))
                .post("/BookStoreV1BooksPost");

        Assert.assertEquals(response.getStatusCode(), 302);
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
        JsonSuccessResponse jsonSuccessResponse = given()
                .body(new CreateUserPostJson("test_rest", "rest@123"))
                .post(PAGE_ACCOUNT_USER)
                .getBody()
                .as(JsonSuccessResponse.class);

        Assert.assertEquals(jsonSuccessResponse.getCode(), "1200");
        Assert.assertEquals(jsonSuccessResponse.getMessage(), "UserName and Password required.");
    }

    @Test
    public void userRegistrationSuccessfulTest2() {
        Response response = given().body(new CreateUserPostJson("test_rest", "rest@123"))
                .post(PAGE_ACCOUNT_USER);

        if (response.getStatusCode() == 200) {
            JsonFailureResponse jsonFailureResponse = response.getBody().as(JsonFailureResponse.class);

            Assert.assertEquals(jsonFailureResponse.getFaultId(), "User already exists");
            Assert.assertEquals(jsonFailureResponse.getFault(),"FAULT_USER_ALREADY_EXISTS");
        } else if (response.getStatusCode() == 201) {
            JsonSuccessResponse jsonSuccessResponse = response.getBody().as(JsonSuccessResponse.class);

            Assert.assertEquals(jsonSuccessResponse.getCode(),"OPERATION_SUCCESS");
        }
    }

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

        Assert.assertEquals(result.get(0), "9781449325862");
    }

    @Test(dependsOnMethods = "addBookTest")
    public void updateBookPutTest() {
        String oldIsbn = "/9781449325862";
        String newIsbn = "9781449331818";

        Response response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth()
                .oauth2(getTokenAPI())
                .body(new CreateNewBookPutJson(getUserId(), newIsbn))
                .put(PAGE_BOOKSTORE_BOOKS.concat(oldIsbn));

        Assert.assertEquals(response.getStatusCode(), 200);
    }


    @Test(dependsOnMethods = "updateBookPutTest")
    public void deleteBookTest() {
        Response response = RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON).auth()
                .oauth2(getTokenAPI())
                .body(new CreateNewBookPutJson(getUserId(), "9781449331818"))
                .delete(PAGE_BOOKSTORE_BOOK);

        Assert.assertEquals(response.getStatusCode(), 204);
    }
}
