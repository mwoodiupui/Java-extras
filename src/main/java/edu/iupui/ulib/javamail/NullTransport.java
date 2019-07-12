/*
 * Copyright 2019 Indiana University.  All rights reserved.
 *
 * Mark H. Wood, IUPUI University Library, Jul 11, 2019
 */

package edu.iupui.ulib.javamail;

import java.util.ArrayList;
import java.util.List;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

/**
 * A JavaMail {@link Transport} that doesn't go anywhere.  Perhaps useful for
 * testing.  This transport does not write any files or make any network
 * connections.
 *
 * <p>
 * To use it for inspecting the "sent" message, you'll need to be using a
 * specific instance of this class, probably obtained from
 * Session.getTransport(protocol), and that instance's sendMessage()
 * method.  The static Transport.send() creates a fresh instance that you cannot
 * manipulate.
 *
 * <p>
 * {@link getMessage()} will return the last message that this instance "sent".
 *
 * @author Mark H. Wood <mwood@iupui.edu>
 */
public class NullTransport
        extends Transport {
    /** Listeners to be added to each instance's ConnectionListener list. */
    private static final List<ConnectionListener> connectionListeners = new ArrayList<>();

    /** Listeners to be added to each instance's TransportListener list. */
    private static final List<TransportListener> transportListeners = new ArrayList<>();

    /** The message passed to the last call on {@link sendMessage} */
    private Message message;

    /** The address list passed to the last call on {@link sendMessage} */
    private Address[] addresses;

    private String host;

    private int port;

    private String user;

    private String password;

    /**
     * Initialize a transport that simply swallows messages.
     *
     * @param session
     * @param urlname URL to the service endpoint.  Not used.
     */
    public NullTransport(Session session, URLName urlname) {
        super(session, urlname);
        for (ConnectionListener l : connectionListeners) {
            super.addConnectionListener(l);
        }
        for (TransportListener l : transportListeners) {
            super.addTransportListener(l);
        }
    }

    /**
     * Add a listener to the list of ConnectionListener which will be set on
     * each new instance.
     *
     * @param l the new ConnectionListener.
     */
    static public void addAConnectionListener(ConnectionListener l) {
        connectionListeners.add(l);
    }

    /**
     * Add a listener to the list of TransportListener which will be set on
     * each new instance.
     *
     * @param l the new TransportListener.
     */
    static public void addATransportListener(TransportListener l) {
        transportListeners.add(l);
    }

    @Override
    public boolean protocolConnect(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        setConnected(true);
        return true; // No credentials needed.
    }

    @Override
    public void sendMessage(Message message, Address[] addresses)
            throws MessagingException {
        message.addRecipients(Message.RecipientType.TO, addresses);
        this.message = message;
        this.addresses = addresses;
        this.notifyTransportListeners(TransportEvent.MESSAGE_DELIVERED,
                message.getAllRecipients(), new Address[0], new Address[0],
                message);
    }

    /**
     * @return the Message passed to the last call on
     * {@link sendMessage(Message, Address[])}.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @return the address list passed to the last call on
     * {@link sendMessage(Message, Address[])};
     */
    public Address[] getAddresses() {
        return addresses;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
