package toolkit;

import config.StageConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestCreator {

    private static final Map<Class, Object> RETROFITS = new ConcurrentHashMap<>();
    private static final String BASE_URL = StageConfig.getInstance().BASE_URL;

    public static Object getRequestInterface(Class clazz, RETROFITS retrofit) {
        if (!retrofit.getCache())
            return retrofit.getRetrofit(BASE_URL).create(clazz);
        else if (RETROFITS.containsKey(clazz)) {
            return RETROFITS.get(clazz);
        } else {
            Object requestInterface = retrofit.getRetrofit(BASE_URL).create(clazz);
            RETROFITS.put(clazz, requestInterface);
            return requestInterface;
        }
    }
}
