package runner;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import model.CreateUserPostJson;
import org.testng.annotations.BeforeClass;

public abstract class BaseRunner {

    private String tokenAPI;

    @BeforeClass
    protected void setTokenAPI() {
//        RestAssured.requestSpecification = new RequestSpecBuilder()
//                .setBaseUri(baseAPI)
//                .setContentType(ContentType.JSON)
//                .setAccept(ContentType.JSON)
//                .build();

        RestAssured.baseURI = EndPoints.BASE_API_URL;

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

    protected String getTokenAPI() {
        return tokenAPI;
    }
}
