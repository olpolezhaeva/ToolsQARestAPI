package runner;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.CreateUserPostJson;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class BaseRunner {

    private String tokenAPI;
    private String userId;

    @BeforeClass
    protected void createUser() {
        RestAssured.baseURI = EndPoints.BASE_API_URL;

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new CreateUserPostJson(BaseProperties.getProperties().getProperty("username"),
                        BaseProperties.getProperties().getProperty("password")))
                .post(EndPoints.PAGE_ACCOUNT_USER);

        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Error: " + response.getBody().jsonPath().get("message").toString());
        } else {
            userId = response
                    .getBody()
                    .jsonPath()
                    .get("userID")
                    .toString();
        }
    }

    @BeforeClass
    protected void setTokenAPI() {
        tokenAPI = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(new CreateUserPostJson(BaseProperties.getProperties().getProperty("username"),
                        BaseProperties.getProperties().getProperty("password")))
                .post(EndPoints.PAGE_GENERATE_TOKEN)
                .getBody()
                .jsonPath()
                .get("token")
                .toString();
    }

    @AfterClass
    protected void deleteUser() {
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth()
                .oauth2(tokenAPI)
                .delete(EndPoints.PAGE_ACCOUNT_USER + "/" + userId);
    }

    protected String getTokenAPI() {
        return tokenAPI;
    }

    protected String getUserId() {
        return userId;
    }
}
