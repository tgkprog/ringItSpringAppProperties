package sel2in.config;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("timeZones");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(32)
                .expireAfterWrite(Duration.ofHours(6)));
        return cacheManager;
    }
}