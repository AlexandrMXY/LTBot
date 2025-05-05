package backend.academy.bot.config;

import backend.academy.api.model.responses.ListLinksResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Profile("!noRedis")
public class RedisConfig {
    public static final String LINKS_TEMPLATE = "redisLinksTemplate";

    @Bean(LINKS_TEMPLATE)
    public RedisTemplate<String, ListLinksResponse> redisLinksTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ListLinksResponse> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
