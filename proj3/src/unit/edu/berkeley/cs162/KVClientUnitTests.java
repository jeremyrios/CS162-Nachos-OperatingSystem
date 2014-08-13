package edu.berkeley.cs162;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;
import java.net.*;

public final class KVClientUnitTests{

    @Test
    public final void testConstructor(){
        String server = "EECS";
        int port = 8080;
 	KVClient kvClient = new KVClient(server, port);
        assertTrue(kvClient.getServer().equals(server));
        assertTrue(kvClient.getPort() == port);
    }
    @Test
    public final void sendMessageTest(){
        /*Thread client = new Thread(){
            public void run(){
                InetAddress address = null;
                Socket connection;
                final int port = 8080;
                try{
                    address = InetAddress.getByName("localhost"); 
                    connection = new Socket(address, port);
                    
                    final OutputStreamWriter streamOut = new OutputStreamWriter(connection.getOutputStream());
                    streamOut.write("hi how is going ?!!!");
                    streamOut.flush();
                    
                    final InputStreamReader streamIn = new InputStreamReader(connection.getInputStream());
                    
                }
                catch(UnknownHostException e){}
                catch(IOException e){}
            }
            
            
        };*/
        
        Thread client = new Thread(){
            public void run(){
                InetAddress address = null;
                Socket connection = null;
                final int port = 8080;
                final String ipAddress = "10.10.66.214";
                
                //String outmessage ;//= "hey there!!!";
                //outmessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>putkey</Key><Value>putvalue</Value>\"</KVMessage>";
                try{
                    String to = "", from = "";
                    KVMessage testMessage = new KVMessage("resp");
                    testMessage.setValue("putvalue");
                    testMessage.setKey("putkey");
                    //Thread.yield();
                    address = InetAddress.getByName("localhost"); 
                    connection = new Socket(address, port);

                    testMessage.sendMessage(connection);
                    
                    final BufferedReader in = new BufferedReader(   
                                 new InputStreamReader(connection.getInputStream()));
                    
                    while( (from = in.readLine()) != null ) {
                             //System.out.println(from);
                             // if ("</KVMessage>".equals(from.replaceAll("\\s",""))) break;
                             // result.append(from);
                             // out.print(from + "\r\n"); // echo to client                     
                    }
               
                    in.close();
                    connection.close();
                }
                catch(KVException e){/*e.getMsg.getMessage();*/}
                catch(UnknownHostException e){e.printStackTrace();}
                catch(IOException e){e.printStackTrace();}
                finally{
                    try{
                        connection.close();
                    }
                    catch(IOException e){}
                }
            }
            
            
        };
        Thread server = new Thread(){
            public void run(){
                Socket s = null;
                try{
                        final ServerSocket connection = new ServerSocket( 8080 );

                                      // Wait for connection
                        String from = "";
                        StringBuilder result = new StringBuilder();
                        s = connection.accept();
                                      // Socket input & output  
                        final BufferedReader in = new BufferedReader(   
                                 new InputStreamReader(s.getInputStream()));
                        int i = 0;
                        
                        //final PrintStream out = new PrintStream(s.getOutputStream());
                        while( (from=in.readLine()) != null && !from.equals("")) {
                             //System.out.println( from );
                             result.append(from);
                             i++;
                             if(i==5) break;
                             // out.print(from + "\r\n"); // echo to client                     
                        }
                        //System.out.println(result);
                        
                        final KVMessage response = new KVMessage("resp", "Success");
                        // response.setMessage("Success");
                        response.sendMessage(s);
                        
                        s.close();  
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally{
                    try{
                        s.close();
                    }
                    catch(IOException e){}
                }
            }
        };
        server.start();
        client.start();
        
        try{
           server.join();
           client.join();
        }
        catch(InterruptedException e){e.printStackTrace();}
    }
}

