package org.ognivo777.pegatraces;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.ognivo777.pegatraces.advices.AdviceRegestry;

import java.lang.instrument.Instrumentation;

public class MethodLoggerAgent {

    public static void premain(
            String agentArgs,
            Instrumentation instrumentation) {

        install(agentArgs, instrumentation);
    }

    public static void agentmain(
            String agentArgs,
            Instrumentation instrumentation) {

        install(agentArgs, instrumentation);
    }

    private static void install(
            String agentArgs,
            Instrumentation instrumentation) {
        AgentConfig config = AgentConfig.parse(agentArgs);

        AgentBuilder.Default agentBuilder = new AgentBuilder.Default();

        AdviceRegestry adviceRegestry = new AdviceRegestry(config);
        adviceRegestry.apply(agentBuilder, instrumentation);

    }
}