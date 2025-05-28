import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.UserModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static data.GenerateValues.generateUniqueEmail;
import static data.TestValues.USER_NAME;
import static data.TestValues.USER_PASSWORD;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static steps.OrderSteps.createOrder;
import static steps.OrderSteps.getUserListOfOrders;
import static steps.UserSteps.*;

@DisplayName("Тесты на получение списка заказов пользователя")
public class GetUserListOfOrdersTest extends BaseTest {
    private UserModel user;
    private String accessToken;
    private String refreshToken;

    private final List<String> validIngredients = Arrays.asList(
            "61c0c5a71d1f82001bdaaa6d",
            "61c0c5a71d1f82001bdaaa6f",
            "61c0c5a71d1f82001bdaaa72"
    );

    @Before
    public void setUp() {
        user = new UserModel(generateUniqueEmail(), USER_PASSWORD, USER_NAME);
        createUser(user);

        Response loginResponse = loginUser(user);
        accessToken = loginResponse.then().extract().path("accessToken");
        refreshToken = loginResponse.then().extract().path("refreshToken");
        createOrder(accessToken, validIngredients);
    }

    @After
    public void cleanUp() {
        if (refreshToken != null && !refreshToken.isBlank()) {
            deleteUser(refreshToken);
        }
    }
    @Test
    @DisplayName("Получение списка заказов авторизованным пользователем")
    @Description("Тест на получение списка заказов, если пользователь авторизован")
    public void getOrdersAsAuthorizedUser() {
        Response response = getUserListOfOrders(accessToken);
        checkGettingOrdersWithAuthUserStep(response);
    }

    @Test
    @DisplayName("Ошибка при получении заказов без авторизации")
    @Description("Проверка, что без авторизации получить заказы невозможно")
    public void getOrdersAsUnauthorizedUser() {
        Response response = getUserListOfOrders(null);
        checkGettingOrdersWithoutAuthErrorStep(response);
    }

    @Step("Проверка успешного получения списка заказов авторизованного пользователя")
    public void checkGettingOrdersWithAuthUserStep(Response response) {
            response.then()
                    .statusCode(SC_OK)
                    .body("success", equalTo(true))
                    .body("orders", notNullValue())
                    .body("orders[0].ingredients", not(empty()))
                    .body("orders[0]", hasKey("status"))
                    .body("orders[0]", hasKey("number"))
                    .body("orders[0]", hasKey("createdAt"))
                    .body("orders[0]", hasKey("updatedAt"));
        }
    @Step ("Проверка ошибки при получении списка заказов без авторизации")
    public void checkGettingOrdersWithoutAuthErrorStep(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
