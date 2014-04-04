<#import "/layouts/default.html.ftl" as layout/>
<!DOCTYPE html>
<html lang="en">
<head>
<@layout.header />
</head>
<body>
<div class="container span12">
${numbers}
${nestedNumbers}
${map}
<#list nestedNumbers as nn>
  <#list nn as n>
  ${n}
  </#list>
</#list>
<#list persons as p>
  ${p.id}
</#list>
${persons}
  Hello!
</div>
<@layout.bodyjs />
</body>
</html>
