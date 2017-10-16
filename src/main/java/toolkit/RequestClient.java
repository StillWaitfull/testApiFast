package toolkit;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RequestClient {
    private static final Logger log = LoggerFactory.getLogger(RequestClient.class);
    private static final String ENCODING = "UTF-8";
    private String responseText = "";
    private HttpEntity body;
    private final Set<BasicHeader> headers = Collections.synchronizedSet(new HashSet<>());
    private final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
    private final CookieStore cookieStore = new BasicCookieStore();
    private final HttpClientContext context = HttpClientContext.create();
    private String token;
    private int statusCode;
    private final CloseableHttpClient httpClient = HttpClients.custom()
            .disableRedirectHandling()
            .setDefaultRequestConfig(globalConfig)
            .setSSLSocketFactory(new SSLConnectionSocketFactory(createSslContext(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
            .setSSLContext(createSslContext())
            .setDefaultCookieStore(cookieStore)
            .build();


    {
        context.setCookieStore(cookieStore);
    }


    public RequestClient setEntity(HttpEntity body) {
        this.body = body;
        return this;
    }

    private static final X509TrustManager X_509_TRUST_MANAGER = new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {

            return null;
        }

        public void checkClientTrusted(
                X509Certificate[] certs, String authType) {

        }

        public void checkServerTrusted(
                X509Certificate[] certs, String authType) {

        }
    };

    private SSLContext createSslContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null,
                    new TrustManager[]{X_509_TRUST_MANAGER}, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext;
    }


    public RequestClient addHeader(String name, String value) {
        headers.add(new BasicHeader(name, value));
        return this;
    }

    public RequestClient setBody(String body) {
        this.body = new StringEntity(body, ENCODING);
        return this;
    }

    public RequestClient setBody(HttpEntity body) {
        this.body = body;
        return this;
    }

    public String getResponseText() {
        return responseText;
    }


    public RequestClient sendRequest(METHODS method, String url, List<BasicNameValuePair> params) {
        HttpEntityEnclosingRequestBase request;
        try {
            URI uri = new URI(method.equals(METHODS.GET) ? (url + "?" + URLEncodedUtils.format(params, ENCODING)) : url);
            request = new HttpRequestWithBody(uri, method);
            request.setEntity(new UrlEncodedFormEntity(params, ENCODING));
            String requestLine = request.getRequestLine().toString();
            headers.forEach(request::addHeader);
            log.info("ApiRequest is " + requestLine);
            HttpResponse response = httpClient.execute(request, context);
            statusCode = response.getStatusLine().getStatusCode();
            responseText = EntityUtils.toString(response.getEntity(), ENCODING);
            log.info("Response is " + responseText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public RequestClient sendRequest(METHODS method, String url) {
        try {
            HttpEntityEnclosingRequestBase request = new HttpRequestWithBody(new URI(url), method);
            request.setEntity(body);
            headers.forEach(request::addHeader);
            log.info("ApiRequest is " + request.getRequestLine());
            HttpResponse response = httpClient.execute(request, context);
            responseText = EntityUtils.toString(response.getEntity(), ENCODING);
            log.info("Response is " + responseText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public String jsonPath(String path) {
        return JsonPath.read(responseText, path).toString();
    }

    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    public RequestClient addCookies(List<Cookie> cookies) {
        cookies.forEach(cookieStore::addCookie);
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }


    public enum METHODS {
        GET, POST, PUT, HEAD, DELETE
    }


    class HttpRequestWithBody extends HttpEntityEnclosingRequestBase {
        String METHOD_NAME = "GET";

        public String getMethod() {
            return METHOD_NAME;
        }

        HttpRequestWithBody(final URI uri, RequestClient.METHODS method) {
            super();
            METHOD_NAME = method.name();
            setURI(uri);
        }
    }


}