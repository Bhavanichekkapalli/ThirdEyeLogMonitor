package demo.hackathon.client;

// Import the Java classes
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import demo.hackathon.GenericLogFileTailer;
import demo.hackathon.GenericLogFileTailerListener;
import demo.hackathon.NotificationType;


/* @Authors
 * Siva Adabala
 * Debraj Nath
 * Bhavani Chekkapalli
 */

/**
 * Implements console-based log file tailing, or more specifically, tail
 * following: it is somewhat equivalent to the unix command "tail -f"
 */
public class LogMonitorClient implements GenericLogFileTailerListener {
	/**
	 * The log file tailer
	 */
	private GenericLogFileTailer genericLogFileTailer;

	private static final int MYTHREADS = 30;

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	public Map<String,List<String>> getErrorPatterns() {
		return errorPatterns;
	}

	public void setErrorPatterns(Map<String,List<String>> errorPatterns) {
		this.errorPatterns = errorPatterns;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public List<String> getMailList() {
		return mailList;
	}

	public void setMailList(List<String> mailList) {
		this.mailList = mailList;
	}

	String directoryName;
	List<String> fileList;
	List<String> highRiskErrorPatterns;
	public List<String> getHighRiskErrorPatterns() {
		return highRiskErrorPatterns;
	}

	public void setHighRiskErrorPatterns(List<String> highRiskErrorPatterns) {
		this.highRiskErrorPatterns = highRiskErrorPatterns;
	}

	public List<String> getLowRiskErrorPatterns() {
		return lowRiskErrorPatterns;
	}

	public void setLowRiskErrorPatterns(List<String> lowRiskErrorPatterns) {
		this.lowRiskErrorPatterns = lowRiskErrorPatterns;
	}

	List<String> lowRiskErrorPatterns;
	Map<String,List<String>> errorPatterns;
	String notificationType;
	List<String> mailList;
	String signature;
	String dirScanRequired;
	
	public String getDirScanRequired() {
		return dirScanRequired;
	}

	public void setDirScanRequired(String dirScanRequired) {
		this.dirScanRequired = dirScanRequired;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	
	public LogMonitorClient() {
		this.directoryName = null;
		this.fileList = null;
		this.highRiskErrorPatterns = null;
		this.lowRiskErrorPatterns = null;
		this.notificationType = NotificationType.SAVE_TO_DISK;
		this.mailList = null;
		this.signature = null;
		this.dirScanRequired="NO";
	}

	/**
	 * Creates a new Tail instance to follow the specified file
	 */
	public LogMonitorClient(String directoryName, List<String> fileList,
			List<String> highRiskErrorPatterns,List<String> lowRiskErrorPatterns, String notificationType,
			List<String> mailList,String dirScanRequired) {
		this.directoryName = directoryName;
		this.fileList = fileList;
		this.highRiskErrorPatterns = highRiskErrorPatterns;
		this.lowRiskErrorPatterns = lowRiskErrorPatterns;
		this.notificationType = notificationType;
		//default to NotificationType.DOWNLOAD_FILE
		if(notificationType == null || "".equalsIgnoreCase(notificationType)){
			this.notificationType = NotificationType.SAVE_TO_DISK;
		}
		this.mailList = mailList;
		this.dirScanRequired = dirScanRequired;
	}

	/**
	 * A new line has been added to the tailed log file
	 * 
	 * @param line
	 *            The new line that has been added to the tailed log file
	 */
	public String newLogFileLine(String line) {
		System.out.println(line);
		return line;
	}

	@Override
	public void run() {
	
		// TODO Auto-generated method stub  < >
		Properties logMonitorProps = null;
		try {
			logMonitorProps = getLogMonitorProperties();
		} catch (IOException e2) {
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return;
		}
		
		String monitoringProcess = logMonitorProps.getProperty("MONITOR_PROCESS");
		if(monitoringProcess == null){
			monitoringProcess = "DAEMON";
		}
		
		System.out.println("directoryName :"+directoryName +" fileList :"+ fileList +" errorPatterns :"+errorPatterns+"	notificationType :"+notificationType+" mailList :"+mailList);
		if(monitoringProcess.equalsIgnoreCase("GUI") && (this.directoryName == null || 
				((this.fileList == null ||(this.fileList != null && this.fileList.size() <=0)) && !"YES".equalsIgnoreCase(this.dirScanRequired)) ||  
				("EMAIL".equals(this.notificationType) && (this.mailList == null||(this.mailList != null && this.mailList.size()<=0))))){
				System.out.println("Please make sure you provide inputs for all mandatory fields");
				return;
		}else if(monitoringProcess.equalsIgnoreCase("DAEMON")){
			this.directoryName = logMonitorProps.getProperty("MONITOR_DIR_NAME");
			if(directoryName == null){
				System.out.println("Please configure MONITOR_DIR_NAME in log_monitor.properties");
				return;
			}
			
			String fileNames = logMonitorProps.getProperty("MONITOR_FILE_LIST");
			if(fileNames != null){
				this.fileList = new ArrayList<String>();
				String[] files = fileNames.split(",");
				for(int fileCnt = 0;fileCnt < files.length;fileCnt++){
					this.fileList.add(directoryName+"\\"+files[fileCnt]);
				}
			}else{
				System.out.println("Please configure MONITOR_FILE_LIST in log_monitor.properties");
				return;
			}
			
			String errPatterns = logMonitorProps.getProperty("MONITOR_HIGH_RISK_PATTERN_LIST");
			if(errPatterns != null){
				this.highRiskErrorPatterns = new ArrayList<String>();
				String[] patterns = errPatterns.split("\\|");
				for(int patternNo = 0;patternNo < patterns.length;patternNo++){
					this.highRiskErrorPatterns.add(patterns[patternNo]);
				}
			}else{
				System.out.println("Please configure MONITOR_HIGH_RISK_PATTERN_LIST in log_monitor.properties");
				return;
			}
			
			errPatterns = logMonitorProps.getProperty("MONITOR_LOW_RISK_PATTERN_LIST");
			if(errPatterns != null){
				this.lowRiskErrorPatterns = new ArrayList<String>();
				String[] patterns = errPatterns.split("\\|");
				for(int patternNo = 0;patternNo < patterns.length;patternNo++){
					this.lowRiskErrorPatterns.add(patterns[patternNo]);
				}
			}else{
				System.out.println("Please configure MONITOR_LOW_RISK_PATTERN_LIST in log_monitor.properties");
				return;
			}
									
			String notifyType = logMonitorProps.getProperty("MONITOR_NOTIFICATION_TYPE");
			if(notifyType != null){
				this.notificationType = notifyType;
			}else{
				this.notificationType = NotificationType.SAVE_TO_DISK;
			}
			
			String mails = logMonitorProps.getProperty("MONITOR_MAIL_LIST");
			if(mails != null){
				this.mailList = new ArrayList<String>();
				String[] mailString = mails.split(",");
				for(int mailIdNo = 0;mailIdNo < mailString.length;mailIdNo++){
					this.mailList.add(mailString[mailIdNo]);
				}
			}else{
				if(this.notificationType == NotificationType.EMAIL){
					System.out.println("Please configure MONITOR_MAIL_LIST in log_monitor.properties");
					return;
				}
			}
			
			String dirScanReq = logMonitorProps.getProperty("MONITOR_DIRECTORY_SCAN_REQUIRED");
			if(dirScanReq != null){
				this.dirScanRequired = dirScanReq;
			}else{
				this.dirScanRequired = "NO";
			}
		}
		
		//GUI or BACKEND will not send these .so read them from property file
		String signatureValue = logMonitorProps.getProperty("MONITOR_MAIL_SIGNATURE");
		if(signatureValue != null){
			this.signature = signatureValue;
		}else{
			this.signature = "LOG MONITOR TEAM";
		}
		
		String patternTraceFile = logMonitorProps.getProperty("MONITOR_PATTERN_TRACE_FILE");
		if(patternTraceFile == null){
			patternTraceFile = ".\\hackathon_trace.dat";
		}
		System.out.println("patternTraceFile :"+patternTraceFile);
		
		String readLogFromBeingingOnServerStartup = logMonitorProps.getProperty("MONITOR_LOGFILE_FROM_BEGIN_ON_STARTUP");
		if(readLogFromBeingingOnServerStartup == null){
			readLogFromBeingingOnServerStartup = "NO";
		}
		System.out.println("readLogFromBeingingOnServerStartup :"+readLogFromBeingingOnServerStartup);
		
		boolean readFromBeginOnStartup = "YES".equalsIgnoreCase(readLogFromBeingingOnServerStartup)? true:false;
		
		

		if("YES".equalsIgnoreCase(dirScanRequired) || "TRUE".equalsIgnoreCase(dirScanRequired)){
			File f = null;
			String[] paths;
			try {
				// create new file
				f = new File(directoryName);
				if (f.isDirectory()) {
					// array of files and directory
					paths = f.list();
					this.fileList = new ArrayList<String>();
					// for each name in the path array
					for (String path : paths) {
						// prints filename and directory name
						System.out.println(directoryName+"\\"+path);
						if(path != null && path.toUpperCase().indexOf(".LOG") >= 0)
							this.fileList.add(directoryName+"\\"+path);
					}
				}
			} catch (Exception e) {
				// if any error occurs
				e.printStackTrace();
				System.out.println("Could not find directory - "+directoryName);
				return;
			}
		}
		ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
		for (String fileName : this.fileList) {
			try {
				System.out.println("Tailing the log :"+fileName);
				//fork thread for each file
				//Runnable worker = new GenericLogFileTailer(new File(fileName),100, readFromBeginOnStartup, highRiskErrorPatterns,lowRiskErrorPatterns, notificationType,signature,mailList,patternTraceFile);
				Runnable worker = new GenericLogFileTailer(new File(fileName),1000, readFromBeginOnStartup, this.highRiskErrorPatterns,this.lowRiskErrorPatterns, this.notificationType,this.signature,this.mailList,patternTraceFile);
				((GenericLogFileTailer) worker).addLogFileTailerListener(this);
				executor.execute(worker);
			} catch (Exception e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			}
		}
		executor.shutdown();

		// Wait until all threads are finish
		while (!executor.isTerminated()) {

		}
		System.out.println("\nFinished all threads");

	}
	
	private Properties getLogMonitorProperties() throws Exception {
		InputStream is = null;
		Properties prop = null;
		try {
			prop = new Properties();
			is = this.getClass().getResourceAsStream("/log_monitor.properties");
			prop.load(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			throw e;
		}
		return prop;
	}
	
	public byte[] downloadTraceFile() throws Exception{
		byte[] data = null;
		Properties logMonitorProps = null;
		try {
			logMonitorProps = getLogMonitorProperties();
		} catch (IOException e2) {
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return null;
		}
		
		
		String patternTraceFile = logMonitorProps.getProperty("MONITOR_PATTERN_TRACE_FILE");
		if(patternTraceFile == null){
			patternTraceFile = ".\\hackathon_trace.dat";
		}
		
		SimpleDateFormat DDMMYYYY = new SimpleDateFormat("dd_MM_yyyy");
		patternTraceFile += "."+DDMMYYYY.format(Calendar.getInstance().getTime());
		System.out.println("patternTraceFile :"+patternTraceFile);
		
		String tmpSourceTraceFilePath = patternTraceFile+".copy";
		try {
			createTempTraceFile(patternTraceFile,tmpSourceTraceFilePath);
			System.out.println("Copied file "+patternTraceFile+" to "+tmpSourceTraceFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("No trace file found at path - "+patternTraceFile);
		}
		
		try {
			data = convertToByteArray(tmpSourceTraceFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}finally{
			if(deleteFile(tmpSourceTraceFilePath)){
				System.out.println("File "+tmpSourceTraceFilePath+" deleted successfully");
			}
		}
		
		return data;
	}
	
	public static void createTempTraceFile(String sourceTraceFilePath,String destinationFilePath) throws IOException {
		
	    Path FROM = Paths.get(sourceTraceFilePath);
	    Path TO = Paths.get(destinationFilePath);
	    //overwrite existing file, if exists
	    CopyOption[] options = new CopyOption[]{
	      StandardCopyOption.REPLACE_EXISTING,
	      StandardCopyOption.COPY_ATTRIBUTES
	    }; 
	    Files.copy(FROM, TO, options);
	 }

	public static byte[] convertToByteArray(String destinationFilePath) throws Exception{
		FileInputStream fileInputStream=null;
		 
        File file = new File(destinationFilePath);
 
        byte[] bFile = new byte[(int) file.length()];
 
        try {
            //convert file into array of bytes
	    fileInputStream = new FileInputStream(destinationFilePath);
	    fileInputStream.read(bFile);
	   
 

        }catch(Exception e){
        	throw new Exception("Could not download file-"+destinationFilePath);
        }finally{
        	if(fileInputStream != null){
        	 fileInputStream.close();
        	 fileInputStream = null;
        	}
        	
        	if(file != null){
        		file = null;
        	}
        }
        return bFile;

	}
	
	public static boolean deleteFile(String filePath) {
		File f = null;
		boolean fileDeleted = false;
		try {
			// create new files
			f = new File(filePath);

			if (f.exists()) {
				// prints
				System.out.println("File exists");

				// delete() invoked
				f.delete();
				System.out.println("delete() invoked");
				
				fileDeleted = true;

			}
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}finally{
			
		}
		return fileDeleted;
	}
	
	/**
	 * 
	 * Generate Alert Metrics
	 */
	
	public int getHighRiskAlertsCount() {
		if(!metricsGenerated){
			try {
				generateCurrentTraceMetrics();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return highRiskAlertsCount;
	}

	public void setHighRiskAlertsCount(int highRiskAlertsCount) {
		this.highRiskAlertsCount = highRiskAlertsCount;
	}

	public int getLowRiskAlertsCount() {
		if(!metricsGenerated){
			try {
				generateCurrentTraceMetrics();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return lowRiskAlertsCount;
	}

	public void setLowRiskAlertsCount(int lowRiskAlertsCount) {
		this.lowRiskAlertsCount = lowRiskAlertsCount;
	}

	int highRiskAlertsCount = 0;
	int lowRiskAlertsCount = 0;
	
	boolean metricsGenerated = false;
	private void generateCurrentTraceMetrics() throws Exception{
		
		Properties logMonitorProps = null;
		try {
			logMonitorProps = getLogMonitorProperties();
		} catch (IOException e2) {
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return ;
		}
		
		
		String patternTraceFile = logMonitorProps.getProperty("MONITOR_PATTERN_TRACE_FILE");
		if(patternTraceFile == null){
			patternTraceFile = ".\\hackathon_trace.dat";
		}
		
		SimpleDateFormat DDMMYYYY = new SimpleDateFormat("dd_MM_yyyy");
		patternTraceFile += "."+DDMMYYYY.format(Calendar.getInstance().getTime());
		System.out.println("patternTraceFile :"+patternTraceFile);
		
		String tmpSourceTraceFilePath = patternTraceFile+".copy";
		try {
			createTempTraceFile(patternTraceFile,tmpSourceTraceFilePath);
			System.out.println("Copied file "+patternTraceFile+" to "+tmpSourceTraceFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("No trace file found at path - "+patternTraceFile);
		}
		
		try {
			countPatterns(tmpSourceTraceFilePath);
			metricsGenerated = true;
		}finally{
			
			if(deleteFile(tmpSourceTraceFilePath)){
				System.out.println("File "+tmpSourceTraceFilePath+" deleted successfully");
			}
		}
	}
	
	private void countPatterns(String tmpSourceTraceFilePath){
		
		Properties logMonitorProps = null;
		try {
			logMonitorProps = getLogMonitorProperties();
		} catch (IOException e2) {
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return ;
		}
		
		
		String directoryName = logMonitorProps.getProperty("MONITOR_DIR_NAME");
		if(directoryName == null){
			directoryName = ".";
		}
		
		String highRiskPatternsFile = directoryName+"/highRiskPatterns.dat";
		String lowRiskPatternsFile = directoryName+"/lowRiskPatterns.dat";
		
		highRiskAlertsCount = 0;
		lowRiskAlertsCount = 0;
		//reading file line by line in Java using BufferedReader       
        FileInputStream fis = null;
        BufferedReader reader = null;
      
        try {
            fis = new FileInputStream(tmpSourceTraceFilePath);
            reader = new BufferedReader(new InputStreamReader(fis));
          
            System.out.println("Started Reading "+tmpSourceTraceFilePath);
          
            String line = reader.readLine();
            while(line != null){
                System.out.println(line);
                
                if(line.indexOf("HIGH_RISK") >= 0){
                	highRiskAlertsCount ++;
                	appendContents(highRiskPatternsFile,line+"\n\n");
                }else if(line.indexOf("LOW_RISK") >= 0){
                	lowRiskAlertsCount ++;
                	appendContents(lowRiskPatternsFile,line+"\n\n");
                }
                line = reader.readLine();
            }           
        } catch (FileNotFoundException ex) {
        	ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
          
        } finally {
            try {
                reader.close();
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

	}
	
	public static synchronized void appendContents(String traceFile, String content) {
        try {
        	System.out.println("Content :"+content);
            File oFile = new File(traceFile);
            if (!oFile.exists()) {
                oFile.createNewFile();
            }
            if (oFile.canWrite()) {
                BufferedWriter oWriter = new BufferedWriter(new FileWriter(traceFile, true));
                oWriter.write (content);
                oWriter.close();
            }
        }
        catch (IOException oException) {
            throw new IllegalArgumentException("Error appending/File cannot be written: \n" + traceFile);
        }
    }
	
	
	public byte[] downloadHighRiskTraceFile() throws Exception{
		byte[] data = null;
		Properties logMonitorProps = null;
		try {
			logMonitorProps = getLogMonitorProperties();
		} catch (IOException e2) {
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return null;
		}
		
		
		String directoryName = logMonitorProps.getProperty("MONITOR_DIR_NAME");
		if(directoryName == null){
			directoryName = ".";
		}
		
		String highRiskPatternsFile = directoryName+"/highRiskPatterns.dat";
				
		try {
			data = convertToByteArray(highRiskPatternsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}finally{
			
		}
		
		return data;
	}
	
	public byte[] downloadLowRiskTraceFile() throws Exception{
		byte[] data = null;
		Properties logMonitorProps = null;
		try {
			logMonitorProps = getLogMonitorProperties();
		} catch (IOException e2) {
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Make sure log_monitor.properties is in CLASSPATH");
			return null;
		}
		
		
		String directoryName = logMonitorProps.getProperty("MONITOR_DIR_NAME");
		if(directoryName == null){
			directoryName = ".";
		}
		
		String lowRiskPatternsFile = directoryName+"/lowRiskPatterns.dat";
				
		try {
			data = convertToByteArray(lowRiskPatternsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}finally{
			
		}
		
		return data;
	}
	/**
	 * Command-line launcher
	 */
	public static void main(String[] args) {
		List<String> fileList = new ArrayList<String>();
		fileList.add("C:\\Siva\\DemoLogs\\test1.log");
		fileList.add("C:\\Siva\\DemoLogs\\test2.log");

		List<String> errorPatterns = new ArrayList<String>();
		errorPatterns.add("Test exception in file1");
		errorPatterns.add("Test exception in file2");
		
		String notificationType = NotificationType.EMAIL;

		String directoryName = "C:\\Siva\\DemoLogs\\";
		
		List<String> mailList = new ArrayList<String>();
		mailList.add("siva.kumar.x.adabala@one.verizon.com");
		
		String dirScanRequired = "NO";
		
		System.out.println("Start Monitoring the logs");
		//Runnable thread = new LogMonitorClient(directoryName, fileList,errorPatterns,errorPatterns,notificationType,mailList,dirScanRequired);
		Runnable thread = new LogMonitorClient();
		thread.run();
		System.out.println("Fork a thread to monitor the logs and come out");
		
		/*ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);

		for (String fileName : fileList) {
			try {
				Runnable worker = new GenericLogFileTailer(new File(fileName), 100, false, errorPatterns,notificationType);
				((GenericLogFileTailer) worker).addLogFileTailerListener(new LogMonitorClient());
				executor.execute(worker);
			} catch (Exception e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			}
		}
		executor.shutdown();

		// Wait until all threads are finish
		while (!executor.isTerminated()) {

		}
		System.out.println("\nFinished all threads");*/
	}

}