import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.CreateNewBookPutJson;
import org.testng.Assert;
import org.testng.annotations.Test;
import runner.BaseRunner;
import runner.EndPoints;

import java.util.List;

public class BookStoreTest extends BaseRunner {
    @Test
    public void testGetBodyContainsBooks() {
        String response = RestAssured
                .given()
                .accept(ContentType.JSON)
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS)
                .getBody()
                .asString();

        Assert.assertTrue(response.contains("books"));
    }

    @Test
    public void testGetStatusCode() {
        Response response = RestAssured
                .given()
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS);

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 200 OK");
    }

    @Test
    public void testGetAllBooks() {
        List<String> books = RestAssured
                .given()
                .accept(ContentType.JSON)
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS)
                .jsonPath()
                .get("books.isbn");

        Assert.assertEquals(books.size(), 8);
    }

    @Test
    public void testVerifyListTitle() {
        String[] expected = {"Git Pocket Guide", "Learning JavaScript Design Patterns", "Designing Evolvable Web APIs with ASP.NET",
                "Speaking JavaScript", "You Don't Know JS", "Programming JavaScript Applications", "Eloquent JavaScript, Second Edition", "Understanding ECMAScript 6"};

        List<String> title = RestAssured
                .given()
                .accept(ContentType.JSON)
                .get(EndPoints.PAGE_BOOKSTORE_BOOKS)
                .jsonPath()
                .get("books.title");

        Assert.assertEquals(title.toArray(), expected);
    }

    @Test
    public void testFailedCreateNewBook() {
        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(new CreateNewBookPutJson("TQ128", "9781449325869"))
                .post("/BookStoreV1BooksPost");

        Assert.assertEquals(response.getStatusCode(), 302);
    }

    @Test
    public void testAddBook() {
        String isbn = "9781449325862";

        List<String> result = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth()
                .oauth2(getTokenAPI())
                .body(String.format("{\"userId\": \"%s\",\n" +
                        "  \"collectionOfIsbns\": [\n" +
                        "    {\n" +
                        "      \"isbn\": \"%s\"\n" +
                        "    }\n" +
                        "  ]}", getUserId(), isbn))
                .post(EndPoints.PAGE_BOOKSTORE_BOOKS)
                .getBody()
                .jsonPath()
                .get("books.isbn");

        Assert.assertEquals(result.get(0), "9781449325862");
    }

    @Test(dependsOnMethods = "testAddBook")
    public void testUpdateBook() {
        String oldIsbn = "/9781449325862";
        String newIsbn = "9781449331818";

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth()
                .oauth2(getTokenAPI())
                .body(new CreateNewBookPutJson(getUserId(), newIsbn))
                .put(EndPoints.PAGE_BOOKSTORE_BOOKS.concat(oldIsbn));

        Assert.assertEquals(response.getStatusCode(), 200);
    }


    @Test(dependsOnMethods = "testUpdateBook")
    public void testDeleteBook() {
        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth()
                .oauth2(getTokenAPI())
                .body(new CreateNewBookPutJson(getUserId(), "9781449331818"))
                .delete(EndPoints.PAGE_BOOKSTORE_BOOK);

        Assert.assertEquals(response.getStatusCode(), 204);
    }
}
