package toolkit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public enum RETROFITS {

    RETROFIT {
        private boolean cacheON = true;

        @Override
        public Retrofit getRetrofit(String baseUrl) {
            return builder
                    .baseUrl(baseUrl)
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
        private boolean cacheON = false;

        @Override
        public Retrofit getRetrofit(String baseUrl) {
            return builder
                    .baseUrl(baseUrl)
                    .client(new OkClient().getApiClient(cacheON))
                    .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                    .build();
        }

        @Override
        public boolean getCache() {
            return cacheON;
        }
    };
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    final Retrofit.Builder builder = new Retrofit.Builder();

    public abstract Retrofit getRetrofit(String baseUrl);

    public abstract boolean getCache();
}
