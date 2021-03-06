package com.talestonini.blackbook;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.talestonini.blackbook.model.GmailQuery;
import com.talestonini.blackbook.repository.GmailQueryRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import static com.google.api.client.util.Preconditions.checkArgument;

public class Utils {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();

    private static final AppEngineDataStoreFactory DATA_STORE_FACTORY = AppEngineDataStoreFactory.getDefaultInstance();

    private static final String APPLICATION_NAME = "Gmail Black Book";

    private static GoogleClientSecrets clientSecrets = null;

    private Utils() {
    }

    public static User getCurrentUser() {
        UserService userService = UserServiceFactory.getUserService();
        return userService.getCurrentUser();
    }

    public static String createLoginURL(HttpServletRequest request) {
        UserService userService = UserServiceFactory.getUserService();
        return userService.createLoginURL(request.getRequestURI());
    }

    public static String createLogoutURL(HttpServletRequest request) {
        UserService userService = UserServiceFactory.getUserService();
        return userService.createLogoutURL(request.getRequestURI());
    }

    public static List<GmailQuery> findGmailQueries(String emailAddress) {
        return GmailQueryRepository.getInstance().findByEmailAddress(emailAddress);
    }

    public static Gmail getGmailService(String userId) throws IOException {
        Credential credential = buildNewFlow().loadCredential(userId);
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static GoogleAuthorizationCodeFlow buildNewFlow() throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, getClientCredential(),
                Collections.singleton(GmailScopes.MAIL_GOOGLE_COM))
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
    }

    public static String getRedirectUri(HttpServletRequest req) {
        GenericUrl url = new GenericUrl(req.getRequestURL().toString());
        url.setRawPath("/oauth2callback");
        return url.build();
    }

    private static GoogleClientSecrets getClientCredential() throws IOException {
        if (clientSecrets == null) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(Utils.class.getResourceAsStream("/client_secrets.json")));

            checkArgument(!clientSecrets.getDetails().getClientId().startsWith("Enter ")
                            && !clientSecrets.getDetails().getClientSecret().startsWith("Enter "),
                    "Download client_secrets.json file from Google Cloud Platform Console.");
        }
        return clientSecrets;
    }
}
