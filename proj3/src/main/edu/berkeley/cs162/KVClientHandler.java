/**
 * Handle client connections over a socket interface
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.io.ObjectInputStream;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * It uses a threadpool to ensure that none of it's methods are blocking.
 */
public class KVClientHandler implements NetworkHandler {
    private KVServer kv_Server = null;
    private ThreadPool threadpool = null;

    public KVClientHandler(KVServer kvServer) {
        initialize(kvServer, 10);
    }

    public KVClientHandler(KVServer kvServer, int connections) {
        initialize(kvServer, connections);
    }

    private void initialize(KVServer kvServer, int connections) {
        this.kv_Server = kvServer;
        threadpool = new ThreadPool(connections);
    }


    private class ClientHandler implements Runnable {
        private KVServer kvServer = null;
        private Socket client = null;
        private InputStream input = null;
        private KVMessage inMsg = null;
        private KVMessage respMsg = null;

        @Override
        public void run() {
            try {
                input = new BufferedInputStream(client.getInputStream());
                inMsg = new KVMessage(input);
                //respMsg = new KVMessage(KVMessage.MessageType.RESP.toString());
                if (KVMessage.MessageType.PUTREQ.toString().equals(inMsg.getMsgType())) {
                    kvServer.put(inMsg.getKey(), inMsg.getValue());
                    respMsg = KVMessage.makeResponse(KVMessage.ResponseType.SUCCESS);
                } else if (KVMessage.MessageType.GETREQ.toString().equals(inMsg.getMsgType())) {
                    final String value = kvServer.get(inMsg.getKey());
                    respMsg = KVMessage.makeResponse(KVMessage.ResponseType.SUCCESS);
                    respMsg.setKey(inMsg.getKey());
                    respMsg.setValue(value);
                } else if (KVMessage.MessageType.DELREQ.toString().equals(inMsg.getMsgType())) {
                    kvServer.del(inMsg.getKey());
                    respMsg = KVMessage.makeResponse(KVMessage.ResponseType.SUCCESS);
                } else {
                    throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.MESSAGE_ERROR));
                }
                respMsg.sendMessage(client);
            } catch (IOException e) {
                // Drop message since we can't do anything.
            }
            catch (KVException e) {
                try {
                    e.getMsg().sendMessage(client);
                } catch (final KVException e2) {
                    // Can't really do anything here, so we just drop the message.
                }
            }
        }

        public ClientHandler(KVServer kvServer, Socket client) {
            this.kvServer = kvServer;
            this.client = client;
        }
    }

    /* (non-Javadoc)
      * @see edu.berkeley.cs162.NetworkHandler#handle(java.net.Socket)
      */
    @Override
    public void handle(Socket client) throws IOException {
        Runnable r = new ClientHandler(kv_Server, client);
        try {
            threadpool.addToQueue(r);
        } catch (InterruptedException e) {
            // Ignore this error
        }
    }
}
