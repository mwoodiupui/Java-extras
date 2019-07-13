/**
 * The MIT License
 * Copyright © 2019 Indiana University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.iupui.ulib.javamail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

    /**
     * The last message that was "sent".
     * <p>
     * Why is this a {@link CompletableFuture}?  Ask the person who thought it
     * was reasonable to create a separate thread to call TransportListeners.
     * Without some sort of synchronization, the listener doesn't fire until
     * after the test has crashed on an NPE.  Unless you are stepping it in a
     * debugger.  What fun!
     */
    private final CompletableFuture<Message> futureSentMessage
            = new CompletableFuture<>();

    @Test
    public void testTransport()
            throws UnsupportedEncodingException, MessagingException,
                   IOException, InterruptedException, ExecutionException {
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
        Message sentMessage = futureSentMessage.get();
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

    /**
     * This seems to be the only way to get a copy of the completed Message
     * without potentially rewriting the code under test.  Consider borrowing
     * this technique for your own tests.
     */
    private class MyTransportListener
            implements TransportListener {
        @Override
        public void messageDelivered(TransportEvent e) {
            futureSentMessage.complete(e.getMessage());
        }

        @Override
        public void messageNotDelivered(TransportEvent e) {
            futureSentMessage.complete(e.getMessage());
        }

        @Override
        public void messagePartiallyDelivered(TransportEvent e) {
            futureSentMessage.complete(e.getMessage());
        }
    }
}
