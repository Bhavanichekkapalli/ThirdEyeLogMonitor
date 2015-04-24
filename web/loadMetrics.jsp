<html>
<script>
function dwnHighRiskPat(){
    document.getElementById('requestAction').value="dwnHighRiskPat";

}
function dwnLowRiskPat(){
    document.getElementById('requestAction').value="dwnLowRiskPat";

}
</script>
<body>
<form action ="demo" method="post" name="myform" >
<input type="hidden" name="requestAction" id="requestAction" />

<table border="0" style="width:500px;" >
<tr><td colspan="2" style="color: blue;" align="center"><b> <h2>Log Metrics </h2></b></td></tr>
<tr><td colspan="2" style="color: blue;" align="center"></td></tr>
<tr><td style="color: red;" align="center">High Risk Pattern Count</td>
<td align="center"><%=request.getAttribute("highRiskPatList") %></td>
<td><input type="Submit" name="Submit" value="Download" onclick="javascript:dwnHighRiskPat()"/></td></tr>
<tr>
<td style="color: orange;" align="center"> Low Risk Pattern Count</td>
<td align="center"><%=request.getAttribute("lowRiskPatList") %></td>
<td><input type="Submit" name="Submit" value="Download" onclick="javascript:dwnLowRiskPat()"/></td>
</tr>
</table>
</form></body></html>