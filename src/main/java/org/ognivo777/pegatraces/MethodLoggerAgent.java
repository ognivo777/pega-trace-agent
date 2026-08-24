package org.ognivo777.pegatraces;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
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

        AgentBuilder agentBuilder = new AgentBuilder.Default().with(
                new ByteBuddy(ClassFileVersion.JAVA_V8) //TODO: config
        );

        // Here we build set of handlers, based provided config
        AdviceRegestry adviceRegestry = new AdviceRegestry(config);
        // Here we attach handlers to JVM
        adviceRegestry.apply(agentBuilder, instrumentation);

    }
}