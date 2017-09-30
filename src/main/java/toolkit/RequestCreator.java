package toolkit;

import retrofit2.Retrofit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestCreator {

    private static final Map<Class, Object> retrofits = new ConcurrentHashMap<>();


    public static Object getRequestInterface(Class clazz, Retrofit retrofit) {
        if (retrofits.containsKey(clazz))
            return retrofits.get(clazz);
        else {
            Object requestInterface = retrofit.create(clazz);
            retrofits.put(clazz, requestInterface);
            return requestInterface;
        }
    }
}
