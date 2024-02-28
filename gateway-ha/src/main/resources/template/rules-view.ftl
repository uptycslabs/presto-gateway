<#-- @ftlvariable name="" type="com.lyft.data.gateway.resource.GatewayViewResource$GatewayView" -->
<#setting datetime_format = "MM/dd/yyyy hh:mm:ss a '('zzz')'">
    <html>
<head>
    <meta charset="UTF-8"/>
    <style>
        .pull-left {
            float: left !important
        }

        .pull-right {
            float: right !important
        }

        .dataTables_filter input {
            width: 500px
        }
    </style>
    <link rel="stylesheet" type="text/css" href="assets/css/common.css"/>
    <link rel="stylesheet" type="text/css" href="assets/css/jquery.dataTables.min.css"/>

    <script src="assets/js/jquery-3.3.1.js"></script>
    <script src="assets/js/jquery.dataTables.min.js"></script>
    <script src="assets/js/hbar-chart.js"></script>
    <script src="assets/js/view-rules.js" defer></script>


    <script type="application/javascript">
        $(document).ready(function () {
            $('#queryHistory').DataTable(
                {
                    "ordering": false,
                    "dom": '<"pull-left"f><"pull-right"l>tip',
                    "width": '100%'
                }
            );
            $("ul.chart").hBarChart();
            document.getElementById("active_backends_tab").style.backgroundColor = "grey";
            renderRulesConfig();
        });
    </script>
</head>
<body>
<#include "header.ftl">
<div>
    Started at :
    <script>document.write(new Date(${gatewayStartTime?long?c}).toLocaleString());</script>
</div>


<div>
    <h3>Routing Rules:</h3>
    <pre><code id="rules"></code></pre>
</div>


<#include "footer.ftl">