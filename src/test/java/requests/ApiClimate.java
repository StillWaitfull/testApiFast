package requests;

import entities.Data;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static requests.Routes.CLIMATE;


public interface ApiClimate {

    @GET(CLIMATE)
    Call<Data> getClimateDataByCity(
            @Query("q") String city,
            @Query("appid") String appId
    );
}
