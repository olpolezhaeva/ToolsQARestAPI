package runner;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.function.Function;

public abstract class BaseModel<Self extends BaseModel<?>> {

    private String tokenAPI;

    public BaseModel(String tokenAPI) {
        this.tokenAPI = tokenAPI;
    }

    protected String getTokenAPI() {
        return tokenAPI;
    }

    protected Response responsePOST(String body, String address) {
        return RestAssured
                .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + tokenAPI)
                .body(body)
                .post(String.format("%s%s", BaseRunner.getBaseAPI(), address));
    }

    public <Value> Self assertEquals(Function<Self, Value> actual, Value expected) {
        Assert.assertEquals(actual.apply((Self) this), expected);

        return (Self) this;
    }
}
