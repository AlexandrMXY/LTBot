package backend.academy.scrapper.configuration;

import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.dto.updates.Update;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    public static final String LINKS_TEMPLATE = "redisLinksTemplate";
    public static final String UPDATES_TEMPLATE = "redisUpdatesTemplate";

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean(LINKS_TEMPLATE)
    public RedisTemplate<String, List<LinkDto>> redisLinksTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, List<LinkDto>> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean(UPDATES_TEMPLATE)
    public RedisTemplate<String, Update> redisUpdatesTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Update> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
