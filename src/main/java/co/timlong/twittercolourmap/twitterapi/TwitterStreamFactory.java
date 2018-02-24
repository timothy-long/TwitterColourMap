package co.timlong.twittercolourmap.twitterapi;

import co.timlong.twittercolourmap.twitterapi.authentication.OAuth;
import co.timlong.twittercolourmap.twitterapi.model.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static java.util.Collections.emptyMap;

@Service
public class TwitterStreamFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterStreamFactory.class);
    private static final String STREAM_ENDPOINT = "https://stream.twitter.com/1.1";
    private static final String SAMPLE_STREAM_ENDPOINT = STREAM_ENDPOINT + "/statuses/sample.json";

    private WebClient client = WebClient.create();

    /**
     * Requests a stream of tweets from Twitter's sample stream. <br /><br />This stream handles all disconnects and
     * errors, meaning only a subscriber handling onNext is needed for this flux.
     *
     * @param auth OAuth authentication to use for the connection
     * @return a never completing stream of tweets
     * @see <a href="https://developer.twitter.com/en/docs/tweets/sample-realtime/overview/GET_statuse_sample">Sample realtime Tweets</a>
     */
    public Flux<Tweet> sampleStream(final OAuth auth) {
        return Mono.just(client)
                .map(client -> client.get().uri(SAMPLE_STREAM_ENDPOINT))
                .doOnNext(client -> {
                    String authHeader = auth.getAuthorizationValue("GET", SAMPLE_STREAM_ENDPOINT, emptyMap());
                    client.header("Authorization", authHeader);
                })
                .map(RequestHeadersSpec::retrieve)
                .flatMapMany(client -> client.bodyToFlux(Tweet.class))
                .doOnError(e -> LOG.error("Error retrieving stream", e))
                .doOnComplete(() -> {
                    LOG.warn("Stream disconnected.");
                    throw new TwitterStreamDisconnectedException();
                })
                .onErrorResume((e) -> {
                    Duration reconnectDelay = Duration.ofSeconds(30);
                    LOG.info("Reattempting stream in {}...", reconnectDelay);
                    return sampleStream(auth).delaySubscription(Duration.ofSeconds(30));
                });
    }
}