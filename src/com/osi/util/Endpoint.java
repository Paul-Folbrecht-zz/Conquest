package com.osi.util;


import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Encapsulates a TCP endpoint- host and port.
 *
 * @author Paul Folbrecht
 */
public class Endpoint implements Serializable {
    protected InetAddress _address;
    protected int _port;

    /**
     *
     */
    public Endpoint(InetAddress address, int port) {
        _address = address;
        _port = port;
    }

    /**
     *
     */
    public Endpoint(Socket socket) {
        this(socket.getInetAddress(), socket.getPort());
    }

    /**
     *
     */
    public InetAddress getAddress() {
        return _address;
    }

    /**
     *
     */
    public int getPort() {
        return _port;
    }

    /**
     *
     */
    public boolean equals(Object object) {
        if (object instanceof Endpoint) {
            Endpoint other = (Endpoint) object;
            return (other.getAddress().getHostAddress().equals(getAddress().getHostAddress()) && other.getPort() == getPort());
        }

        return false;
    }

    /**
     *
     */
    public String toString() {
        StringBuffer text = new StringBuffer("Address ");

        text.append(_address.getHostAddress());
        text.append(", Port ");
        text.append(_port);

        return text.toString();
    }
}
