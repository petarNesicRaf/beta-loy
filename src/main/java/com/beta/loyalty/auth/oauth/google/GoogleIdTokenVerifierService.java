package com.beta.loyalty.auth.oauth.google;

import com.beta.loyalty.auth.oauth.VerifiedOidcUser;
import com.beta.loyalty.common.exceptions.UnauthorizedException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoogleIdTokenVerifierService {
    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenVerifierService(@Value("${auth.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();
    }

    public VerifiedOidcUser verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) throw new UnauthorizedException("Invalid Google token");

            GoogleIdToken.Payload payload = idToken.getPayload();

            String subject = payload.getSubject();
            String email = payload.getEmail(); // can be null depending on consent/scopes
            String name = (String) payload.get("name");

            return new VerifiedOidcUser(subject, email, name);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Google token");
        }
    }

}
