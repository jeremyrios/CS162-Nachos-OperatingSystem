package edu.berkeley.cs162;

import java.io.ByteArrayInputStream;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;

public final class KVMessageUnitTests{

    @Test
    public final void putMessageTest(){
        try {

            // put test
            final ByteArrayInputStream fakePutStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>putkey</Key><Value>putvalue</Value>\"</KVMessage>".getBytes());
            KVMessage kv = new KVMessage(fakePutStream);
            assertTrue(kv != null);
            assertTrue(kv.getKey().equals("putkey"));
            assertTrue(kv.getMsgType().equals("putreq"));
            assertTrue(kv.getMessage() == null);
            assertTrue(kv.getValue().equals("putvalue"));
        }
       catch (KVException e){}
    }
    
    @Test
    public final void getMessageTest(){
            // get test
        try{
            final ByteArrayInputStream fakeGetStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>getkey</Key></KVMessage>".getBytes());
            KVMessage kv1 = new KVMessage(fakeGetStream);
            assertTrue(kv1 != null);
            assertTrue(kv1.getMsgType().equals("getreq"));
            assertTrue(kv1.getKey().equals("getkey"));
        }
        catch(KVException e){}
    }
    
    @Test 
    public final void delMessageTest(){
            // delete test
        try{
            final ByteArrayInputStream fakeDelStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<KVMessage type=\"delreq\">\n<Key>delkey</Key>\n</KVMessage>".getBytes());
            KVMessage kv2 = new KVMessage(fakeDelStream);
            assertTrue(kv2 != null);
            assertTrue(kv2.getMsgType().equals("delreq"));
            assertTrue(kv2.getKey().equals("delkey"));
        }
        catch (KVException e){}
    }
    @Test
    public final void respMessageTest(){
            // success response test
        try{
            final ByteArrayInputStream fakePutRespStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<KVMessage type=\"resp\">\n<Message>Success</Message>\n</KVMessage>".getBytes());
            KVMessage kv3 = new KVMessage(fakePutRespStream);
            assertTrue(kv3 != null);
            assertTrue(kv3.getMsgType().equals("resp"));
            assertTrue(kv3.getMessage().equals("Success"));

            // unsuccess put/get/delete response test
            final ByteArrayInputStream fakeErrorStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"resp\"><Message>Error Message</Message></KVMessage>".getBytes());
            KVMessage kv4 = new KVMessage(fakeErrorStream);
            assertTrue(kv4 != null);
            assertTrue(kv4.getMsgType().equals("resp"));
            assertTrue(kv4.getMessage().equals("Error Message"));
        }
        catch(KVException e){}
        
    }
    
    @Test
    public final void wrongFormatMessageTest(){
            // invalid message type and exception constructor
            final ByteArrayInputStream fakeInvalidMessage = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"Alex Rammos Mom\"><Message></Message></KVMessage>".getBytes());
            try{
                KVMessage kv5 = new KVMessage(fakeInvalidMessage);
            }
            catch (KVException e){
                assertTrue(e.getMsg().getMessage().equals("Unknown Error"));
                assertTrue(e.getMsg().getMsgType().equals("resp"));
            }

     }
        
    @Test
    public final void toXmlTestPutReq(){


       //putreq test
       try{    
            KVMessage kv = new KVMessage("putreq");
            kv.setKey("Name");
            kv.setValue("Alex");
            String toCompare = "<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?><KVMessagetype=\"putreq\"><Key>Name</Key><Value>Alex</Value></KVMessage>"; 
            assertTrue(toCompare.replaceAll("\\s","").equals(kv.toXML().replaceAll("\\s","")));    
       }
       catch(KVException e){  
            System.out.println(e.getMsg().getMessage());
       }
     }

    @Test
    public final void testToXmlGetReq(){

       //getreq test
       try{    
            KVMessage kv = new KVMessage("getreq");
            kv.setKey("Name");
            String toCompare = "<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?><KVMessagetype=\"getreq\"><Key>Name</Key></KVMessage>"; 
            assertTrue(toCompare.replaceAll("\\s","").equals(kv.toXML().replaceAll("\\s","")));    
       }
       catch(KVException e){  
            System.out.println(e.getMsg().getMessage());
       }
     }

    @Test
    public final void testToXmlDelReq(){

       //delreq test
       try{    
            KVMessage kv = new KVMessage("delreq");
            kv.setKey("Name");
            String toCompare = "<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?><KVMessagetype=\"delreq\"><Key>Name</Key></KVMessage>"; 
            assertTrue(toCompare.replaceAll("\\s","").equals(kv.toXML().replaceAll("\\s","")));    
       }
       catch(KVException e){  
            System.out.println(e.getMsg().getMessage());
       }
     }

    @Test
    public final void testToXmlGetResp(){

       //get resp test
       try{    
            KVMessage kv = new KVMessage("resp");
            kv.setKey("Name");
            kv.setValue("Max");
            String toCompare = "<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?><KVMessagetype=\"resp\"><Key>Name</Key><Value>Max</Value></KVMessage>"; 
            assertTrue(toCompare.replaceAll("\\s","").equals(kv.toXML().replaceAll("\\s","")));    
       }
       catch(KVException e){  
            System.out.println(e.getMsg().getMessage());
       }
     }

    @Test
    public final void testToXmlPutResp(){

       //put resp test
       try{    
            KVMessage kv = new KVMessage("resp");
            kv.setMessage("Success"); 
            String toCompare = "<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?><KVMessagetype=\"resp\"><Message>Success</Message></KVMessage>"; 
            assertTrue(toCompare.replaceAll("\\s","").equals(kv.toXML().replaceAll("\\s","")));    
       }
       catch(KVException e){  
            System.out.println(e.getMsg().getMessage());
       }
     }

    @Test
    public final void testToXmlDelResp(){

       //del resp test
       try{    
            KVMessage kv = new KVMessage("resp");
            kv.setMessage("Success"); 
            String toCompare = "<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?><KVMessagetype=\"resp\"><Message>Success</Message></KVMessage>"; 
            assertTrue(toCompare.replaceAll("\\s","").equals(kv.toXML().replaceAll("\\s","")));    
       }
       catch(KVException e){  
            System.out.println(e.getMsg().getMessage());
       }
     }
}
