package org.ognivo777.pegatraces.advices;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.context.PRRequestor;
import com.pega.pegarules.pub.context.PRThread;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.runtime.FUASupport;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isNative;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class FlowAdvice implements PegaEventHandler {
    private static final String ACTIVITY_INTERFACE = "com.pega.pegarules.pub.runtime.Activity";
    private static final String SERVICE_HANDLER_METHOD = "perform";


    @Advice.OnMethodEnter
    public static long enter() {
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.Enter long startTime,
            @Advice.This FUASupport flowInstance,
//            @Advice.Origin("#t.#m") String typeAndMethod,
            @Advice.FieldValue("mRuleSet") String mRuleSet,
            @Advice.FieldValue("flowType") String flowType,
            @Advice.FieldValue("flowName") String flowName,
            @Advice.FieldValue("bDraftMode") boolean bDraftMode,
            @Advice.FieldValue("mFlowHandle") String mFlowHandle,
            @Advice.FieldValue("interestPage") ClipboardPage interestPage,
            @Advice.Thrown Throwable throwable) {

        long duration = System.nanoTime() - startTime;

        PRThread theThread = ThreadContainer.get();
        String prThreadName = theThread.getName();
        String requestorId = "NA";
        PRRequestor requestor = theThread.getRequestor();
        if (requestor != null) {
            requestorId = requestor.getId();
        }

        String exitMessage =
                "[FLOW][" + duration / 1_000_000 + "ms]" +
//                "[" + typeAndMethod + "]" +
                "[CL:" + flowInstance.getDefinitionAppliesToClass() + " ID:" + flowType + " RS:" + mRuleSet + "]" +
                "[" + flowName + "][Draft:" + bDraftMode + "]" +
//                "[ " + mFlowHandle + "]" +
                "[Interest:" + (interestPage!=null? interestPage.getClassName():"NA") + "]" +
                "[pyID:" + (interestPage!=null?interestPage.getString("pyID"):"NA") + "][" + prThreadName + "][" + requestorId + "] " +
                " |" + (throwable!=null ? throwable.getClass().getSimpleName() + ":" + throwable.getMessage() : "");


        System.out.println(exitMessage);
    }

    @Override
    public void inject(AgentBuilder agentBuilder, Instrumentation instrumentation) {
        agentBuilder
                .type(
                        ElementMatchers
                                .hasSuperType(ElementMatchers.named(ACTIVITY_INTERFACE))
                                .and(ElementMatchers.nameStartsWith("com.pegarules.generated.flow.ra_action_"))
                ).
                transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.
                                visit(
                                Advice.to(this.getClass())
                                        .on(
                                                ElementMatchers.named(SERVICE_HANDLER_METHOD)
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
