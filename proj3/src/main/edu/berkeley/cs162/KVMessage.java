/**
 * XML Parsing library for the key-value store
 *
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 *
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs162;

import java.io.*;
import java.util.Scanner;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.JAXBException;
import java.net.*;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This is the object that is used to generate messages the XML based messages
 * for communication between clients and servers.
 */

@XmlRootElement(name = "KVMessage")
public class KVMessage {

    /** default noarg constructor required by JAXB */
    public KVMessage() {}

    /** The type of this message. Can be one of either "getreq", "putreq", "delreq", or "resp" */
    private String msgType = null;

    /** The key of this message. Can be up to 256 bytes. */
    private String key = null;

    /** The value of this message. Can be up to 265 kilobytes. */
    private String value = null;

    /** The contents of this message. */
    private String message = null;

    enum MessageType {
        GETREQ {
            public String toString() {
                return "getreq";
            }
        },
        PUTREQ {
            public String toString() {
                return "putreq";
            }
        },
        DELREQ {
            public String toString() {
                return "delreq";
            }
        },
        RESP {
            public String toString() {
                return "resp";
            }
        }
    }

    public final String getKey() {
        return key;
    }

    @XmlElement(name = "Key")
    public final void setKey(String key) {
        this.key = key;
    }

    public final String getValue() {
        return value;
    }

    @XmlElement(name = "Value")
    public final void setValue(String value) {
        this.value = value;
    }

    public final String getMessage() {
        return message;
    }

    @XmlElement(name = "Message")
    public final void setMessage(String message) {
        this.message = message;
    }

    @XmlAttribute(name = "type")
    public void setMsgType(String type) {
        this.msgType = type;
    }

    public final String getMsgType() {
        return this.msgType;
    }

    /* Solution from http://weblogs.java.net/blog/kohsuke/archive/2005/07/socket_xml_pitf.html */
    private class NoCloseInputStream extends FilterInputStream {
        public NoCloseInputStream(InputStream in) {
            super(in);
        }

        public void close() {
        } // ignore close
    }

    /**
     * @param msgType  The message Type of the message
     * @throws KVException of type "resp" with message "Message format incorrect" if msgType is unknown
     */
    public KVMessage(final String msgType) throws KVException {
        this.msgType = parseMessageType(msgType).toString();
    }

    public KVMessage(final String msgType, final String message) throws KVException {
        final MessageType messageType = parseMessageType(msgType);
        switch (messageType) {
            case RESP:
                if (message == null) throw new KVException(makeResponse(ResponseType.UNKNOWN_ERROR));
                this.message = message;
                this.msgType = msgType;
                break;
            default:
                this.msgType = msgType;
                break;
        }
    }

    /**
     * Parse KVMessage from incoming network connection
     *
     * @param sock
     * @throws KVException if there is an error in parsing the message. The exception should be of type "resp and message should be :
     *                     a. "XML Error: Received unparseable message" - if the received message is not valid XML.
     *                     b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
     *                     c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type.
     */
    public KVMessage(InputStream input) throws KVException {
        try {
            final KVMessage kvTemp = (KVMessage) _jaxbCtx.createUnmarshaller().unmarshal(new NoCloseInputStream(input));
            this.setMsgType(kvTemp.getMsgType());

            switch (parseMessageType(this.msgType)) {
                case PUTREQ:
                    this.setKey(kvTemp.getKey());
                    this.setValue(kvTemp.getValue());
                    break;
                case GETREQ:
                case DELREQ:
                    this.setKey(kvTemp.getKey());
                    break;
                case RESP:
                    this.setMessage(kvTemp.getMessage());
                    if (kvTemp.getKey() != null && kvTemp.getValue() != null) {
                        this.setKey(kvTemp.getKey());
                        this.setValue(kvTemp.getValue());
                    }
                    break;
                default:
                    throw new KVException(makeResponse(ResponseType.MESSAGE_ERROR));
            }

        } catch (final UnmarshalException e) {
            throw new KVException(makeResponse(ResponseType.MESSAGE_ERROR));
        } catch (final JAXBException je) {
            throw new KVException(makeResponse(ResponseType.UNKNOWN_ERROR));
        }
    }

    /**
     * Generate the XML representation for this message.
     *
     * @return the XML String
     * @throws KVException if not enough data is available to generate a valid KV XML message
     */
    public String toXML() throws KVException {

        Writer sw = new StringWriter();

        try {
            _jaxbCtx.createMarshaller().marshal(this, sw);
        } catch (final JAXBException je) {
            throw new KVException(makeResponse(ResponseType.UNKNOWN_ERROR));
        }

        return sw.toString();
    }

    public void sendMessage(Socket sock) throws KVException {

        OutputStream out = null;
        try {
            out = new BufferedOutputStream(sock.getOutputStream());
            String xml = this.toXML();
            out.write(xml.getBytes(), 0, xml.length());
            out.flush();
            sock.shutdownOutput();
        } catch (final IOException e) {
            throw new KVException(KVMessage.makeResponse(ResponseType.DATA_SEND_ERROR));
        }

    }

    private MessageType parseMessageType(final String msgType) throws KVException {
        for (final MessageType messageType : MessageType.values()) {
            if (messageType.toString().equals(msgType)) {
                return messageType;
            }
        }
        throw new KVException(makeResponse(ResponseType.UNKNOWN_ERROR));
    }

    public enum ResponseType {
        SUCCESS {
            public String toString() {
                return "Success";
            }
        },
        DATA_SEND_ERROR {
            public String toString() {
                return "Network Error: Could not send data";
            }
        },
        DATA_RECEIVE_ERROR {
            public String toString() {
                return "Network Error: Could not receive data";
            }
        },
        CONNECT_ERROR {
            public String toString() {
                return "Network Error: Could not connect";
            }
        },
        SOCKET_ERROR {
            public String toString() {
                return "Network Error: Could not create socket";
            }
        },
        MESSAGE_ERROR {
            public String toString() {
                return "XML Error: Received unparseable message";
            }
        },
        KEY_ERROR {
            public String toString() {
                return "Oversized key";
            }
        },
        VALUE_ERROR {
            public String toString() {
                return "Oversized value";
            }
        },
        IO_ERROR {
            public String toString() {
                return "IO Error";
            }
        },
        DNE_ERROR {
            public String toString() {
                return "Does not exist";
            }
        },
        UNKNOWN_ERROR {
            public String toString() {
                return "Unknown Error";
            }
        }
    }
    public static KVMessage makeResponse(final ResponseType responseType) {
        try {
            return new KVMessage(MessageType.RESP.toString(), responseType.toString());
        } catch (final KVException e) { return null; }
    }

    public static KVMessage unknownError(final String customError) {
        try {
            return new KVMessage(
                    MessageType.RESP.toString(),
                    ResponseType.UNKNOWN_ERROR.toString() + ": " + customError
            );
        } catch (final KVException e) { return null; }

    }

    private static JAXBContext _jaxbCtx = null;
    static {
        try {
            _jaxbCtx = JAXBContext.newInstance(KVMessage.class);
        } catch (final Throwable t) {
            System.err.println("Unable to initialize jaxBContext");
            System.exit(-1);
        }
    }
}
