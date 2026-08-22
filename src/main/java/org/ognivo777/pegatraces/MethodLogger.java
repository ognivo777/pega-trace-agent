package org.ognivo777.pegatraces;

import com.pega.pegarules.pub.context.PRRequestor;
import com.pega.pegarules.pub.context.PRThread;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.util.StringMap;
import net.bytebuddy.asm.Advice;

public class MethodLogger {

    @Advice.OnMethodEnter
    public static long enter(
            @Advice.Origin("#t.#m") String method
//            , @Advice.FieldValue("mThread") Object mThread
//            , @Advice.Argument(0) StringMap aKeys
//            ,
//            @Advice.Argument(1) ClipboardPage aNewPrimaryPage,
//            @Advice.Argument(2) ParameterPage aNewParam
    ) {
        System.out.println(
                " -------- [ENTER] method: " +
                method + " thread=" +
                Thread.currentThread().getName() + " --------"
        );
        PRThread theThread = ThreadContainer.get();
        if(theThread!=null) {
            String prThreadName = theThread.getName();
            String requestorId = "NA";
            PRRequestor requestor = theThread.getRequestor();
            if (requestor != null) {
                requestorId = requestor.getId();
            }
            System.out.println(
                    " prThread=" + prThreadName +
                    " requestor=" + requestorId);
        }
//        if(aKeys!=null){
//            String activityClass = aKeys.getString("pxObjClass");
//            String activityName = aKeys.getString("pyActivityName");
//            String forClass = aKeys.getString("pyClassName");
//            System.out.printf("%s: %s#%s(%n",activityClass, forClass, activityName);
//            for (Object aKey : aKeys.keySet()) {
//                String aStringKey = (String) aKey;
//                String value = aKeys.getString(aStringKey);
//                System.out.printf("%s=%s%n", aStringKey, value);
//            }
//            System.out.println(")");
//        }

//        if (mThread!=null) {
//            System.out.println("mThread.class:" + mThread.getClass());
//        }

        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.Enter long startTime,
            @Advice.Origin("#t.#m") String method,
            @Advice.Thrown Throwable throwable) {

        long duration = System.nanoTime() - startTime;

        if (throwable == null) {
            System.out.println(
                    "[EXIT]  " +
                    method);
        } else {
            System.out.println(
                    "[EXIT]  " +
                    method +
                    " exception=" +
                    throwable);
        }

        System.out.println("<<< Exited method: " + method + " (Took " + (duration / 1_000_000.0) + "ms)");

    }
}