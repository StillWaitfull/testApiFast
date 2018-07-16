package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class StageConfig {

    private static StageConfig instance;
    @JsonProperty("baseUrl")
    public String BASE_URL;

    public static StageConfig getInstance() {
        if (instance == null) {
            String stage = System.getenv("stage");
            if (stage == null) stage = ApplicationConfig.getInstance().CONFIG_NAME;
            String path = StageConfig.class.getClassLoader().getResource("configs" +File.separator+stage).getPath();
            try {
                instance = new ObjectMapper(new YAMLFactory()).readValue(new File(path), StageConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при парсинге файла с конфигами " + path);
            }
        }
        return instance;
    }
}
