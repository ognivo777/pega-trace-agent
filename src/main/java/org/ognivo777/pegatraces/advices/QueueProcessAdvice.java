package org.ognivo777.pegatraces.advices;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.runtime.PublicAPI;
import net.bytebuddy.asm.Advice;

public class QueueProcessAdvice implements BasicClassMethodHandler {

    private static final String[] QP_CLASSES = {
            "com.pega.dsm.dnode.impl.dataflow.asyncexecutor.QueueProcessorExecutor"
    };
    private static final String[] QP_EXECUTION_METHODS = {"execute"};

    public String[] getClassNames() {
        return QP_CLASSES;
    }

    @Override
    public String[] getMethodNames() {
        return QP_EXECUTION_METHODS;
    }

    @Advice.OnMethodEnter
    public static long enter() {
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.Enter long startTime,
            @Advice.Argument(0) PublicAPI aTools,
            @Advice.Argument(1) ClipboardPage aContextPage,
            @Advice.Argument(2) ClipboardPage aDataFlowConfig,
            @Advice.Thrown Throwable throwable) {
        long duration = System.nanoTime() - startTime;
        String pzActivityToRun = aContextPage.getStringIfPresent("pzActivityToRun");
        String processorName = aDataFlowConfig.getString("pyPurpose");
        String exitMessage =
                "[QP][" + duration / 1_000_000 + "ms]" +
                "[" + processorName + "][" + pzActivityToRun + "]" +
                " |" + (throwable != null ? throwable.getClass().getSimpleName() + ":" + throwable.getMessage() : "");
        System.out.println(exitMessage);
    }

}
