package runner;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import model.CreateUserPostJson;
import org.testng.annotations.BeforeClass;

public abstract class BaseRunner {

    private static final String baseAPI = "https://bookstore.toolsqa.com";
    private static final String pageGenerateToken = "/Account/v1/GenerateToken";

    private String tokenAPI;

    @BeforeClass
    protected void setTokenAPI() {
//        RestAssured.requestSpecification = new RequestSpecBuilder()
//                .setBaseUri(baseAPI)
//                .setContentType(ContentType.JSON)
//                .setAccept(ContentType.JSON)
//                .build();

        RestAssured.baseURI = baseAPI;

        tokenAPI = RestAssured
                .given()
                .header("Content-Type", "application/json") //Change to contentType. Same for AcceptType.
                .body(new CreateUserPostJson(BaseProperties.getProperties().getProperty("username"),
                        BaseProperties.getProperties().getProperty("password")))
                .post(pageGenerateToken)
                .getBody()
                .jsonPath()
                .get("token")
                .toString();
    }

    protected String getTokenAPI() {
        return tokenAPI;
    }

    protected static String getBaseAPI() {
        return baseAPI;
    }
}
