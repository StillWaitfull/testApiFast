package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;


public class ApplicationConfig {

    private static ApplicationConfig instance;
    @JsonProperty("timeout")
    public String TIMEOUT;
    @JsonProperty("configName")
    public String CONFIG_NAME;
    @JsonProperty("pause")
    public Integer PAUSE;

    public static ApplicationConfig getInstance() {
        if (instance == null) {
            String path =  ApplicationConfig.class.getClassLoader().getResource("application.yml").getPath();;
            try {
                instance = new ObjectMapper(new YAMLFactory()).readValue(new File(path), ApplicationConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при парсинге файла с конфигами " + path);
            }
        }
        return instance;
    }


}
