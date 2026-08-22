package org.ognivo777.pegatraces;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class PegaMatchers {

    public static final String MGMT_IEXECUTABLE = "com.pega.pegarules.session.external.mgmt.IExecutable";

    public static final String SERVICE_API = "com.pega.pegarules.integration.engine.internal.services.ServiceAPI";

    public static final String SERVICE_HANDLER = "com.pega.pegarules.integration.engine.internal.services.ServiceHandler";
    private static final String[] SERVICE_HANDLER_METHODS = {"processRequest"};


    public static final String MGMT_EXECUTABLE = "com.pega.pegarules.session.internal.mgmt.Executable";
    private static final String[] MGMT_EXECUTABLE_METHODS = {
            "doActivity",
            "doAction"
//            "invokeActivity",
//            "executeActivityInContext",
//            "invokeConnector",
//            "applyModel",
//            "doDataTransform",
//            "doDecision",
//            "executeReport",
//            "findDataPage",
//            "findDataPageByInsKey",
//            "createPage",
//            "findPage",
//            "findPageWithException",
//            "loadAsyncDeclarativePage",
//            "logAlertTraceEvent",
//            "logCall",
//            "queueActivityForAsynchronousExecution",
//            "startParseRuleTimer",
//            "stopParseRuleTimer",
//            "",
//            ""
    };

    public PegaMatchers(AgentConfig config) {
        //TODO: choose more granular what to handle
    }

    public ElementMatcher.Junction<TypeDescription> getMatcher(){
//        return ElementMatchers.hasSuperType(ElementMatchers.named(MGMT_EXECUTABLE));
        return ElementMatchers.hasSuperType(ElementMatchers.named(SERVICE_HANDLER)).or(ElementMatchers.named(SERVICE_HANDLER));
//        return ElementMatchers.namedOneOf(pegaClasses);
    }

    public ElementMatcher.Junction<? super MethodDescription> methodNames() {
//        return ElementMatchers.namedOneOf(MGMT_EXECUTABLE_METHODS);
        return ElementMatchers.namedOneOf(SERVICE_HANDLER_METHODS);
//        return ElementMatchers.any();
    }
}
