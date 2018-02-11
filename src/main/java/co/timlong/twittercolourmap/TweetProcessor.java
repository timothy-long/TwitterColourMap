package co.timlong.twittercolourmap;

import co.timlong.twittercolourmap.twitterapi.TwitterStreamFactory;
import co.timlong.twittercolourmap.twitterapi.authentication.OAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class TweetProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(TweetProcessor.class);
    private final TwitterStreamFactory twitter;
    private final OAuth authentication;
    private Disposable subscriber;

    public TweetProcessor(TwitterStreamFactory twitter, OAuth authentication) {
        this.twitter = twitter;
        this.authentication = authentication;
    }

    @PostConstruct
    public void startConsuming() {
        LOG.info("Starting streaming...");

        subscriber = twitter.sampleStream(authentication)
                .doOnNext(t -> LOG.trace("Processing {}", t))
                .doOnError(t -> LOG.error("Error processing stream", t))
                .doOnComplete(() -> LOG.warn("Stream ended"))
                .subscribe();
    }

    @PreDestroy
    public void stopConsuming() {
        if (subscriber != null) {
            LOG.info("Cancelling subscription...");
            subscriber.dispose();
            subscriber = null;
        }
    }
}
