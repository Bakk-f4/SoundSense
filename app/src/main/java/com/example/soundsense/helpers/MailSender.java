package com.example.soundsense.helpers;

import android.util.Log;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MailSender{
    private final String TAG = "MailSender";
    String stringSenderEmail;
    String stringReceiverEmail;
    String stringPasswordSenderEmail;
    String stringHost;
    Properties properties;




    public void sendMail(String address, String message){
        try {
            stringSenderEmail = "gruppo.5.webd@gmail.com";
            stringReceiverEmail = address;
            stringPasswordSenderEmail = "federicoionirynacamilla";

            stringHost = "smtp.gmail.com";

            properties = System.getProperties();

            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    Log.i(TAG,"Autenticazione");
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            //MimeMessage mimeMessage = new MimeMessage(session);
            //mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

            //mimeMessage.setSubject("Subject: SoundSense notification");
            //mimeMessage.setText(message);
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(stringSenderEmail));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(stringReceiverEmail));
            mimeMessage.setSubject("SoundSense notification");
            mimeMessage.setText(message);


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        Log.i(TAG,"MessagingException");
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            Log.i(TAG,"start completato");

        } catch (AddressException e) {
            e.printStackTrace();
            Log.i(TAG,"AddressException");
        } catch (MessagingException e) {
            e.printStackTrace();
            Log.i(TAG,"MessagingException");
        }

    }


}


