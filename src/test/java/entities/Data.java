package entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Data {

    private final String cod;
    private final float message;
    private final int cnt;
    private final List<Weather> list;
    public Data(
            @JsonProperty(value = "cod", required = true) String cod,
            @JsonProperty(value = "message", required = true) float message,
            @JsonProperty(value = "cnt", required = true) int cnt,
            @JsonProperty(value = "list", required = true) List<Weather> list) {
        this.cod = cod;
        this.message = message;
        this.cnt = cnt;
        this.list = list;
    }

    public List<Weather> getList() {
        return list;
    }

    public String getCod() {
        return cod;
    }

    public float getMessage() {
        return message;
    }

    public int getCnt() {
        return cnt;
    }
}

