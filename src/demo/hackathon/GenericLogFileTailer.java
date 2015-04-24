package demo.hackathon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* @Authors
 * Siva Adabala
 * Debraj Nath
 * Bhavani Chekkapalli
 */
public class GenericLogFileTailer implements Runnable {
	/**
	 * How frequently to check for file changes; defaults to 5 seconds
	 */
	private long sampleInterval = 2000;

	/**
	 * The log file to tail
	 */
	private File logfile;

	/**
	 * Log Patterns
	 */
	private List<String> errorPatterns = null;

	public List<String> getErrorPatterns() {
		return errorPatterns;
	}

	public void setErrorPatterns(List<String> errorPatterns) {
		this.errorPatterns = errorPatterns;
	}

	/**
	 * Notification Type
	 */
	
	String notificationType = null;
	
	
	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	String signature = null;
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	List<String>  mailIdsList = null;
	

	public List<String> getMailIdsList() {
		return mailIdsList;
	}

	public void setMailIdsList(List<String> mailIdsList) {
		this.mailIdsList = mailIdsList;
	}
	
	String patternTraceFile = null;

	public String getPatternTraceFile() {
		return patternTraceFile;
	}

	public void setPatternTraceFile(String patternTraceFile) {
		this.patternTraceFile = patternTraceFile;
	}

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
	/**
	 * Defines whether the log file tailer should include the entire contents of
	 * the exising log file or tail from the end of the file when the tailer
	 * starts
	 */
	private boolean startAtBeginning = false;

	/**
	 * Is the tailer currently tailing?
	 */
	private boolean tailing = false;

	/**
	 * Set of listeners
	 */
	private Set listeners = new HashSet();

	/**
	 * Creates a new log file tailer that tails an existing file and checks the
	 * file for updates every 5000ms
	 */
	public GenericLogFileTailer(File file) {
		this.logfile = file;
	}

	/**
	 * Creates a new log file tailer
	 * 
	 * @param file
	 *            The file to tail
	 * @param sampleInterval
	 *            How often to check for updates to the log file (default =
	 *            5000ms)
	 * @param startAtBeginning
	 *            Should the tailer simply tail or should it process the entire
	 *            file and continue tailing (true) or simply start tailing from
	 *            the end of the file
	 * @param errorPatterns
	 *            Should be provided by client/user
	 */
	public GenericLogFileTailer(File file, long sampleInterval,
			boolean startAtBeginning, List<String> highRiskErrorPatterns,List<String> lowRiskErrorPatterns,String notificationType,String signature,
			List<String> mailIdsList,String patternTraceFile) {
		this.startAtBeginning = startAtBeginning;
		this.logfile = file;
		this.sampleInterval = sampleInterval;
		//this.errorPatterns = errorPatterns;
		this.highRiskErrorPatterns = highRiskErrorPatterns;
		this.lowRiskErrorPatterns = lowRiskErrorPatterns;
		this.notificationType = notificationType;
		this.signature = signature;
		this.mailIdsList = mailIdsList;
		this.patternTraceFile = patternTraceFile;
	}

	public void addLogFileTailerListener(GenericLogFileTailerListener listener) {
		this.listeners.add(listener);
	}

	public void removeLogFileTailerListener(
			GenericLogFileTailerListener listener) {
		this.listeners.remove(listener);
	}

	protected void fireNewLogFileLine(String line) {
		for (Iterator i = this.listeners.iterator(); i.hasNext();) {
			GenericLogFileTailerListener listener = (GenericLogFileTailerListener) i
					.next();
			listener.newLogFileLine(line);
		}
	}

	public void stopTailing() {
		this.tailing = false;
	}

	public void run() {
		
		SimpleDateFormat DDMMYYYY = new SimpleDateFormat("dd_MM_yyyy");
		patternTraceFile += "."+DDMMYYYY.format(Calendar.getInstance().getTime());
		System.out.println("patternTraceFile :"+patternTraceFile);
		
		// The file pointer keeps track of where we are in the file
		long filePointer = 0;

		// Determine start point
		if (this.startAtBeginning) {
			filePointer = 0;
		} else {
			filePointer = this.logfile.length();
		}

		try {
			// Start tailing
			this.tailing = true;
			RandomAccessFile file = new RandomAccessFile(logfile, "r");
			int traceCount = 0;
			while (this.tailing) {
				try {
					// Compare the length of the file to the file pointer
					long fileLength = this.logfile.length();
					if (fileLength < filePointer) {
						// Log file must have been rotated or deleted;
						// reopen the file and reset the file pointer
						file = new RandomAccessFile(logfile, "r");
						filePointer = 0;
					}

					if (fileLength > filePointer) {
						// There is data to read
						file.seek(filePointer);
						String line = file.readLine();
						while (line != null) {
							this.fireNewLogFileLine(line);

							boolean highRiskErrorPatternsFound = false;
							boolean lowRiskErrorPatternsFound = false;
							
							String subject = "Alert :: Found Error Pattern in  the log file - " + logfile.getAbsolutePath();
							//Check if Pattern matches.If so , notify based on the notify type
							if(isPatternMatching(line, this.highRiskErrorPatterns)){
								highRiskErrorPatternsFound = true;
								line = "HIGH_RISK -"+line+"\n\n";
								subject = "Alert!!! High risk error pattern found in  the log file - " + logfile.getAbsolutePath();
							}else if(isPatternMatching(line, this.lowRiskErrorPatterns)){
								lowRiskErrorPatternsFound = true;
								line = "LOW_RISK -"+line+"\n\n";
								subject = "Alert!!! Low risk error pattern found in  the log file - " + logfile.getAbsolutePath();
							}
							
							if(highRiskErrorPatternsFound || lowRiskErrorPatternsFound){
								//TODO : REMOVE BELOW 4 lines 
								if(traceCount == 5){
									this.tailing = false;
									break;
								}
								traceCount ++;
								
								System.out.println("mailIdsList :"+mailIdsList+" Notification Type:"+notificationType);
								//Code to Notify
								if(NotificationType.EMAIL.equals(notificationType)){
									//Line for Body, File name for Subject, Signature name
									MailSMTP mailSMTP = new MailSMTP();
									InetAddress addr;
							        addr = InetAddress.getLocalHost();
							        String hostname = addr.getHostName();
							        String from = "activationTeam@" + hostname + ".vz.com";
							        
							        String body = "Hi Team,\n\n"
							        		+ "Please review the file  " + logfile.getAbsolutePath() + "  on Host: " + hostname + "  , Traces of error patterns were identified on the file. Please see below line for error\n \n"+line+ "\n\nThanks & Regards \n"
							        				+ "\n" + signature ; 
							        
									mailSMTP.sendMail(from, mailIdsList, subject, body);
								}/*else if(notificationType == NotificationType.SAVE_TO_DISK){
									//Write Content to Disk
									appendContents(patternTraceFile,line);
								}*/else{
									System.out.println("No notification type specified by client");
								}
								appendContents(patternTraceFile,line);
							}
							//System.out.println(logfile.getName() + " -> "	+ line);
							
							//Read next line
							line = file.readLine();
						}
						filePointer = file.getFilePointer();
					}
					// Sleep for the specified interval
					Thread.sleep(this.sampleInterval);
				} catch (Exception e) {
				}
			}

			// Close the file that we are tailing
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean isPatternMatching(String line, List<String> patterns) {
		boolean patternMatched = false;

		if (patterns.size() <= 0) {
			return false;
		}
		StringBuilder builder = new StringBuilder("");
		for (String pattern : patterns) {
			builder.append(pattern).append("|");
		}
		String patternsString = "";
		if (builder.toString().indexOf("|") >= 0) {
			patternsString = builder.toString().substring(0,
					builder.toString().lastIndexOf("|"));
		}

		System.out.println("Pattern String :" + patternsString);
		Pattern p = Pattern.compile("(" + patternsString + ")");
		Matcher m = p.matcher(line);

		// List<String> animals = new ArrayList<String>();
		while (m.find()) {
			System.out.println("Found a " + m.group() + ".");
			patternMatched = true;
			// animals.add(m.group());
		}

		return patternMatched;
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


}