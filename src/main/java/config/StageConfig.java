package config;

import toolkit.YamlConfigProvider;


public class StageConfig {

    private static final String HOST = YamlConfigProvider.getStageParameters("host");

    private static final String PROTOCOL = YamlConfigProvider.getStageParameters("protocol") + "://";

    public static final String BASE_URL = PROTOCOL + HOST + YamlConfigProvider.getStageParameters("baseUrl");

}
