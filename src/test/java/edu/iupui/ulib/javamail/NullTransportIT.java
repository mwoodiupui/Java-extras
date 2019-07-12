/*
 * Copyright 2019 Indiana University.  All rights reserved.
 *
 * Mark H. Wood, IUPUI University Library, Jul 11, 2019
 */

package edu.iupui.ulib.javamail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

/**
 *
 * @author Mark H. Wood <mwood@iupui.edu>
 */
public class NullTransportIT {
    private static final String PROTOCOL = "null";

    // The last message that was "sent".
    private Message sentMessage;

    @Test
    public void testTransport()
            throws UnsupportedEncodingException, MessagingException, IOException {
        // Create a recipient address.
        Address fromAddress = new InternetAddress("sender@example.com",
                "Jane Sender");
        Address toAddress = new InternetAddress("recipient@example.com",
                "John Q. Recipient");

        // Configure a transport spy to capture messages being sent.  See MyTransportListener.
        NullTransport.addATransportListener(new MyTransportListener());

        // Create an appropriately configured Session.
        Properties props = new Properties();
        props.setProperty("mail.host", "testing.example.com");

        Session session = Session.getInstance(props);
        session.setProtocolForAddress(toAddress.getType(), PROTOCOL);
        Provider nullProvider = new Provider(Provider.Type.TRANSPORT,
                PROTOCOL,
                NullTransport.class.getCanonicalName(),
                "Indiana University",
                "0");
        session.setProvider(nullProvider);

        // Compose a message.
        Message message = new MimeMessage(session);
        message.setFrom(fromAddress);
        message.addRecipient(Message.RecipientType.TO, toAddress);
        message.setSentDate(new Date());
        message.setSubject("Testing");
        message.setText("This is a text.");
        message.saveChanges();

        // "Send" the message.
        Transport.send(message);
        //NullTransport transport = (NullTransport) session.getTransport(PROTOCOL);
        //transport.sendMessage(message, null);

        // Check the captured sent message.
        ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
        sentMessage.writeTo(messageStream);
        System.out.println();
        System.out.println("vvvvvvvvvv  Formatted message  vvvvvvvvvv");
        System.out.println(messageStream.toString(StandardCharsets.UTF_8.name()));
        System.out.println("^^^^^^^^^^  Formatted message  ^^^^^^^^^^");

        // Check the addresses.
        System.out.println();
        System.out.println("Recipients:");
        for (Address address : sentMessage.getAllRecipients()) {
            System.out.format("  %s:  %s%n", address.getType(), address.toString());
        }

        System.out.println();
    }

    private class MyTransportListener
            implements TransportListener {
        @Override
        public void messageDelivered(TransportEvent e) {
            sentMessage = e.getMessage();
        }

        @Override
        public void messageNotDelivered(TransportEvent e) {
            sentMessage = e.getMessage();
        }

        @Override
        public void messagePartiallyDelivered(TransportEvent e) {
            sentMessage = e.getMessage();
        }
    }
}
