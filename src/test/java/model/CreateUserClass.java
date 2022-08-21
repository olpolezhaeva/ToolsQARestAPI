package model;

import io.restassured.response.Response;
import runner.BaseModel;

public final class CreateUserClass extends BaseModel<CreateUserClass> {

    public CreateUserClass(String tokenAPI) {
        super(tokenAPI);
    }

    public Response getResponseCreateUser(String username, String password) {
        return responsePOST(String.format("{ \"userName\": \"%s\", \"password\": \"%s\" }", username, password), "/Account/v1/User");
    }
}
