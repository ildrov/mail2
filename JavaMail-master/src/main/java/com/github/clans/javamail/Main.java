package com.github.clans.javamail;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.FlagTerm;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.io.Serializable;

public class Main {
    static final String ENCODING = "UTF-8";

    public static void main(String args[]) throws MessagingException, IOException {
        String subject = "Subject";
        String content = "Test";
        String smtpHost="smtp.rambler.ru";
        String address="testtatar@rambler.ru";
        String login="testtatar";
        String password="Tatartest1337!";
        String smtpPort="25";
        String pop3Host="pop3.rambler.ru";
        receiveMessage(login, password, pop3Host);
    }

    public static void receiveMessage(String user, String password, String host) throws MessagingException, IOException {
        Authenticator auth = new MyAuthenticator(user, password);

        Properties props = System.getProperties();
        props.put("mail.user", user);
        props.put("mail.host", host);
        props.put("mail.debug", "false");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.transport.protocol", "smtp");

        Session session = Session.getDefaultInstance(props, auth);
        Store store = session.getStore();
        store.connect();
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);


        System.out.println("Количество писем = "+ inbox.getMessageCount());

        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        ArrayList<String> attachments = new ArrayList<String>();

        LinkedList<MessageBean> listMessages = getPart(messages, attachments);

        inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);

        inbox.close(false);
        store.close();
    }

    public static LinkedList<MessageBean> getPart(Message[] messages, ArrayList<String> attachments) throws MessagingException, IOException {
        LinkedList<MessageBean> listMessages = new LinkedList<MessageBean>();
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        for (Message inMessage : messages) {
            attachments.clear();
            if (inMessage.isMimeType("text/plain")) {
                MessageBean message = new MessageBean(inMessage.getMessageNumber(), MimeUtility.decodeText(inMessage.getSubject()), inMessage.getFrom()[0].toString(), null, f.format(inMessage.getSentDate()), (String) inMessage.getContent(), false, null);
                listMessages.add(message);


            } else if (inMessage.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) inMessage.getContent();
                MessageBean message = null;
                for (int i = 0; i < mp.getCount(); i++) {
                    Part part = mp.getBodyPart(i);
                    if ((part.getFileName() == null || part.getFileName() == "") && part.isMimeType("text/plain")) {
                        message = new MessageBean(inMessage.getMessageNumber(), inMessage.getSubject(), inMessage.getFrom()[0].toString(), null, f.format(inMessage.getSentDate()), (String) part.getContent(), false, null);
                    } else if (part.getFileName() != null || part.getFileName() != ""){
                        if ((part.getDisposition() != null) && (part.getDisposition().equals(Part.ATTACHMENT))) {
                            attachments.add(saveFile(MimeUtility.decodeText(part.getFileName()), part.getInputStream()));
                            if (message != null) message.setAttachments(attachments);
                        }
                    }
                }

                listMessages.add(message);
            }
        }

        return listMessages;

    }
    /* private static String saveMes(String cont, InputStream input) {
        String put = "C:\\Сообщения\\чтото.txt"+cont;
        try {
            byte[] content = new byte[input.available()];
            input.read(content);
            File file = new File(put);
            FileOutputStream out = new FileOutputStream(file);
            out.write(content);
            input.close();
            out.close();
            return put;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return put;
     }*/

    private static String saveFile(String filename, InputStream input) {
        String path = "C:\\Файлы\\"+filename;
        try {
            byte[] attachment = new byte[input.available()];
            input.read(attachment);
            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);
            out.write(attachment);
            input.close();
            out.close();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }
}

class MyAuthenticator extends Authenticator {
    private String user;
    private String password;

    MyAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        String user = this.user;
        String password = this.password;
        return new PasswordAuthentication(user, password);
    }
}






class MessageBean implements Serializable {
    private String subject;
    private String from;
    private String to;
    private String dateSent;
    private String content;
    private boolean isNew;
    private int msgId;
    private ArrayList<String> attachments;

    public MessageBean(int msgId, String subject, String from, String to, String dateSent, String content, boolean isNew, ArrayList<String> attachments) {

        this.subject = subject;
        this.from = from;
        this.to = to;
        this.dateSent = dateSent;
        this.content = content;
        this.isNew = isNew;
        this.msgId = msgId;
        this.attachments = attachments;
        /*System.out.println("");
        System.out.println("Айди письма = " + msgId);
        System.out.println("Дата отправления письма = " + dateSent);
        System.out.println("Отправитель = " + from);

         */

        try(FileWriter writer = new FileWriter("C:\\Сообщения\\чтото.txt",true)) {
            String text = "\nАйди письма: "+msgId;
            String dat = "\nДата: "+dateSent;
            String send = "\nОтправитель: "+from;
            writer.write(text);
            writer.write(dat);
            writer.write(send);

        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
        }



    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDateSent() {
        return dateSent;
    }

    public void setDateSent(String dateSent) {
        this.dateSent = dateSent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<String> attachments) {
        this.attachments = new ArrayList<String>(attachments);
    }
}