package entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Weather {

    @JsonProperty(value = "main")
    private Main mainInfo;

    public Main getMainInfo() {
        return mainInfo;
    }

    private static class Main {
        private Main(
                @JsonProperty(value = "temp") double temp,
                @JsonProperty(value = "temp_min") double tempMin,
                @JsonProperty(value = "temp_max") double tempMax,
                @JsonProperty(value = "pressure") double pressure) {
            this.temp = temp;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.pressure = pressure;
        }

        private final double temp;
        private final double tempMin;
        private final double tempMax;
        private final double pressure;

        public double getTemp() {
            return temp;
        }

        public double getTempMin() {
            return tempMin;
        }

        public double getTempMax() {
            return tempMax;
        }

        public double getPressure() {
            return pressure;
        }
    }

}
