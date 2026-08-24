package org.ognivo777.pegatraces.advices;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.runtime.ParameterPage;
import com.pega.pegarules.pub.util.StringMap;
import com.pega.pegarules.session.internal.mgmt.PRStackFrameImpl;
import net.bytebuddy.asm.Advice;

public class ActivtyInternalAdvice implements BasicClassMethodHandler {

    private static final String[] IEXECUTABLE = {
//            "com.pega.pegarules.session.external.mgmt.IExecutable",
            "com.pega.pegarules.session.internal.mgmt.Executable"
    };

    private static final String[] ACTIVITY_METHODS = {"doActivity", "doAction"};

    public String[] getClassNames() {
        return IEXECUTABLE;
    }

    @Override
    public String[] getMethodNames() {
        return ACTIVITY_METHODS;
    }

    @Advice.OnMethodEnter
    public static long enter(

    ) {
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.Enter long startTime,
            @Advice.FieldValue("mTopOfStack") PRStackFrameImpl mTopOfStack,
            @Advice.Argument(0) StringMap aKeys,
            @Advice.Argument(1) ClipboardPage aNewPrimaryPage,
            @Advice.Argument(2) ParameterPage aNewParam
    ){
        try {
            long duration = System.nanoTime() - startTime;
            String activityClass = aKeys.getString("pxObjClass");
            boolean isRuleObjValidate = activityClass.equals("Rule-Obj-Validate");
            boolean isRuleObjActivity = isRuleObjValidate || activityClass.equals("Rule-Obj-Activity") || activityClass.equals("Rule-Generated-Activity");
            String activityName = "";
            String forClass = "";
            if (isRuleObjActivity) {
                activityName = aKeys.getString("pyActivityName");
                forClass = aKeys.getString("pyClassName");
            }

            String prevInfo = "TOP";
            String stepPageName = "NA";
            String stepPageClass = "NA";

            if(mTopOfStack!=null) {
                ClipboardPage stepPage = mTopOfStack.getStepPage();
//                ClipboardPage stepPage = mTopOfStack.getPrimaryPage();
                if (stepPage != null) {
                    stepPageName = stepPage.getName();
                    stepPageClass = stepPage.getClassName();
                }

                if (!mTopOfStack.isRoot() && mTopOfStack.getPrevious()!=null){
                    String prevLabel0 = mTopOfStack.getPrevious().getLabelVariable(0);
                    String prevLabel1 = mTopOfStack.getPrevious().getLabelVariable(1);
                    String prevLabel2 = mTopOfStack.getPrevious().getLabelVariable(2);
                    prevInfo = "" + prevLabel0 + "/" + prevLabel1 + "/" + prevLabel2;
                }
            }



//            PegaGeneralContext context = PegaGeneralContext.getContext();

            String exitMessage =
                    "[ACTIVITY][" + duration / 1_000_000 + "ms]" +
                            "[" + activityClass + "." + activityName + "][" + forClass + "]" +
                            " step page: " + stepPageName + "(" + stepPageClass + ")" +
                            " stack: " + prevInfo;

            System.out.println(exitMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
