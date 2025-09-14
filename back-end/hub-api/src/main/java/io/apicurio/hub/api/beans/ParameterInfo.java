package io.apicurio.hub.api.beans;

public class ParameterInfo {
    private final String paramName;
    private final String paramType;

    public ParameterInfo(String paramName, String paramType) {
        this.paramName = paramName;
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamType() {
        return paramType;
    }

    @Override
    public String toString() {
        return "Parameter: " + paramName + ", Parameter Type: " + paramType;
    }
}
