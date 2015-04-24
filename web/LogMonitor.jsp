<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Log Monitor</title>

<script>
	function showPage(id,pageName){
		var iframeObj = document.getElementById(id);
		
		if(iframeObj){
			if(pageName == "logMonitorSettings"){
				iframeObj.src = "logSettings.jsp";
			}else if(pageName == "downloadTraceLog"){
				iframeObj.src = "loadDownload.jsp";
			}else if(pageName == "traceMetrics"){
				iframeObj.src = "demo?requestAction=loadMetrics";
			}
		}
	}
</script>
</head>
<body>
<div style="border-size: 1px;border: solid 2px #c0c0c0;">
<table border="0"  width="100%" height="100%" style="background-color:#ffffff;">
	<tr>
		<td colspan="2" style="border-size: 1px;border: solid 2px #c0c0c0;text-align: top;" title="Eye that never sleeps..."><h1 style="color:#dc0138;"  > <img src="./images/eye.png" align="down;"/> Third Eye </h1></td>
	</tr>
	<tr bordercolor="red">
		<td width="20%" style="border-size: 1px;border: solid 2px #c0c0c0; padding-bottom: 480px;"> 
			<table>
				<tr>
					<td> 
						<a href="#" onclick="showPage('rightPanelIframe','logMonitorSettings');">Log Monitor Settings</a>
					</td>
				</tr>
				<tr>
					<td> 
						<a href="#" onclick="showPage('rightPanelIframe','downloadTraceLog');" >Download Trace Log</a>
					</td>
				</tr>
				<tr>
					<td> 
						<a href="#" onclick="showPage('rightPanelIframe','traceMetrics');" >Trace Metrics</a>
					</td>
				</tr>
			</table>
		</td>
		<td width="80%" > 
			<iframe style="border-size: 1px;border: solid 2px #c0c0c0; padding-bottom: 0px;"  id="rightPanelIframe" name="rightPanelIframe" src="" width="100%" height="600px;"/>
		</td>
	</tr>
</table>
</div>
</body>
</html>