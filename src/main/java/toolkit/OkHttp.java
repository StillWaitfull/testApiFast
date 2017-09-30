package toolkit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ApplicationConfig;
import config.StageConfig;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class OkHttp {
    private final static Logger log = LoggerFactory.getLogger(OkHttp.class);
    public final static ThreadLocal<String> RESPONSE_THREAD_LOCAL = new ThreadLocal<>();
    private final static ThreadLocal<String> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int TIMEOUT = Integer.parseInt(ApplicationConfig.TIMEOUT);

    public enum RETROFITS {
        RETROFIT {
            @Override
            public Retrofit getRetrofit() {
                return builder
                        .addConverterFactory(
                                JacksonConverterFactory.create(mapper
                                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        ))
                        .build();
            }
        }, RETROFIT_WITHOUT_MAPPING {
            @Override
            public Retrofit getRetrofit() {
                return
                        builder.addConverterFactory(JacksonConverterFactory.create())
                                .build();
            }
        };
        final Retrofit.Builder builder = new Retrofit.Builder()
                .client(new OkHttp().getClient())
                .baseUrl(StageConfig.BASE_URL);

        public abstract Retrofit getRetrofit();
    }

    private static Map<String, String> getRequiredParamsMap() {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", "web/test2");
        params.put("usr_longitude", "37.774456");
        //    params.put("clear_cache", "1");
        params.put("usr_latitude", "55.655751");
        params.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
        return params;
    }




    private Request addParamToRequest(Request request, Map<String, String> params) {
        HttpUrl.Builder builderUrl = request.url().newBuilder();
        params.forEach(builderUrl::addQueryParameter);
        HttpUrl url = builderUrl.build();
        return request
                .newBuilder()
                .url(url)
                .build();
    }

    private String bodyToString(final RequestBody request) {
        try {
            final Buffer buffer = new Buffer();
            if (request != null)
                request.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    private final Interceptor interceptorGetData = chain -> {
        Request request = chain.request();
        String requestBody = bodyToString(request.body());
        String requestString = String.format("Request %s with Headers %s", request.url(), request.headers());
        log.debug(String.format("Request body is %s", requestBody));
        Response response = chain.proceed(request);
        String jsonString = response.body().string();
        RESPONSE_THREAD_LOCAL.set(jsonString);
        REQUEST_THREAD_LOCAL.set(requestString);
        log.info(String.format("Response for %s  with response\n %s \n", response.request().url(), jsonString));
        return chain.proceed(request);
    };




    private OkHttpClient getClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .addInterceptor(interceptorGetData)
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }

}
