package org.ognivo777.pegatraces.advices;

import com.pega.pegarules.integration.config.internal.connect.ConnectorBaseImpl;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.context.PRRequestor;
import com.pega.pegarules.pub.context.PRThread;
import com.pega.pegarules.pub.context.ThreadContainer;
import net.bytebuddy.asm.Advice;

public class ConnectorAdvice implements BasicClassMethodHandler {
    private final static String[] CONNECTOR_CLASS = {
             "com.pega.pegarules.integration.config.internal.connect.ConnectorBaseImpl"
    };
    private final static String[] CONNECTOR_METHOD = {"invoke"};

    @Override
    public String[] getClassNames() {
        return CONNECTOR_CLASS;
    }

    @Override
    public String[] getMethodNames() {
        return CONNECTOR_METHOD;
    }

    @Advice.OnMethodEnter
    public static long enter(

    ) {
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.Enter long startTime,
            @Advice.This ConnectorBaseImpl connectorInstance,
            @Advice.FieldValue("mConnector") ClipboardPage mConnector,
            @Advice.Thrown Throwable throwable) {

        long duration = System.nanoTime() - startTime;

        PRThread theThread = ThreadContainer.get();
        String prThreadName = theThread.getName();
        String requestorId = "NA";
        PRRequestor requestor = theThread.getRequestor();
        if (requestor != null) {
            requestorId = requestor.getId();
        }

        String pyClassName = "NA";
        String pyRuleName = "NA";
        String pyRuleSet = "NA";
        String pyRuleSetVersion = "NA";
        if (mConnector != null) {
            pyClassName = mConnector.getString("pyClassName");
            pyRuleName = mConnector.getString("pyRuleName");
            pyRuleSet = mConnector.getString("pyRuleSet");
            pyRuleSetVersion = mConnector.getString("pyRuleSetVersion");
        }

        String exitMessage =
                "[CONNECTOR][" + duration / 1_000_000 + "ms]" +
                        "[" + connectorInstance.getPegaConnectorClass() + "]" +
                        "[" + connectorInstance.getPageClass() + "]" +
                        "[" + pyClassName + "][" + pyRuleName + "][" + pyRuleSet + ":" + pyRuleSetVersion + "][" +
                        "[" + prThreadName + "][" + requestorId + "] " +
                        " |" + (throwable != null ? throwable.getClass().getSimpleName() + ":" + throwable.getMessage() : "");

        System.out.println(exitMessage);

    }


}
