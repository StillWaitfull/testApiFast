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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class OkHttp {
    private final static Logger log = LoggerFactory.getLogger(OkHttp.class);
    public final static ThreadLocal<String> RESPONSE_THREAD_LOCAL = new ThreadLocal<>();
    private final static ThreadLocal<String> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int TIMEOUT = Integer.parseInt(ApplicationConfig.TIMEOUT);
    private static final ConcurrentHashMap<HttpUrl, Pair<Response, String>> CACHE_REQUESTS = new ConcurrentHashMap<>();


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
        Pair<Response, String> responsePair = simpleCache(chain);
        Response response = responsePair.first();
        MediaType contentType = response.body().contentType();
        ResponseBody responseBody = ResponseBody.create(contentType, responsePair.second());
        return response.newBuilder().body(responseBody).build();
    };

    private Response proceedRequest(Interceptor.Chain chain) {
        try {
            Request request = chain.request();
            String requestBody = bodyToString(request.body());
            log.debug(String.format("Request url is %s", request.url()));
            log.debug(String.format("Request body is %s", requestBody));
            Response response = chain.proceed(request);
            String responseBody = response.body().string();
            REQUEST_THREAD_LOCAL.set(requestBody);
            RESPONSE_THREAD_LOCAL.set(responseBody);
            log.info(String.format("Response for %s  with response\n %s \n", response.request().url(), responseBody));
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Pair<Response, String> simpleCache(Interceptor.Chain chain) {
        Request request = chain.request();
        if (request.method().equals("GET")) {
            if (CACHE_REQUESTS.keySet().contains(request.url()))
                return CACHE_REQUESTS.get(request.url());
            Response response = proceedRequest(chain);
            Pair<Response, String> pair = new Pair<>(response, RESPONSE_THREAD_LOCAL.get());
            CACHE_REQUESTS.put(request.url(), pair);
            return pair;
        }
        return new Pair<>(proceedRequest(chain), RESPONSE_THREAD_LOCAL.get());
    }


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
