package org.ognivo777.pegatraces.advices;

import com.pega.pegarules.pub.context.PRRequestor;
import com.pega.pegarules.pub.context.PRThread;
import com.pega.pegarules.pub.context.ThreadContainer;
import net.bytebuddy.asm.Advice;

public class ServiceRestAdvice implements BasicClassMethodHandler {

    private static final String[] SERVICE_HANDLER_CLASSES = {
            "com.pega.pegarules.integration.engine.internal.services.ServiceAPI"
    };
    private static final String[] SERVICE_HANDLER_METHODS = {"processRequest"};

    public String[] getClassNames() {
        return SERVICE_HANDLER_CLASSES;
    }

    @Override
    public String[] getMethodNames() {
        return SERVICE_HANDLER_METHODS;
    }

    @Advice.OnMethodEnter
    public static long enter() {
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.Enter long startTime,
//            @Advice.This Object instance,
//            @Advice.Origin("#t.#m") String method,
            @Advice.FieldValue("mServiceType") String mServiceType,
            @Advice.FieldValue("mServicePackage") String mServicePackage,
            @Advice.FieldValue("mServiceClass") String mServiceClass,
            @Advice.FieldValue("mServiceMethod") String mServiceMethod,
            @Advice.FieldValue("mHttpMethod") String mHttpMethod,
            @Advice.FieldValue("mErrorMessage") String mErrorMessage,
            @Advice.FieldValue("mDataVolume") long mDataVolume,
            @Advice.Thrown Throwable throwable) {

        long duration = System.nanoTime() - startTime;

        PRThread theThread = ThreadContainer.get();
        String prThreadName = theThread.getName();
        String requestorId = "NA";
        PRRequestor requestor = theThread.getRequestor();
        if (requestor != null) {
            requestorId = requestor.getId();
        }

        //Do not worry, will be compiled as StringBuilder.append by javac
        String exitMessage =
                "[SERVICE][" + duration / 1_000_000 + "ms][" + mServiceType + "][" + mDataVolume + "][" + prThreadName + "][" + requestorId + "] " +
                mHttpMethod +
                "|" + mServicePackage +
                "|" + mServiceClass +
                "|" + mServiceMethod +
                "|" + (mErrorMessage!=null ? mErrorMessage : "") +
                "|" + (throwable!=null ? throwable.getClass().getSimpleName() + ":" + throwable.getMessage() : "");

        System.out.println(exitMessage);
    }

}
