package toolkit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.StageConfig;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public enum RETROFITS {

    RETROFIT(true) {
        @Override
        public Retrofit getRetrofit() {
            return builder
                    .client(new OkClient().setCacheOn(cacheOn).getApiClient())
                    .addConverterFactory(
                            JacksonConverterFactory.create(new ObjectMapper()
                                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                            ))
                    .build();
        }
    }, RETROFIT_WITHOUT_CACHE(false) {
        @Override
        public Retrofit getRetrofit() {
            return builder
                    .client(new OkClient().setCacheOn(cacheOn).getApiClient())
                    .addConverterFactory(
                            JacksonConverterFactory.create(new ObjectMapper()
                                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                            ))
                    .build();
        }
    };
    final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(StageConfig.BASE_URL);

    boolean cacheOn;

    public boolean isCacheOn() {
        return cacheOn;
    }

    RETROFITS(boolean cacheOn) {
        this.cacheOn = cacheOn;
    }

    public abstract Retrofit getRetrofit();
}
