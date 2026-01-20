package com.vupico.ticket.service;

import com.vupico.ticket.domain.Category;
import com.vupico.ticket.domain.ClassifyResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TicketClassifierService {

    private final ChatClient chatClient;

    @Value("${prompts.classifier.system-prompt}")
    private String systemPrompt;

    @Value("${prompts.classifier.repair-prompt}")
    private String repairPrompt;

    public TicketClassifierService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ClassifyResponse classify(String ticketText) {

        Exception last = null;

        // This retry loop can be avoided if we use validation Advisor, for now I am going with a simple implementation due to time
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                ClassifyResponse resp = this.chatClient.prompt()
                        .system(systemPrompt)
                        .user(ticketText)
                        .call()
                        .entity(ClassifyResponse.class);

                double c = resp.confidence();
                if (c < 0.0) c = 0.0;
                if (c > 1.0) c = 1.0;

                String reason = (resp.reason() == null) ? "" : resp.reason().trim();
                if (reason.isBlank()) {
                    reason = "Insufficient detail; defaulting to general support.";
                }

                return new ClassifyResponse(resp.category(), c, reason);

            } catch (Exception e) {
                last = e;
                try {
                    ClassifyResponse resp = this.chatClient.prompt()
                            .system(systemPrompt + "\n\n" + repairPrompt)
                            .user(ticketText)
                            .call()
                            .entity(ClassifyResponse.class);

                    double c = resp.confidence();
                    if (c < 0.0) c = 0.0;
                    if (c > 1.0) c = 1.0;

                    String reason = (resp.reason() == null) ? "" : resp.reason().trim();
                    if (reason.isBlank()) {
                        reason = "Insufficient detail; defaulting to general support.";
                    }

                    return new ClassifyResponse(resp.category(), c, reason);

                } catch (Exception retryEx) {
                    last = retryEx;
                }
            }
        }

        // Fallback never fails endpoint
        return new ClassifyResponse(Category.GENERAL, 0.50,
                "Model output was invalid; defaulting to general support.");
    }

}
