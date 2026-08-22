package org.ognivo777.pegatraces;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import org.ognivo777.pegatraces.advices.AdviceRegestry;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

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

        AdviceRegestry adviceRegestry = new AdviceRegestry(config);

        PegaMatchers pegaMatchers = new PegaMatchers(config);

        new AgentBuilder.Default()
                // 1. Log what the agent is doing to System.out
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
                .type(pegaMatchers.getMatcher())
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(
                                Advice.to(MethodLogger.class)
                                        .on(
                                                pegaMatchers.methodNames()
                                                        .and(isMethod())
                                                        .and(not(isAbstract()))
                                                        .and(not(isNative()))
                                        )
                        )
                )
                // Disable adding fields or changing method signatures
                .disableClassFormatChanges()
                // 4. Install the transformer into the JVM
                .installOn(instrumentation);
    }
}