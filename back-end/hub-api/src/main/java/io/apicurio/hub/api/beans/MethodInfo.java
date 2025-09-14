package io.apicurio.hub.api.beans;

import java.util.List;

public class MethodInfo {
    private final String methodName;
    private final String returnType;
    private final List<ParameterInfo> parameters;

    public MethodInfo(String methodName, String returnType, List<ParameterInfo> parameters) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Method: " + methodName + ", Return Type: " + returnType);
        if (parameters != null) {
            result.append(", Parameters: ");
            for (ParameterInfo parameter : parameters) {
                result.append(parameter).append(", ");
            }
        }
        return result.toString();
    }
}
