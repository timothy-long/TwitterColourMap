package co.timlong.twittercolourmap.twitterapi.authentication;

import org.springframework.util.Base64Utils;
import org.springframework.web.util.UriUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Basic OAuth implementation.
 *
 * @see <a href="https://developer.twitter.com/en/docs/basics/authentication/guides/authorizing-a-request">Authorizing a request</a>
 * @see <a href="https://developer.twitter.com/en/docs/basics/authentication/guides/creating-a-signature.html">Creating a signature</a>
 */
public class OAuth {
    private final String consumerKey;
    private final String consumerSecret;
    private final String accessTokenKey;
    private final String accessTokenSecret;

    public OAuth(String consumerKey, String consumerSecret, String accessTokenKey, String accessTokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessTokenKey = accessTokenKey;
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getAuthorizationValue(final String method, final String url, final Map<String, Object> params) {
        String oAuthNonce = UUID.randomUUID().toString().replace("-", "");
        String oAuthTimestamp = String.valueOf(Instant.now().getEpochSecond());

        TreeMap<String, Object> signingParams = new TreeMap<>(params);
        signingParams.put("oauth_consumer_key", consumerKey);
        signingParams.put("oauth_nonce", oAuthNonce);
        signingParams.put("oauth_signature_method", "HMAC-SHA1");
        signingParams.put("oauth_timestamp", oAuthTimestamp);
        signingParams.put("oauth_token", accessTokenKey);
        signingParams.put("oauth_version", "1.0");
        String allParams = signingParams.entrySet()
                .stream()
                .map(e -> urlEncode(e.getKey()) + '=' + urlEncode(e.getValue().toString()))
                .collect(Collectors.joining("&"));

        String value = method + "&"
                + urlEncode(url) + "&"
                + urlEncode(allParams);

        String signKey = urlEncode(consumerSecret) + '&' + urlEncode(accessTokenSecret);
        byte[] signKeyBytes = signKey.getBytes();
        SecretKeySpec signingKey = new SecretKeySpec(signKeyBytes, "HmacSHA1");
        byte[] signedKeyBytes;

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            signedKeyBytes = mac.doFinal(value.getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new TwitterAuthException(e);
        }

        String signedKey = Base64Utils.encodeToString(signedKeyBytes);

        return "OAuth "
                + "oauth_consumer_key=\"" + urlEncode(consumerKey) + "\", "
                + "oauth_token=\"" + urlEncode(accessTokenKey) + "\", "
                + "oauth_signature=\"" + urlEncode(signedKey) + "\", "
                + "oauth_signature_method=\"HMAC-SHA1\", "
                + "oauth_timestamp=\"" + urlEncode(oAuthTimestamp) + "\", "
                + "oauth_nonce=\"" + urlEncode(oAuthNonce) + "\", "
                + "oauth_version=\"1.0\"";

    }

    private String urlEncode(final String source) {
        return UriUtils.encode(source, Charset.forName("UTF-8"));
    }
}
