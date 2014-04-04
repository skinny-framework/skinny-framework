<#macro header>
  <meta charset="utf-8"/>
  <meta content="${s.csrfKey}" name="${s.csrfKey}" />
  <script type="text/javascript" src="//code.jquery.com/jquery-2.1.0.min.js"></script>
  <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"/>
</#macro>

<#macro bodyjs>
<script type="text/javascript" src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/assets/js/skinny-framework.js" th:src="@{/assets/js/skinny-framework.js}"></script>
</#macro>

