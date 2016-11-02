package edu.hneu.studentsportal.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

@Service
public class GmailService {

    @Autowired
    private OAuth2RestOperations oauth2RestTemplate;

    public Gmail api() {
        final GoogleCredential credential = new GoogleCredential().setAccessToken(oauth2RestTemplate.getAccessToken().getValue());
        return new Gmail.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName("henu-students").build();
    }

    public Message convertToGmailMessage(final MimeMessage email) throws MessagingException, IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        email.writeTo(byteArrayOutputStream);
        return new Message().setRaw(Base64.encodeBase64URLSafeString(byteArrayOutputStream.toByteArray()));
    }

}