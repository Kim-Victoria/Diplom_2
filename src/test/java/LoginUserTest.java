import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.UserModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.UserSteps;

import static data.GenerateValues.generateUniqueEmail;
import static data.TestValues.*;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static steps.UserSteps.*;

@DisplayName("Тесты на авторизацию(логин) пользователя")
public class LoginUserTest extends BaseTest {
    private UserModel user;
    private String userToken;

    @Before
    public void setUp() {
            user = new UserModel(generateUniqueEmail(), USER_PASSWORD, USER_NAME);
            createUser(user);
        }

    @After
    public void cleanUp() {
        if (user != null) {
            try {
            userToken = loginUser(user)
                    .then()
                    .extract()
                    .path("refreshToken");
            if (userToken != null) {
                UserSteps.deleteUser(userToken);
            }
        } catch (Exception ignored) {
        }
    }
}

    @Test
    @DisplayName("Успешный логин пользователя в системе")
    @Description("Тест на успешный логин под существующим пользователем")
    public void loginUserSuccessful() {
        Response loginResponse = loginUser(user);
        checkLoginSuccessfulStep(loginResponse);
    }
    @Test
    @DisplayName("Логин пользователя без email")
    @Description("Тест на логин пользователя без указания email")
    public void loginUserWithoutEmail() {
        UserModel loginWithoutEmail = loginUserStep("", USER_PASSWORD);
        Response response = loginUser(loginWithoutEmail);
        checkLoginErrorWithoutEssentialFieldStep(response);
    }
    @Test
    @DisplayName("Логин пользователя без пароля")
    @Description("Тест на логин пользователя без указания пароля")
    public void loginUserWithoutPassword() {
        UserModel loginWithoutPassword = loginUserStep(USER_EMAIL, "");
        Response response = loginUser(loginWithoutPassword);
        checkLoginErrorWithoutEssentialFieldStep(response);
    }
    @Test
    @DisplayName("Логин пользователя с неверным email")
    @Description("Тест на логин пользователя с неверным email")
    public void loginUserWithWrongEmail() {
        UserModel loginWithWrongEmail = loginUserStep("wrong_email", USER_PASSWORD);
        Response response = loginUser(loginWithWrongEmail);
        checkLoginErrorWithWrongInfoStep(response);
    }
    @Test
    @DisplayName("Логин пользователя с неверным паролем")
    @Description("Тест на логин пользователя с неверным паролем")
    public void loginUserWithWrongPassword() {
        UserModel loginWithWrongPassword = loginUserStep(user.getEmail(), "wrong_password");
        Response response = loginUser(loginWithWrongPassword);
        checkLoginErrorWithWrongInfoStep(response);
    }

    @Step ("Логин пользователя в системе")
    public UserModel loginUserStep(String email, String password) {
        return new UserModel(email, password);
    }
    @Step("Проверка успешного логина пользователя в системе")
    public void checkLoginSuccessfulStep(Response loginResponse) {
        loginResponse.then().statusCode(SC_OK).body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract().path("refreshToken");
    }
    @Step ("Проверка ошибки при логине пользователя без обязательного поля")
    public void checkLoginErrorWithoutEssentialFieldStep(Response response) {
        response.then().statusCode(SC_UNAUTHORIZED).body("success", equalTo(false));
    }
    @Step ("Проверка ошибки при логине с неверными данными")
    public void checkLoginErrorWithWrongInfoStep(Response response) {
        response.then().statusCode(SC_UNAUTHORIZED).body("success", equalTo(false));
    }
}
