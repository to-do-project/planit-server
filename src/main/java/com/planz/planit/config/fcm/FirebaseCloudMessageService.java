package com.planz.planit.config.fcm;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.planz.planit.config.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.planz.planit.config.BaseResponseStatus.FCM_SEND_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseCloudMessageService {
    @Value("${fcm.key.scope}")
    private String API_URL;
    private final ObjectMapper objectMapper;

    public void sendMessageTo(List<String> targetToken, String title, String body) throws BaseException {
        AtomicBoolean fcmPushSuccess = new AtomicBoolean(true);
        //log.info("sendMessageTo method");
        targetToken.forEach(i-> {
            try {
                String message = makeMessage(i, title, body);

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(message,
                        MediaType.get("application/json; charset=utf-8"));
                Request request = null;
                request = new Request.Builder()
                        .url(API_URL)
                        .post(requestBody)
                        .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                        .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                        .build();

                Response response = client.newCall(request).execute();

                log.info(response.body().string());
            } catch (IOException e) {
                log.info(e.toString());
                fcmPushSuccess.set(false);
            }
        });
        if(!fcmPushSuccess.get()){
            throw new BaseException(FCM_SEND_ERROR);
        }
    }
    private String makeMessage(String targetToken, String title, String body) throws JsonParseException, JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/firebase_service_key.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

}
