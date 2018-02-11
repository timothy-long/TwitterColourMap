package co.timlong.twittercolourmap;

import co.timlong.twittercolourmap.twitterapi.authentication.OAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitterConfig {
    @Bean
    public OAuth oAuth(@Value("${twitter.auth.consumer.key}") final String consumerKey,
                       @Value("${twitter.auth.consumer.secret}") final String consumerSecret,
                       @Value("${twitter.auth.oauth.accesstoken.key}") final String accessTokenKey,
                       @Value("${twitter.auth.oauth.accesstoken.secret}") final String accessTokenSecret
    ) {
        return new OAuth(consumerKey, consumerSecret, accessTokenKey, accessTokenSecret);
    }
}
