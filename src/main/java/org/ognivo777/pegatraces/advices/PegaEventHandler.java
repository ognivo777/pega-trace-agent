package org.ognivo777.pegatraces.advices;

import net.bytebuddy.agent.builder.AgentBuilder;
import java.lang.instrument.Instrumentation;

public interface PegaEventHandler {
    void inject(AgentBuilder agentBuilder, Instrumentation instrumentation);
}
