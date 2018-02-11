package co.timlong.twittercolourmap.twitterapi.authentication;

import co.timlong.twittercolourmap.twitterapi.model.BearerAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Map;

public class ApplicationOAuth2 {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationOAuth2.class);

    private final String consumerKey;
    private final String consumerSecret;

    public ApplicationOAuth2(final String consumerKey, final String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public Mono<Map<String, String>> authHeaders() {
        return Mono.just(this)
                .map(auth -> urlEncode(auth.consumerKey) + ':' + urlEncode(auth.consumerSecret))
                .map(token -> Base64Utils.encodeToString(token.getBytes()))
                .flatMap(token -> WebClient.create()
                        .post()
                        .uri("https://api.twitter.com/oauth2/token")
                        .header("Authorization", "Basic " + token)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .syncBody("grant_type=client_credentials")
                        .retrieve()
                        .bodyToMono(BearerAccessToken.class)
                )
                .map(token -> Map.of("Authorization", token.getAuthorizationHeaderValue()))
                .onErrorMap(TwitterAuthException::new);
    }

    private String urlEncode(final String source) {
        return UriUtils.encode(source, Charset.forName("UTF-8"));
    }
}