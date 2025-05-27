import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.UserModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.OrderSteps;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static data.GenerateValues.generateUniqueEmail;
import static data.TestValues.USER_NAME;
import static data.TestValues.USER_PASSWORD;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static steps.OrderSteps.createOrder;
import static steps.UserSteps.*;

@DisplayName("Тесты на создание заказа")
public class CreateOrderTest extends BaseTest {
    private UserModel user;
    private String accessToken;
    private String orderNumber;
    private final List<String> validIngredients = Arrays.asList(
            "61c0c5a71d1f82001bdaaa6d",
            "61c0c5a71d1f82001bdaaa75"
    );
    private final List<String> invalidIngredients = Arrays.asList("invalid123", "invalid456");

    @Before
    public void setUp() {
        user = new UserModel(generateUniqueEmail(), USER_PASSWORD, USER_NAME);
        createUser(user);

    }
    @After
    public void tearDown() {
        if (orderNumber != null) {
            OrderSteps.cancelOrder(orderNumber);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Тест на успешное создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderWithAuth() {
        Response loginResponse = loginUser(user);
        loginResponse.then()
                .extract().path("Authorization", accessToken);
        Response response = createOrder(accessToken, validIngredients);
        checkSuccessfulOrderCreationStep(response);
    }
    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Тест на создание заказа с ингредиентами без авторизации")
    public void createOrderWithoutAuth() {
        Response response = createOrder(null, validIngredients);
        checkOrderCreationWithoutAuthorizationStep(response);
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Тест на ошибку при создании заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        Response response = createOrder(accessToken, Collections.emptyList());
        checkCreateOrderWithoutIngredientsErrorStep(response);
    }

    @Test
    @DisplayName("Создание заказа с невалидными ингредиентами")
    @Description("Тест на создание заказа с невалидным хешем ингредиента")
    public void createOrderWithInvalidIngredients() {
        Response response = createOrder(accessToken, invalidIngredients);
        checkCreateOrderWithInvalidIngredientsErrorStep(response);
    }
    @Step("Проверка успешного создания заказа после авторизации и с валидными игнгредиентами")
    public void checkSuccessfulOrderCreationStep(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .extract().path("order.number");
    }
    @Step("Проверка создания заказа без авторизации")
    public void checkOrderCreationWithoutAuthorizationStep(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .extract().path("order.number");
    }
    @Step("Проверка ошибки при создании заказа без ингредиентов")
    public void checkCreateOrderWithoutIngredientsErrorStep(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }
    @Step ("Проверка ошибки при создании заказа с невалидными ингредиентами")
    public void checkCreateOrderWithInvalidIngredientsErrorStep(Response response) {
        response.then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }
}
