package org.example.adeem.Service;

import org.example.adeem.API.APIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class ClaudeApiService {

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.model:claude-sonnet-4-5-20250929}")
    private String model;

    private static final String SYSTEM_PROMPT = """
            أنت مساعد طبي أولي متخصص بالأمراض الجلدية ضمن منصة تطبيب عن بعد اسمها أديم.
            
            قواعد صارمة يجب الالتزام بها دائماً:
            1. لا تعطِ تشخيصاً نهائياً أو قطعياً لأي حالة أبداً - أنت أداة مساعدة فقط.
            2. لا تصف أدوية أو جرعات علاجية محددة.
            3. اشرح المعلومات العامة عن الأعراض الجلدية الشائعة بشكل تثقيفي فقط.
            4. في نهاية كل رد، ذكّر المستخدم بوضوح أن هذا لا يغني عن مراجعة طبيب مختص عبر المنصة.
            5. إذا ذكر المستخدم أعراضاً قد تكون خطيرة أو طارئة (نزيف شديد، انتشار سريع، حمى مرتفعة مصاحبة لطفح جلدي، صعوبة تنفس)، وجّهه فوراً لحجز استشارة عاجلة أو التوجه لأقرب طوارئ، ولا تحاول تهدئته بنفسك أو تقليل الأمر.
            6. تحدث بلغة عربية واضحة وبسيطة، بأسلوب متعاطف ومطمئن دون مبالغة.
            7. لا تتناول مواضيع خارج نطاق الأمراض الجلدية والاستخدام الطبي للمنصة.
            """;

    public String sendMessage(List<Map<String, String>> conversationHistory) {

        RestClient restClient = RestClient.create();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
                "system", SYSTEM_PROMPT,
                "messages", conversationHistory
        );

        try {
            String response = restClient.post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractTextFromResponse(response);

        } catch (Exception e) {
            System.out.println("=== CLAUDE API ERROR DEBUG ===");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Exception message: " + e.getMessage());

            if (e instanceof org.springframework.web.client.HttpClientErrorException httpError) {
                System.out.println("HTTP Status: " + httpError.getStatusCode());
                System.out.println("Response body: " + httpError.getResponseBodyAsString());
            }
            System.out.println("================================");

            throw new org.example.adeem.API.ExternalServiceException(
                    "Failed to get response from Claude API", e);
        }

    }

    private String extractTextFromResponse(String rawResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode contentArray = root.get("content");

            StringBuilder text = new StringBuilder();
            for (JsonNode block : contentArray) {
                if ("text".equals(block.get("type").asText())) {
                    text.append(block.get("text").asText());
                }
            }
            return text.toString();

        } catch (Exception e) {
            throw new APIException("Failed to parse chatbot response");
        }
    }
}