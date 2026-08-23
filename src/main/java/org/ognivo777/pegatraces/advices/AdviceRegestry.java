package org.ognivo777.pegatraces.advices;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.ognivo777.pegatraces.AgentConfig;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class AdviceRegestry {
    private final List<PegaEventHandler> handlers = new ArrayList<>();

    public AdviceRegestry(AgentConfig config) {
        //TODO fill advices by config
        handlers.add(new ServiceRestAdvice());
    }

    public AgentBuilder apply(AgentBuilder agentBuilder, Instrumentation instrumentation) {
        for (PegaEventHandler handler : handlers) {
            handler.inject(agentBuilder, instrumentation);
        }
        return agentBuilder;
    }
}
