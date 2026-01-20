package com.vupico.ticket.api;

import com.vupico.ticket.domain.ClassifyRequest;
import com.vupico.ticket.domain.ClassifyResponse;
import com.vupico.ticket.service.TicketClassifierService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class TicketController {

    private final TicketClassifierService service;

    public TicketController(TicketClassifierService service) {
        this.service = service;
    }

    @PostMapping("/classify")
    public ClassifyResponse classify(@Valid @RequestBody ClassifyRequest request) {
        return service.classify(request.text());
    }
}
