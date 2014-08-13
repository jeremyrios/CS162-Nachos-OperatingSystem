/**
 * Persistent Key-Value storage layer. Current implementation is transient,
 * but assume to be backed on disk when you do your project.
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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * This is a dummy KeyValue Store. Ideally this would go to disk,
 * or some other backing store. For this project, we simulate the disk like
 * system using a manual delay.
 */
public class KVStore implements KeyValueInterface {
    private Dictionary<String, String> store = null;

    public KVStore() {
        resetStore();
    }

    private void resetStore() {
        store = new Hashtable<String, String>();
    }

    public Dictionary<String, String> getStore() {
        return this.store;
    }

    public boolean put(String key, String value) throws KVException {
        AutoGrader.agStorePutStarted(key, value);

        try {
            putDelay();
            store.put(key, value);
            return false;
        } finally {
            AutoGrader.agStorePutFinished(key, value);
        }
    }

    public String get(String key) throws KVException {
        AutoGrader.agStoreGetStarted(key);

        try {
            getDelay();
            String retVal = this.store.get(key);
            if (retVal == null) {
                KVMessage msg = new KVMessage("resp", "key \"" + key + "\" does not exist in store");
                throw new KVException(msg);
            }
            return retVal;
        } finally {
            AutoGrader.agStoreGetFinished(key);
        }
    }

    public void del(String key) throws KVException {
        AutoGrader.agStoreDelStarted(key);

        try {
            delDelay();
            if(key != null)
                this.store.remove(key);
        } finally {
            AutoGrader.agStoreDelFinished(key);
        }
    }

    private void getDelay() {
        AutoGrader.agStoreDelay();
    }

    private void putDelay() {
        AutoGrader.agStoreDelay();
    }

    private void delDelay() {
        AutoGrader.agStoreDelay();
    }

    public String toXML() throws KVException {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();

            // The tokens we will be using.
            final String KVSTORE = "KVStore";
            final String KVPAIR = "KVPair";
            final String KEY = "Key";
            final String VALUE = "Value";

            final Element rootElement = doc.createElement(KVSTORE);
            doc.appendChild(rootElement);

            Enumeration<String> enumKey = store.keys();

            while (enumKey.hasMoreElements()) {
                String key = enumKey.nextElement();
                String value = store.get(key);
                Element kvPairNode = doc.createElement(KVPAIR);
                rootElement.appendChild(kvPairNode);
                Element keyNode = doc.createElement(KEY);
                keyNode.appendChild(doc.createTextNode(key));
                kvPairNode.appendChild(keyNode);
                Element valueNode = doc.createElement(VALUE);
                valueNode.appendChild(doc.createTextNode(value));
                kvPairNode.appendChild(valueNode);
            }

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource domSource = new DOMSource(doc);
            final Writer sw = new StringWriter();
            final Result streamResult = new StreamResult(sw);

            transformer.transform(domSource, streamResult);

            return sw.toString();

        } catch (final ParserConfigurationException e) {
            throw new KVException(KVMessage.unknownError("Could not create an XML parser"));
        } catch (final TransformerException e) {
            throw new KVException(KVMessage.unknownError("Could not generate XML"));
        }

    }

    public void dumpToFile(String fileName) throws KVException {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(this.toXML());
            out.newLine();
            out.close();
        } catch (IOException e) {
            throw new KVException(KVMessage.unknownError("Could not write to the given file"));
        }
    }

    public void restoreFromFile(String fileName) throws KVException {
        try {
            File file = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("KVPair");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String key = getTagValue("Key", eElement);
                    String value = getTagValue("Value", eElement);
                    this.store.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new KVException(KVMessage.unknownError("Could not read from the given file"));
        } catch (final ParserConfigurationException e) {
            throw new KVException(KVMessage.unknownError("Could not create an XML parser"));
        } catch (SAXException e) {
            throw new KVException(KVMessage.unknownError("Could not parse the given XML file"));
        }
    }


    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = nlList.item(0);
        return nValue.getNodeValue();
    }

}
