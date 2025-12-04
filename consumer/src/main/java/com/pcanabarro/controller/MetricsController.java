package com.pcanabarro.controller;

import com.pcanabarro.metrics.GlobalFlowCounter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @PostMapping("/reset-flow-counter")
    public String resetFlowCounter() {
        GlobalFlowCounter.END_TO_END_MESSAGES.set(0);
        GlobalFlowCounter.LAST_BATCH_START_NS.set(System.nanoTime());
        return "Global end-to-end message counter and time reset.";
    }

    @GetMapping("/flow-counter")
    public long getFlowCounter() {
        return GlobalFlowCounter.END_TO_END_MESSAGES.get();
    }
}
