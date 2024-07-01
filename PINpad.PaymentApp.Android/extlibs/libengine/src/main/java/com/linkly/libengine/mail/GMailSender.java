package com.linkly.libengine.mail;

import android.graphics.Bitmap;

import com.linkly.libengine.dependencies.IDependency;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import timber.log.Timber;

public class GMailSender extends javax.mail.Authenticator {
    private String mailhost;// = "smtp.gmail.com";
    private String port;// = "465";
    private String user;// = "philip@eft-solutions.co.uk";
    private String password;// = "";
    private String sender;// = "philip@eft-solutions.co.uk";
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(IDependency d) {

        if (d != null) {
            user = d.getPayCfg().getMailUser();
            password = d.getPayCfg().getMailPassword();
            sender = d.getPayCfg().getMailSender();
            port = d.getPayCfg().getMailPort();
            mailhost = d.getPayCfg().getMailHost();
        }

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized boolean addAttachment(Multipart multipart, Bitmap bitmap ){

        try {

            if (bitmap == null || multipart == null)
                return false;
            ByteArrayOutputStream imageByteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageByteArrayOutputStream);
            byte[] imageByteArray = imageByteArrayOutputStream.toByteArray();

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            //DataSource source = new FileDataSource(attachementPath); // ex : "C:\\test.pdf"
            DataSource merchantSource = new ByteArrayDataSource(imageByteArray, "image/png");
            attachmentBodyPart.setDataHandler(new DataHandler(merchantSource));
            attachmentBodyPart.setFileName("receipt.png"); // ex : "test.pdf"


            multipart.addBodyPart(attachmentBodyPart); // add the attachement part
        } catch ( Exception e) {
            Timber.w(e);
        }
        return true;
    }


    public synchronized boolean sendMail(Bitmap merchantReceipt, Bitmap cardholderReceipt, String subject, String recipients, String bodyText) throws Exception {

        try{
            MimeMessage message = new MimeMessage(session);
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

            Multipart multipart = new MimeMultipart();

            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(bodyText);

            addAttachment(multipart, merchantReceipt);
            addAttachment(multipart, cardholderReceipt);

            multipart.addBodyPart(textBodyPart);  // add the text part

            message.setContent(multipart);

            Transport.send(message);
            return true;

        } catch ( AddressException a) {
            return true;
        } catch(Exception e){
            Timber.w(e);
        }
        return false;
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}