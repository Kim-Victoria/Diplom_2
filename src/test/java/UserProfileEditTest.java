import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.UserModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static data.TestValues.*;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static steps.UserSteps.*;

@DisplayName("Тесты на изменение данных пользователя")
public class UserProfileEditTest extends BaseTest {
    private UserModel user;
    private String accessToken;
    private String refreshToken;

    @Before
    public void setUp() {
        user = new UserModel(generateUniqueEmail(), USER_PASSWORD, USER_NAME);
        createUser(user);
    }

    @After
    public void cleanUp() {
        if (refreshToken != null && !refreshToken.isBlank()) {
            deleteUser(refreshToken);
        }
    }

    @Test
    @DisplayName("Успешное обновление имени пользователя")
    @Description("Тест на успешное обновление имени пользователя")
    public void updateUserNameSuccessfully() {
        Response loginResponse = loginUser(user);

        refreshToken = loginResponse.then().extract().path("refreshToken");
        accessToken = loginResponse.then().extract().path("accessToken");

        String newName = generateUniqueName();
        UserModel update = new UserModel(newName);
        Response response = updateUserInfo(accessToken, update);
        checkSuccessfulUserNameUpdateStep(response, newName);
    }
    @Test
    @DisplayName("Успешное обновление email пользователя")
    @Description("Тест на успешное обновление email пользователя")
    public void updateUserEmailSuccessfully() {
        Response loginResponse = loginUser(user);
        refreshToken = loginResponse.then().extract().path("refreshToken");
        accessToken = loginResponse.then().extract().path("accessToken");

        String newEmail = generateUniqueEmail();
        UserModel update = new UserModel(newEmail, null, null);
        Response response = updateUserInfo(accessToken, update);
        checkSuccessfulUserEmailUpdateStep(response, newEmail);
    }
    @Test
    @DisplayName("Успешное обновление пароля пользователя")
    @Description("Тест на успешное обновление пароля пользователя")
    public void updateUserPasswordSuccessfully() {
        Response loginResponse = loginUser(user);
        refreshToken = loginResponse.then().extract().path("refreshToken");
        accessToken = loginResponse.then().extract().path("accessToken");

        String newPassword = "NewPassword";
        UserModel update = new UserModel(null, newPassword, null);
        Response response = updateUserInfo(accessToken, update);
        checkSuccessfulUserPasswordUpdateStep(response, newPassword);
    }
    @Test
    @DisplayName("Обновление пароля пользователя без авторизации")
    @Description("Тест на обновление пароля пользователя без авторизации")
    public void updateUserPasswordWithoutAuthorizationError() {
        String newPassword = "New Password";
        UserModel update = new UserModel(null, newPassword, null);
        Response response = updateUserInfo(null, update);
        checkUserUpdateWithoutAuthErrorStep(response);
    }
    @Test
    @DisplayName("Обновление имени пользователя без авторизации")
    @Description("Тест на обновление имени пользователя без авторизации")
    public void updateUserNameWithoutAuthorizationError() {
        String newName = generateUniqueName();
        UserModel update = new UserModel(null, null, newName);
        update.setName(newName);
        Response response = updateUserInfo(null, update);
        checkUserUpdateWithoutAuthErrorStep(response);
    }
    @Test
    @DisplayName("Обновление еmail пользователя без авторизации")
    @Description("Тест на обновление email пользователя без авторизации")
    public void updateUserEmailWithoutAuthorizationError() {
        String newEmail = generateUniqueEmail();
        UserModel update = new UserModel(newEmail, null, null);
        Response response = updateUserInfo(null, update);
        checkUserUpdateWithoutAuthErrorStep(response);
    }
    @Test
    @DisplayName("Обновление email на уже используемый адрес")
    @Description("Тест на передачу почты для обновления, которая уже используется")
    public void updateUserEmailWithExistingEmailError() {
        Response loginResponse = loginUser(user);
        refreshToken = loginResponse.then().extract().path("refreshToken");
        accessToken = loginResponse.then().extract().path("accessToken");

        UserModel update = new UserModel(USER_EMAIL, null, null);
        Response response = updateUserInfo(accessToken, update);
        checkUpdateEmailWithExistingErrorStep(response);
    }

    @Step("Проверка успешного изменение имени пользователя")
    public void checkSuccessfulUserNameUpdateStep(Response response, String newName) {
        response.then().statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.name", equalTo(newName));
    }
    @Step ("Проверка успешного обновления email пользователя")
    public void checkSuccessfulUserEmailUpdateStep(Response response, String newEmail) {
        response.then().statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail));
    }
    @Step ("Проверка успешного обновление пароля пользователя")
    public void checkSuccessfulUserPasswordUpdateStep(Response response, String newPassword) {
        response.then().statusCode(SC_OK)
                .body("success", equalTo(true));
    }
    @Step("Проверка ошибки при изменение данных пользователя без авторизации")
    public void checkUserUpdateWithoutAuthErrorStep(Response response) {
        response.then().statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false));
    }
    @Step ("Проверка ошибки при передаче почты для обновления, которая уже используется")
    public void checkUpdateEmailWithExistingErrorStep(Response response) {
        response.then().statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User with such email already exists"));
    }
}
