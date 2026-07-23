package org.example.adeem.Service;

import org.example.adeem.API.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Service
public class ZoomApiService {

    @Value("${zoom.account-id}")
    private String accountId;

    @Value("${zoom.client-id}")
    private String clientId;

    @Value("${zoom.client-secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();

    // ==================== الخطوة 1: جيب access token ====================
    private String getAccessToken() {

        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        try {
            Map<String, Object> response = restClient.post()
                    .uri("https://zoom.us/oauth/token?grant_type=account_credentials&account_id=" + accountId)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .retrieve()
                    .body(Map.class);

            return (String) response.get("access_token");

        } catch (Exception e) {
            throw new ExternalServiceException("Failed to authenticate with Zoom", e);
        }
    }

    // ==================== الخطوة 2: أنشئ اجتماع فعلي ====================
    public Map<String, String> createMeeting(String topic, LocalDateTime startTime) {

        String accessToken = getAccessToken();

        String formattedTime = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Map<String, Object> requestBody = Map.of(
                "topic", topic,
                "type", 2,
                "start_time", formattedTime,
                "duration", 30,
                "timezone", "Asia/Riyadh",
                "settings", Map.of(
                        "host_video", true,
                        "participant_video", true,
                        "join_before_host", false,
                        "waiting_room", true,
                        "auto_recording", "none"
                )
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("https://api.zoom.us/v2/users/me/meetings")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String meetingId = String.valueOf(response.get("id"));
            String joinUrl = (String) response.get("join_url");

            return Map.of(
                    "meetingId", meetingId,
                    "joinUrl", joinUrl
            );

        } catch (Exception e) {
            throw new ExternalServiceException("Failed to create Zoom meeting", e);
        }
    }
}