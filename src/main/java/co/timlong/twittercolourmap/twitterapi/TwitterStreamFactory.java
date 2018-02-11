package co.timlong.twittercolourmap.twitterapi;

import co.timlong.twittercolourmap.twitterapi.authentication.ApplicationOAuth2;
import co.timlong.twittercolourmap.twitterapi.authentication.OAuth;
import co.timlong.twittercolourmap.twitterapi.model.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Service
public class TwitterStreamFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterStreamFactory.class);
    private static final String STREAM_API_ENDPOINT = "https://stream.twitter.com/1.1";

    public Flux<Tweet> filterStream(final OAuth auth, final Map<String, Object> params) {
        String url = STREAM_API_ENDPOINT + "/statuses/filter.json";

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        return Mono.just(WebClient.create())
                .map(client -> client.post()
                        .uri(url)
                        .syncBody(body)
                        .accept(MediaType.APPLICATION_STREAM_JSON)
                )
                .doOnNext(client -> {
                    String authHeader = auth.getAuthorizationValue("POST", url, params);
                    client.header("Authorization", authHeader);
                })
                .map(WebClient.RequestHeadersSpec::retrieve)
                .flatMapMany(client -> client.bodyToFlux(Tweet.class))
                .onErrorMap(t -> !(t instanceof TwitterApiException), TwitterApiException::new);
    }

    public Flux<Tweet> sampleStream(OAuth auth) {
        String url = STREAM_API_ENDPOINT + "/statuses/sample.json";

        return Mono.just(WebClient.create())
                .map(client -> client.get()
                        .uri(url)
                        .accept(MediaType.APPLICATION_STREAM_JSON)
                )
                .doOnNext(client -> {
                    String authHeader = auth.getAuthorizationValue("GET", url, Collections.emptyMap());
                    client.header("Authorization", authHeader);
                })
                .map(WebClient.RequestHeadersSpec::retrieve)
                .flatMapMany(client -> client.bodyToFlux(Tweet.class))
                .onErrorMap(t -> !(t instanceof TwitterApiException), TwitterApiException::new);
    }
}
