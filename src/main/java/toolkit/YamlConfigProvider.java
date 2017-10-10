package toolkit;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConfigProvider {

    private static final Yaml yaml = new Yaml();
    private static final String configFilePath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "configs" + File.separator;
    private static  Map paramsMap = new HashMap();
    private static  Map appParamsMap = new HashMap();

    static {
        String appConfigs = "application.yml";
        appParamsMap = loadToMap(appConfigs, false);

        String configName = System.getenv("stage");
        if (configName == null) {
            configName = String.valueOf(appParamsMap.get("configName"));
        }

        paramsMap = loadToMap(configName, true);

    }

    private static Map loadToMap(String path, boolean config) {
        Map loadMap;
        try {
            Iterable<Object> params = yaml.loadAll(new FileInputStream(new File(config ? (configFilePath + path) : path)));
            loadMap = (LinkedHashMap) params.iterator().next();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("file with configs not found " + path);
        }
        return loadMap;
    }

    public static String getStageParameters(String parameter) {
        if (!paramsMap.containsKey(parameter))
            throw new RuntimeException("There is no parameter " + parameter + " in stage config");
        return String.valueOf(paramsMap.get(parameter));
    }

    public static String getAppParameters(String parameter) {
        if (!appParamsMap.containsKey(parameter))
            throw new RuntimeException("There is no parameter " + parameter + " in application config");
        return String.valueOf(appParamsMap.get(parameter));
    }
}
