<#if packageName??>
package ${packageName};
</#if>

import org.springframework.stereotype.Service;
<#if imports??>
<#list imports as imp>
import ${imp};
</#list>
</#if>

@Service
public class ${className}Impl implements ${className} {

    @Override
    public ${returnType} ${methodName}(${parameters}) {
        // Implement the method here
        <#if returnType != "void">
        return null;
        </#if>
    }
}
