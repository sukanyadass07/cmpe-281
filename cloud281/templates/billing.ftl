<h1>${message}<h1>
<#list billing?keys as key> 
    ${key} = ${billing[key]} 
</#list> 