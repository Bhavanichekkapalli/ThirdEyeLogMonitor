package testDemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import demo.hackathon.client.LogMonitorClient;

public class PatterenSelectServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// Set the response message's MIME type
		response.setContentType("text/html;charset=UTF-8");
		// Allocate a output writer to write the response message into the
		// network socket
		// PrintWriter out = response.getWriter();

		try {
			String requestAction = request.getParameter("requestAction");
			if (requestAction.equalsIgnoreCase("download")) {
				LogMonitorClient logMonitorClient = new LogMonitorClient();
				byte[] downloadData = logMonitorClient.downloadTraceFile();
				// response.setContentType(MimeType );
				response.setHeader("Content-Disposition",
						"attachment;filename=" + "TraceLog.log");
				ServletOutputStream stream = response.getOutputStream();
				stream.write(downloadData);
				stream.flush();
				/*
				 * RequestDispatcher rd = request
				 * .getRequestDispatcher("/download.jsp"); rd.forward(request,
				 * response);
				 */
			} else if (requestAction.equalsIgnoreCase("loadMetrics")) {

				LogMonitorClient logMonitorClient = new LogMonitorClient();
				int highRiskCnt = logMonitorClient.getHighRiskAlertsCount();
				int lowRiskCnt = logMonitorClient.getLowRiskAlertsCount();
				request.setAttribute("lowRiskPatList", lowRiskCnt);
				request.setAttribute("highRiskPatList", highRiskCnt);
				RequestDispatcher rd = request
						.getRequestDispatcher("/loadMetrics.jsp");
				rd.forward(request, response);
			}else if (requestAction.equalsIgnoreCase("dwnHighRiskPat")) {

				LogMonitorClient logMonitorClient = new LogMonitorClient();
				byte[] downloadData = logMonitorClient.downloadHighRiskTraceFile();
				// response.setContentType(MimeType );
				response.setHeader("Content-Disposition",
						"attachment;filename=" + "HighRiskTraceLog.csv");
				ServletOutputStream stream = response.getOutputStream();
				stream.write(downloadData);
				stream.flush();
				
			} else if (requestAction.equalsIgnoreCase("dwnLowRiskPat")) {

				LogMonitorClient logMonitorClient = new LogMonitorClient();
				byte[] downloadData = logMonitorClient.downloadLowRiskTraceFile();
				// response.setContentType(MimeType );
				response.setHeader("Content-Disposition",
						"attachment;filename=" + "LowRiskTraceLog.csv");
				ServletOutputStream stream = response.getOutputStream();
				stream.write(downloadData);
				stream.flush();
				
			} else if (requestAction.equalsIgnoreCase("loadLogSettings")) {

				String highRiskpattListVal = request
						.getParameter("patternListVal");
				String fileListVal = request.getParameter("fileListVal");
				String notificationType = request.getParameter("alertNotify");
				String emailListVal = request.getParameter("mailListVal");
				String directoryName = request.getParameter("fileDir");
				String isDirSearchReq = request.getParameter("fileDirChk");
				if("ON".equalsIgnoreCase(isDirSearchReq)){
					isDirSearchReq="YES";
				}else{
					isDirSearchReq="NO";
				}
				String timeDelay = request.getParameter("timeDelay");
				String lowRiskPatVal = request
						.getParameter("lowpatternListVal");

				ArrayList<String> mailList = new ArrayList<String>();
				ArrayList<String> fileList = new ArrayList<String>();
				ArrayList<String> hightRiskPatList = new ArrayList<String>();
				ArrayList<String> lowRiskPatList = new ArrayList<String>();

				String pattValArr[] = highRiskpattListVal.split(",");
				for (int i = 0; i < pattValArr.length; i++) {
					hightRiskPatList.add(pattValArr[0]);

				}

				String LowpattValArr[] = lowRiskPatVal.split(",");
				for (int i = 0; i < LowpattValArr.length; i++) {
					lowRiskPatList.add(LowpattValArr[0]);

				}
				String fileListArr[] = fileListVal.split(",");
				for (int i = 0; i < fileListArr.length; i++) {
					fileList.add(fileListArr[0]);

				}

				String emailListArr[] = emailListVal.split(",");
				for (int i = 0; i < emailListArr.length; i++) {
					mailList.add(emailListArr[0]);

				}

				Runnable thread = new LogMonitorClient(directoryName, fileList,
						hightRiskPatList, lowRiskPatList, notificationType,
						mailList, isDirSearchReq);
				thread.run();

				System.out.println(mailList.size() + " ::" + fileList.size()
						+ " ::" + notificationType + "::"
						+ hightRiskPatList.size() + "::" + directoryName + "::"
						+ isDirSearchReq);

				RequestDispatcher rd = request
						.getRequestDispatcher("/test.jsp");
				rd.forward(request, response);// method may be include or
												// forward

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// out.close(); // Always close the output writer
		}
	}
}