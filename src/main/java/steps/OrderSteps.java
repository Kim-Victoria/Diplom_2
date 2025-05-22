package steps;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static models.EndPoints.*;

public class OrderSteps {

    public static Response createOrder(String accessToken, List<String> validIngredients) {
        RequestSpecification request = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Collections.singletonMap("ingredients", validIngredients))
                .log().all();

        if (accessToken != null && !accessToken.isBlank()) {
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }
            request.header("Authorization", "Bearer " + accessToken);
        }

        return request.when().post(CREATE_ORDER_PATH);
    }

    public static Response getUserListOfOrders(String accessToken) {
        RequestSpecification request = given().contentType(ContentType.JSON);

        if (accessToken != null && !accessToken.isBlank()) {
            request.header("Authorization", accessToken);
        }

        return request
                .when()
                .get(GET_USER_ORDERS_PATH)
                .then()
                .extract().response();
    }

    public static void cancelOrder(String orderNumber) {
        if (orderNumber != null && !orderNumber.isBlank()) {
            given()
                    .when()
                    .delete(CANCEL_ORDER_PATH)
                    .then()
                    .extract().response();
        }
    }
}
