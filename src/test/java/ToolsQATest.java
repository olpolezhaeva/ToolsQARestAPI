import io.restassured.response.Response;
import model.CreateUserClass;
import org.testng.Assert;
import org.testng.annotations.Test;
import runner.BaseRunner;

public class ToolsQATest extends BaseRunner {

    @Test
    public void testCreateSameUser() {
        Response response = new CreateUserClass(getTokenAPI())
                .getResponseCreateUser("1", "Q1w2e3r4t5!");

        Assert.assertEquals(response.jsonPath().get("code"), "1204");
        Assert.assertEquals(response.jsonPath().get("message"), "User exists!");
    }
}
