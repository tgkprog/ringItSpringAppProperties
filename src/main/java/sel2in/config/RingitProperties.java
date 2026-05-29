package sel2in.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ringit")
public class RingitProperties {

    private String defaultTimeZone = "Asia/Kolkata";
    private List<String> supportedTimeZones = new ArrayList<>();
    private final Db db = new Db();

    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    public List<String> getSupportedTimeZones() {
        return supportedTimeZones;
    }

    public void setSupportedTimeZones(List<String> supportedTimeZones) {
        this.supportedTimeZones = supportedTimeZones;
    }

    public Db getDb() {
        return db;
    }

    public static class Db {

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}