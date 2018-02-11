package co.timlong.twittercolourmap.twitterapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Base64Utils;

public class BearerAccessToken {
    private final String accessToken;

    @JsonCreator
    public BearerAccessToken(@JsonProperty("access_token") String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAuthorizationHeaderValue() {
        return "Bearer " + Base64Utils.encodeToUrlSafeString(accessToken.getBytes());
    }
}
