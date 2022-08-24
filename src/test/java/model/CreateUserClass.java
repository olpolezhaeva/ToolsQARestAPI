package model;

import runner.EndPoints;
import runner.BaseModel;

public final class CreateUserClass extends BaseModel<CreateUserClass> {

    public CreateUserClass(String tokenAPI) {
        super(tokenAPI);
    }

    public CreateUserGetJson getResponseCreateUser(String username, String password) {
        return responsePOST(new CreateUserPostJson(username, password), EndPoints.PAGE_ACCOUNT_USER)
                .as(CreateUserGetJson.class);
    }

    public boolean responseReceived(String username, String password) {
        return getResponseCreateUser(username, password) != null;
    }
}
