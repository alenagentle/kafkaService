package com.irlix.Server.services;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class MyMileService {

    public void sendSimpleEmail(String toAddress, String subject, String message){
        System.out.println("we are in void sendSimpleEmail()");

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com" );
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.port", "" + 587);
        props.setProperty("mail.smtp.starttls.enable", "true");

        Session  session = Session.getInstance(props, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
        //использовала свою учетную запись gmail.com, сгенерировала пароль для двухфакторной аутентификации и подставила в параметр объекта PasswordAuthentication
                return new PasswordAuthentication("почта","пароль");
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("почта"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);
            System.out.println("Email Sent successfully....");

        }
        catch (MessagingException mex){
            mex.printStackTrace();
        }
    }
}
