package tests.climate;

import entities.Data;
import org.junit.Assert;
import org.junit.Test;
import requests.ApiClimate;
import retrofit2.Response;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;
import tests.AbstractTest;
import toolkit.OkHttp;
import toolkit.RequestCreator;

import java.io.IOException;
import java.util.UUID;

import static toolkit.OkHttp.RESPONSE_THREAD_LOCAL;

@Features("Функциональные тесты на получение данных о погоде по городу")
public class ClimateTests extends AbstractTest {

    private static final ApiClimate API_CLIMATE = (ApiClimate) RequestCreator.getRequestInterface(ApiClimate.class, OkHttp.RETROFITS.RETROFIT.getRetrofit());
    private static final String APP_ID = "b1b15e88fa797225412429c1c50c122a1";


    @Stories("Проверка получения корректного результата")
    @Test
    public void getWeatherByCityTest() throws IOException {
        String city = "London";
        Response<Data> weatherListResponse = API_CLIMATE.getClimateDataByCity(city, APP_ID).execute();
        Assert.assertEquals("Код ответа на запрос погоды по городу " + city + " не 200 " + RESPONSE_THREAD_LOCAL.get(), 200, weatherListResponse.code());
        Data weatherList = weatherListResponse.body();
        Assert.assertTrue("Запрос не вернул ни одного резульатата", weatherList.getList().size() > 0);
    }


    @Stories("Проверка получения результата c некоректным городом")
    @Test
    public void getWeatherByIncorrectCityTest() throws IOException {
        String appId = "b1b15e88fa797225412429c1c50c122a1";
        String city = UUID.randomUUID().toString();
        Response<Data> weatherListResponse = API_CLIMATE.getClimateDataByCity(city, appId).execute();
        Assert.assertEquals("Код ответа на запрос погоды по городу " + city + " не 200 " + RESPONSE_THREAD_LOCAL.get(), 200, weatherListResponse.code());
        Data weatherList = weatherListResponse.body();
        Assert.assertTrue("Запрос не вернул ни одного резульатата", weatherList.getList().size() > 0);
    }
}
