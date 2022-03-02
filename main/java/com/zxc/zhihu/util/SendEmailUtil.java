package com.zxc.zhihu.util;

import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Component
public class SendEmailUtil {
    private static String fromEmailAddress = "13032958183@163.com";
    private static String fromEmailPassword = "MCEPZONTAWDQLOXX";
    private static String hostAddress = "smtp.163.com";

    public static void sendMail(String destMailAddress, String title, String message) {

        String to = destMailAddress;


        String from = fromEmailAddress;

        String host = "smtp.163.com";


        Properties properties = System.getProperties();


        properties.setProperty("mail.smtp.host", host);

        properties.put("mail.smtp.auth", "true");
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmailAddress, fromEmailPassword);
            }
        });

        try {
            MimeMessage message1 = new MimeMessage(session);

            message1.setFrom(new InternetAddress(from));

            message1.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            message1.setSubject(title);

            message1.setText(message);

            Transport.send(message1);
            System.out.println("Sent message successfully...");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

	/*public static void main(String[] args) {
		SendEmailUtil.sendMail("13032958183@163.com","知乎账号安全提醒","知乎网管理员提醒：尊敬的知友"+"zxc"+"您的账号异常登录，如非本人操作，请尽快修改密码");
	}*/
}
