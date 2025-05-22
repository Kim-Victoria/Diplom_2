package steps;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.UserModel;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static models.EndPoints.*;

public class UserSteps {

    public static Response createUser(UserModel user) {
            return given()
                    .contentType(ContentType.JSON)
                    .body(user)
                    .when()
                    .post(CREATE_USER_PATH)
                    .then()
                    .extract().response();
        }
        public static Response loginUser(UserModel user) {
            return given()
                    .contentType(ContentType.JSON)
                    .body(user)
                    .when()
                    .post(LOGIN_USER_PATH)
                    .then()
                    .extract().response();
        }
        public static void deleteUser(String userToken) {
            given()
                    .when()
                    .delete(DELETE_USER_PATH + userToken)
                    .then()
                    .extract().response();
        }

        public static Response updateUserInfo(String accessToken, UserModel update) {
            RequestSpecification request = given()
                .contentType(ContentType.JSON)
                .body(update);

            if (accessToken != null) {
            request.header("Authorization", accessToken);
            }

            return request
                .when()
                .patch(UPDATE_USER_PATH);
        }

        public static String generateUniqueLogin() {
            return "testUser_" + UUID.randomUUID();
        }

        public static String generateUniqueEmail() {
            return "user_" + System.currentTimeMillis() + "@example.com";
        }

        public static String generateUniqueName() {
            return "user_" + UUID.randomUUID();
        }
}
