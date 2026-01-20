package com.vupico.ticket.service;

import com.vupico.ticket.domain.Category;
import com.vupico.ticket.domain.ClassifyResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TicketClassifierService {

    private final ChatClient chatClient;

    public TicketClassifierService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ClassifyResponse classify(String ticketText) {

        String systemPrompt = """
            You are a strict support ticket classifier.

            Categories:
            - BILLING: charges, invoices, refunds, payment methods, subscriptions, pricing, credit card issues
            - TECHNICAL: bugs, errors, outages, performance, login failures, broken features, API issues
            - GENERAL: how-to, general queries, account questions not related to payment, feature requests

            Output rules:
            1) Output ONLY a JSON object with fields: category, confidence, reason
            2) category must be exactly one of: BILLING, TECHNICAL, GENERAL
            3) confidence must be between 0.0 and 1.0
            4) reason must be ONE short sentence
            5) If ambiguous, choose GENERAL with confidence <= 0.60
            """;

        String repairPrompt = """
            Your previous output was invalid.
            Return ONLY valid JSON with EXACT fields: category, confidence, reason.
            category must be BILLING or TECHNICAL or GENERAL.
            confidence must be a number between 0.0 and 1.0.
            reason must be ONE short sentence.
            No markdown. No extra keys. No extra text.
            """;

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
