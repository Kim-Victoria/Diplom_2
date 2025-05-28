package data;

import io.qameta.allure.Step;

import java.util.UUID;

public class GenerateValues {
    @Step("Генерация логина пользователя")
    public static String generateUniqueLogin() {
        return "testUser_" + UUID.randomUUID();
    }
    @Step("Генерация email пользователя")
    public static String generateUniqueEmail() {
        return "user_" + System.currentTimeMillis() + "@example.com";
    }
    @Step("Генерация имени пользователя")
    public static String generateUniqueName() {
        return "user_" + UUID.randomUUID();
    }
    @Step("Генерация пароля пользователя")
    public static String generateUniquePassword() {
        return UUID.randomUUID().toString().substring(6);
    }
}
