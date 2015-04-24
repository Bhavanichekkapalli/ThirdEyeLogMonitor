package demo.hackathon;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSMTP {
	

    private static String USER_NAME = "activationTeam@hackathon.vz.com";  
    private static String PASSWORD = "********"; 

        public static void main(String[] args) throws Exception {
        
        String from = USER_NAME;
        String pass = PASSWORD;
        ArrayList<String> mailIds = new ArrayList<String>();
       
        if (args != null && args.length != 4) {
        	System.out.println("Invalid No Of Arguments , Please provide <mailId> <logFile> <pattern> <signature> " );
        	System.exit(-1);
        }
        
        
        if ( args[0]!= null && args[0].indexOf(";") > 0 ){
        String [] mailID = args[0].split(";");
        for (int i = 0; i < mailID.length; i++) {
        	mailIds.add(mailID[i]);
        }
        }
        else {
        mailIds.add(args[0]);     	
        }
  
        
        String logFileLoc = args[1];
        String pattern = args[2];
        String signature = args[3];
        
        String hostname= null;
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        hostname = addr.getHostName();
        
        String subject = "Alert :: Found Error Pattern " + pattern + " in  the file Name " + logFileLoc;
        String body = "Hi,\n"
        		+ "Please review the file  " + logFileLoc + "  on Host " + hostname + "  , Traces of " + pattern + " were identified on the file " + "\n\nThanks & Regards \n"
        				+ "\n" + signature ; 
        
        
        
        from = "activationTeam@" + hostname + ".vz.com";
      
        MailSMTP mailSMTP = new MailSMTP();
        mailSMTP.sendMail(from, mailIds, subject, body);
    }

    /**
     * @param from
     * @param pass
     * @param to
     * @param subject
     * @param body
     */
    public void sendMail(String from, List<String> mailIds, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.verizon.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.trust", "smtpserver");
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", "dummy");
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", "25");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.**ssl.enable", "true");
        props.setProperty("mail.smtp.**ssl.required", "true");

     
        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[mailIds.size()];

            // To get the array of addresses
            
            
            for( int i = 0; i < mailIds.size(); i++ ) {
            	System.out.println("Value is " + mailIds.get(i) );
            	                toAddress[i] = new InternetAddress(mailIds.get(i));
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipients(Message.RecipientType.TO, mailIds.get(i));
            }
            

            message.setSubject(subject);
            message.setText(body);
           
           /* 
            * Below Code is for Sending Attachment
            * 
            * DataSource source = new FileDataSource(logFileLoc);
            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(logFileLoc);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            
            */
            
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, "dummy");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}