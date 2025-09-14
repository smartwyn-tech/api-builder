package io.apicurio.hub.api.beans;

import java.util.List;

public class InterfaceInfo {
    private final List<MethodInfo> methodInfoList;
    private final String packageName;
    private final List<String> imports;

    public InterfaceInfo(List<MethodInfo> methodInfoList, String packageName, List<String> imports) {
        this.methodInfoList = methodInfoList;
        this.packageName = packageName;
        this.imports = imports;
    }

    public List<MethodInfo> getMethodInfoList() {
        return methodInfoList;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<String> getImports() {
        return imports;
    }

    @Override
    public String toString() {
        return "InterfaceInfo{" +
                "methodInfoList=" + methodInfoList +
                ", packageName='" + packageName + '\'' +
                ", imports=" + imports +
                '}';
    }
}

