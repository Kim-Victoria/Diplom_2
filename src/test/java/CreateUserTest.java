import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.UserModel;
import org.junit.After;
import org.junit.Test;
import steps.UserSteps;

import static data.GenerateValues.generateUniqueEmail;
import static data.TestValues.*;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static steps.UserSteps.*;

@DisplayName("Тесты на создание пользователя")
public class CreateUserTest extends BaseTest {
private UserModel user;
private String userToken;

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
        @DisplayName("Успешное создание пользователя")
        @Description("Тест на создание пользователя со всеми валидными данными")
        public void createUserSuccessful() {
            user = generateUserStep(generateUniqueEmail(), USER_PASSWORD, USER_NAME);
            Response response = createUser(user);
            checkSuccessfulUserCreationStep(response);
    }

        @Test
        @DisplayName("Создание пользователя, который уже зарегистрирован")
        @Description("Тест на создание пользователя, который уже зарегистрирован")
        public void createAlreadyRegisteredUser() {
            String email = generateUniqueEmail();
            user = generateUserStep(email, USER_PASSWORD, USER_NAME);
            createUser(user);
            Response response = createUser(user);
            checkExistingUserCreationErrorStep(response);
        }

        @Test
        @DisplayName("Создание пользователя без указания email")
        @Description("Тест на создание пользователя без обязательного поля email")
        public void createUserWithoutEmail() {
            user = generateUserStep("", USER_PASSWORD, USER_NAME);
            Response response = createUser(user);
            checkUserCreationErrorWithoutEssentialStep(response);
        }
        @Test
        @DisplayName("Создание пользователя без указания пароля")
        @Description("Тест на создание пользователя без обязательного поля с паролем")
        public void createUserWithoutPassword() {
            user = generateUserStep(USER_EMAIL, "", USER_NAME);
            Response response = createUser(user);
            checkUserCreationErrorWithoutEssentialStep(response);
        }
        @Test
        @DisplayName("Создание пользователя без указания имени")
        @Description("Тест на создание пользователя без обязательного поля с именем")
        public void createUserWithoutName() {
            user = generateUserStep(USER_EMAIL, USER_PASSWORD, "");
            Response response = createUser(user);
            checkUserCreationErrorWithoutEssentialStep(response);
        }

        @Step("Создание модели пользователя")
        public UserModel generateUserStep(String email, String password, String name) {
            return new UserModel(email, password, name);
        }
        @Step ("Проверка на успешное создание пользователя в системе")
        public void checkSuccessfulUserCreationStep(Response response) {
            response.then().statusCode(SC_OK).body("success", equalTo(true));
        }
        @Step ("Проверка ошибки при создании пользователя, уже существующего в системе")
        public void checkExistingUserCreationErrorStep(Response response) {
            response.then().statusCode(SC_FORBIDDEN).body("success", equalTo(false)).body("message", equalTo("User already exists"));
        }
        @Step ("Проверка ошибки при создании пользователя без указания обязательного поля")
        public void checkUserCreationErrorWithoutEssentialStep(Response response) {
            response.then().statusCode(SC_FORBIDDEN).body("success", equalTo(false)).body("message", equalTo( "Email, password and name are required fields"));
        }
}

