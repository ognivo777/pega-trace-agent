package org.ognivo777.pegatraces.advices;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.instrument.Instrumentation;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isNative;
import static net.bytebuddy.matcher.ElementMatchers.not;

public interface PegaEventHandler {
    String[] getClassNames();
    String[] getMethodNames();

    default void inject(AgentBuilder agentBuilder, Instrumentation instrumentation) {
        agentBuilder
                .type(
//                        ElementMatchers.namedOneOf(getClassNames())
                        ElementMatchers
                                .hasSuperType(ElementMatchers.namedOneOf(getClassNames()))
                                .or(ElementMatchers.namedOneOf(getClassNames()))
                ).
                transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(
                                Advice.to(this.getClass())
                                        .on(
                                                ElementMatchers.namedOneOf(getMethodNames())
                                                        .and(isMethod())
                                                        .and(not(isAbstract()))
                                                        .and(not(isNative()))
                                        )
                        )
                )
                // Disable adding fields or changing method signatures
                .disableClassFormatChanges()
                // Install the transformer into the JVM
                .installOn(instrumentation);
    }
}
