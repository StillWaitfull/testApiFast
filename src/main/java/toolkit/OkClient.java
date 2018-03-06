package toolkit;

import config.ApplicationConfig;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class OkClient {
    private final static Logger log = LoggerFactory.getLogger(OkClient.class);
    public final static ThreadLocal<String> RESPONSE_THREAD_LOCAL = new ThreadLocal<>();
    private final static ThreadLocal<String> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    private static final int TIMEOUT = Integer.parseInt(ApplicationConfig.TIMEOUT);
    private static final ConcurrentHashMap<Pair<HttpUrl, Headers>, Pair<Response, String>> CACHE_REQUESTS = new ConcurrentHashMap<>();


    private static Map<String, String> getRequiredParamsMap() {
        Map<String, String> params = new HashMap<>();
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

    private final Interceptor cache = chain -> {
        Pair<Response, String> responsePair = simpleCache(chain);
        Response response = responsePair.first();
        return response.newBuilder().body(ResponseBody.create(Objects.requireNonNull(response.body()).contentType(), responsePair.second())).build();
    };

    private final Interceptor logger = chain -> {
        Pair<Response, String> responseStringPair = proceedRequest(chain);
        Response response = responseStringPair.first();
        return response.newBuilder().body(ResponseBody.create(Objects.requireNonNull(response.body()).contentType(), responseStringPair.second())).build();
    };

    private final Interceptor requiredParams = chain -> {
        Request request = chain.request();
        request = addParamToRequest(request, getRequiredParamsMap());
        return chain.proceed(request);
    };

    private Pair<Response, String> proceedRequest(Interceptor.Chain chain) {
        try {
            Request request = chain.request();
            String requestBody = bodyToString(request.body());
            Response response = chain.proceed(request);
            String responseBody = Objects.requireNonNull(response.body()).string();
            REQUEST_THREAD_LOCAL.set(request.method().equals("GET") ? request.url().toString() : requestBody);
            RESPONSE_THREAD_LOCAL.set(responseBody);
                log.debug(String.format("Request url is %s", request.url()));
                log.debug(String.format("Request body is %s", requestBody));
                log.info(String.format("Response for %s  with response\n %s \n", response.request().url(), responseBody));

            return new Pair<>(response, RESPONSE_THREAD_LOCAL.get());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    private Pair<Response, String> simpleCache(Interceptor.Chain chain) {
        Request request = chain.request();
        if (request.method().equals("GET")) {
            Pair<HttpUrl, Headers> pairRequest = new Pair<>(request.url(), request.headers());
            if (CACHE_REQUESTS.keySet().contains(pairRequest)) {
                return CACHE_REQUESTS.get(pairRequest);
            }
            Pair<Response, String> pairResponse = proceedRequest(chain);
            CACHE_REQUESTS.put(pairRequest, pairResponse);
            return pairResponse;
        }
        return proceedRequest(chain);
    }

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };


    private OkHttpClient.Builder getBuilder() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return new OkHttpClient.Builder()
                    .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) TRUST_ALL_CERTS[0])
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    OkHttpClient getApiClient(boolean cacheOn) {
        OkHttpClient.Builder builder = getBuilder();
        builder.addInterceptor(cacheOn?cache:logger);
        return builder.build();
    }


}
