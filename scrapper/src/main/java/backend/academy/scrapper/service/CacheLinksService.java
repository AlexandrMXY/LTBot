package backend.academy.scrapper.service;

import backend.academy.scrapper.configuration.RedisConfig;
import backend.academy.scrapper.dto.LinkDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CacheLinksService extends LinksService {
    private static final String USER_CACHE_PREFIX = "user:";

    @Autowired
    @Qualifier(RedisConfig.LINKS_TEMPLATE)
    private RedisTemplate<String, List<LinkDto>> template;

    @Override
    public LinkDto addLink(long userId, LinkDto link) {
        template.delete(USER_CACHE_PREFIX + userId);
        return super.addLink(userId, link);
    }

    @Override
    public LinkDto deleteLink(long userId, String url) {
        template.delete(USER_CACHE_PREFIX + userId);
        return super.deleteLink(userId, url);
    }

    @Override
    public List<LinkDto> getLinks(long userId) {
        String cacheKey = USER_CACHE_PREFIX + userId;
        List<LinkDto> result = template.opsForValue().get(cacheKey);
        if (result != null) {
            return result;
        }
        result = super.getLinks(userId);
        template.opsForValue().set(cacheKey, result);
        return result;
    }
}
