import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloTest{
	
	public static void main(String[] args){
		
		String hello = "This is \"|\" the error pattern";
		
		String[] patterns = hello.split("\"|\"");
		for(int i=0;i<patterns.length;i++){
			System.out.println("Pattern :"+patterns[i]);
		}
		
		String line = "Hello this is test log.Test exception in file1";
		
		List<String> errorPatterns = new ArrayList<String>();
		errorPatterns.add("Test exception in file1");
		errorPatterns.add("Test exception in file2");
		errorPatterns.add("Hello");
		
		HelloTest helloTest = new HelloTest();
		boolean isPatternMatched = helloTest.isPatternMatching(line,errorPatterns);
		
		System.out.println("Pattern Matched :"+isPatternMatched);
		
		
		String directoryName = "C:\\Siva\\DemoLogs\\";
		File f = null;
		String[] paths;

		try {
			// create new file
			f = new File(directoryName);

			if (f.isDirectory()) {
				// array of files and directory
				paths = f.list();

				// for each name in the path array
				for (String path : paths) {
					// prints filename and directory name
					System.out.println(directoryName+"\\"+path);
				}
			}
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}

		SimpleDateFormat DDMMYYYY = new SimpleDateFormat("dd_MM_yyyy");
		System.out.println("Date :"+DDMMYYYY.format(Calendar.getInstance().getTime()));
		
		try {
			createTempTraceFile("C:\\Siva\\DemoLogs\\hackathon_trace.dat","C:\\Siva\\DemoLogs\\hackathon_trace.dat.04242015");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	boolean isPatternMatching(String line, List<String> patterns) {
		  boolean patternMatched = false;
		  
		  
		  if(patterns.size() <= 0){
			  return false;
		  }
		  StringBuilder builder = new StringBuilder("");
		  for(String pattern : patterns){
			  builder.append(pattern).append("|");
		  }
		  String patternsString = "";
		  if(builder.toString().indexOf("|") >= 0){
			  patternsString = builder.toString().substring(0, builder.toString().lastIndexOf("|"));
		  }
			
		  System.out.println("Pattern String :"+patternsString);
			Pattern p = Pattern.compile("("+patternsString+")");
			Matcher m = p.matcher(line);

			//List<String> animals = new ArrayList<String>();
			while (m.find()) {
				System.out.println("Found a " + m.group() + ".");
				patternMatched = true;
				//animals.add(m.group());
			}
			
			return patternMatched;
		}
	
}