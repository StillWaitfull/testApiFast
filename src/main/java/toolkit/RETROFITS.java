package toolkit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import config.StageConfig;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public enum RETROFITS {

    RETROFIT {
        private boolean cacheON=true;
        @Override
        public Retrofit getRetrofit() {
            return builder
                    .client(new OkClient().getApiClient(cacheON))
                    .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                    .build();
        }

        @Override
        public boolean getCache() {
            return cacheON;
        }
    },
    RETROFIT_WITHOUT_CACHE {
        private boolean cacheON=false;
        @Override
        public Retrofit getRetrofit() {
            return builder
                    .client(new OkClient().getApiClient(cacheON))
                    .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                    .build();
        }

        @Override
        public boolean getCache() {
            return cacheON;
        }
    };
    final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(StageConfig.BASE_URL);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public abstract Retrofit getRetrofit();
    public abstract boolean getCache();
}
