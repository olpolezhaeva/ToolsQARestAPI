package runner;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

public abstract class BaseRunner {

    private static final String baseAPI = "https://bookstore.toolsqa.com";

    private String tokenAPI;

    @BeforeClass
    protected void setTokenAPI() {
        tokenAPI = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(String.format("{ \"userName\": \"%s\", \"password\": \"%s\" }",
                        BaseProperties.getProperties().getProperty("username"),
                                BaseProperties.getProperties().getProperty("password")))
                .post(String.format("%s%s", baseAPI, "/Account/v1/GenerateToken"))
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
