package toolkit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestCreator {

    private static final Map<Class, Object> RETROFITS = new ConcurrentHashMap<>();


    public static Object getRequestInterface(Class clazz, RETROFITS retrofit) {
        if (!retrofit.isCacheOn())
            return retrofit.getRetrofit().create(clazz);
        else if (RETROFITS.containsKey(clazz)) {
            return RETROFITS.get(clazz);
        } else {
            Object requestInterface = retrofit.getRetrofit().create(clazz);
            RETROFITS.put(clazz, requestInterface);
            return requestInterface;
        }
    }
}
