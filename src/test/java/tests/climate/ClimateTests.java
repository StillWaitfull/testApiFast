package tests.climate;

import entities.Data;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.ApiClimate;
import retrofit2.Response;
import tests.AbstractTest;
import toolkit.RequestCreator;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static toolkit.OkClient.getResponseText;
import static toolkit.RETROFITS.RETROFIT;

@Feature("Функциональные тесты на получение данных о погоде по городу")
public class ClimateTests extends AbstractTest {

    private static final ApiClimate API_CLIMATE = (ApiClimate) RequestCreator.getRequestInterface(ApiClimate.class, RETROFIT);
    private static final String APP_ID = "b1b15e88fa797225412429c1c50c122a1";


    @Story("Проверка получения корректного результата")
    @Test
    void getWeatherByCityTest() throws IOException {
        String city = "London";
        Response<Data> weatherListResponse = API_CLIMATE.getClimateDataByCity(city, APP_ID).execute();
        Assertions.assertEquals(200, weatherListResponse.code(), "Код ответа на запрос погоды по городу " + city + " не 200 " + getResponseText());
        Data weatherList = weatherListResponse.body();
        Assertions.assertTrue(Objects.requireNonNull(weatherList).getList().size() > 0, "Запрос не вернул ни одного резульатата");
    }


    @Story("Проверка получения результата c некоректным городом")
    @Test
    void getWeatherByIncorrectCityTest() throws IOException {
        String appId = "b1b15e88fa797225412429c1c50c122a1";
        String city = UUID.randomUUID().toString();
        Response<Data> weatherListResponse = API_CLIMATE.getClimateDataByCity(city, appId).execute();
        Assertions.assertEquals(200, weatherListResponse.code(), "Код ответа на запрос погоды по городу " + city + " не 200 " + getResponseText());
        Data weatherList = weatherListResponse.body();
        Assertions.assertTrue(Objects.requireNonNull(weatherList).getList().size() > 0, "Запрос не вернул ни одного резульатата");
    }
}
