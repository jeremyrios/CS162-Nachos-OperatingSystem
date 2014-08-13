/**
 * Implementation of a set-associative cache.
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


/**
 * A set-associate cache which has a fixed maximum number of sets (numSets).
 * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
 * If a set is full and another entry is added, an entry is dropped based on the eviction policy.
 */
public class KVCache implements KeyValueInterface {
    private int numSets = 100;
    private int maxElemsPerSet = 10;
    private final List<CacheSet> _sets;

    /**
     * Creates a new LRU cache.
     * @param cacheSize the maximum number of entries that will be kept in this cache.
     */
    public KVCache(int numSets, int maxElemsPerSet) {
        this.numSets = numSets;
        this.maxElemsPerSet = maxElemsPerSet;
        this._sets = new ArrayList<CacheSet>();
        for (int i = 0; i < numSets; ++i) {
            this._sets.add(new CacheSet(i, this.maxElemsPerSet));
        }
    }

    /**
     * Retrieves an entry from the cache.
     * Assumes the corresponding set has already been locked for writing.
     * @param key the key whose associated value is to be returned.
     * @return the value associated to this key, or null if no value with this key exists in the cache.
     */
    public String get(String key) {
        // Must be called before anything else
        AutoGrader.agCacheGetStarted(key);
        AutoGrader.agCacheGetDelay();

        final String toReturn = _sets.get(this.getSetId(key)).get(key);

        // Must be called before returning
        AutoGrader.agCacheGetFinished(key);

        return toReturn;
    }

    /**
     * Adds an entry to this cache.
     * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
     * If the cache is full, an entry is removed from the cache based on the eviction policy
     * Assumes the corresponding set has already been locked for writing.
     * @param key   the key with which the specified value is to be associated.
     * @param value a value to be associated with the specified key.
     * @return true is something has been overwritten
     */
    public boolean put(String key, String value) {
        // Must be called before anything else
        AutoGrader.agCachePutStarted(key, value);
        AutoGrader.agCachePutDelay();

        _sets.get(this.getSetId(key)).put(key, value);

        // Must be called before returning
        AutoGrader.agCachePutFinished(key, value);
        return false;
    }

    /**
     * Removes an entry from this cache.
     * Assumes the corresponding set has already been locked for writing.
     * @param key   the key with which the specified value is to be associated.
     */
    public void del(String key) {
        // Must be called before anything else
        AutoGrader.agCacheDelStarted(key);
        AutoGrader.agCacheDelDelay();

        _sets.get(this.getSetId(key)).del(key);

        // Must be called before returning
        AutoGrader.agCacheDelFinished(key);
    }

    /**
     * @param key
     * @return  the write lock of the set that contains key.
     */
    public WriteLock getWriteLock(String key) {
        return _sets.get(this.getSetId(key)).getWriteLock();
    }

    /**
     *
     * @param key
     * @return set of the key
     */
    private int getSetId(String key) {
        return Math.abs(key.hashCode() % numSets);
    }

    public String toXML() throws KVException {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            final Document doc = docBuilder.newDocument();

            // The tokens we will be using.
            final String KV_CACHE = "KVCache";
            final String SET = "Set";
            final String ID = "Id";
            final String CACHE_ENTRY = "CacheEntry";
            final String IS_REFERENCED = "isReferenced";
            final String IS_VALID = "isValid";
            final String KEY = "Key";
            final String VALUE = "Value";

            final Element rootElement = doc.createElement(KV_CACHE);
            doc.appendChild(rootElement);

            for (final CacheSet curSet : _sets) {
                Element curSetNode = doc.createElement(SET);
                rootElement.appendChild(curSetNode);

                Attr curSetId = doc.createAttribute(ID);
                curSetId.setValue(Integer.toString(curSet.getId()));
                curSetNode.setAttributeNode(curSetId);

                for (final CacheEntry curEntry : curSet.getCacheEntries()) {
                    Element curEntryNode = doc.createElement(CACHE_ENTRY);
                    curSetNode.appendChild(curEntryNode);

                    Attr curEntryIsReferenced = doc.createAttribute(IS_REFERENCED);
                    curEntryIsReferenced.setValue(Boolean.toString(curEntry.getIsReferenced()));
                    curEntryNode.setAttributeNode(curEntryIsReferenced);

                    Attr curEntryIsValid = doc.createAttribute(IS_VALID);
                    curEntryIsValid.setValue(Boolean.toString(curEntry.getIsValid()));
                    curEntryNode.setAttributeNode(curEntryIsValid);

                    Element curEntryKey = doc.createElement(KEY);
                    Element curEntryValue = doc.createElement(VALUE);
                    curEntryNode.appendChild(curEntryKey);
                    curEntryNode.appendChild(curEntryValue);

                    curEntryKey.appendChild(doc.createTextNode(curEntry.getKey()));
                    curEntryValue.appendChild(doc.createTextNode(curEntry.getValue()));
                }
                // Handle invalid entries
                for (int i = curSet.getCacheEntries().size(); i < this.maxElemsPerSet; ++i) {
                    Element curInvalidNode = doc.createElement(CACHE_ENTRY);
                    curSetNode.appendChild(curInvalidNode);

                    Attr curEntryIsReferenced = doc.createAttribute(IS_REFERENCED);
                    curEntryIsReferenced.setValue(Boolean.toString(false));
                    curInvalidNode.setAttributeNode(curEntryIsReferenced);

                    Attr curEntryIsValid = doc.createAttribute(IS_VALID);
                    curEntryIsValid.setValue(Boolean.toString(false));
                    curInvalidNode.setAttributeNode(curEntryIsValid);

                    Element curEntryKey = doc.createElement(KEY);
                    Element curEntryValue = doc.createElement(VALUE);
                    curInvalidNode.appendChild(curEntryKey);
                    curInvalidNode.appendChild(curEntryValue);


                }
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

    static class CacheEntry {
        boolean _isReferenced;
        boolean _isValid;
        final String _key;
        String _value;

        CacheEntry(final boolean isReferenced,
                   final boolean isValid,
                   final String key,
                   final String value) {
            _isReferenced = isReferenced;
            _isValid = isValid;
            _key = key;
            _value = value;
        }

        boolean getIsValid() { return _isValid; }

        boolean getIsReferenced() { return _isReferenced; }
        CacheEntry setIsReferenced(final boolean isReferenced) { _isReferenced = isReferenced; return this; }

        String getKey() { return _key; }

        String getValue() { return _value; }
        CacheEntry setValue(final String value) { _value = value; return this; }

    }

    /**
     * Encapsulates a cache set of this KVCache.
     */
    static class CacheSet {
        /**
         * The id of this Set. Somewhere in the range of 0 to numSets - 1.
         */
        final int _id;

        /**
         * The size of this CacheSet.
         */
        final int _size;

        /**
         * This Queue holds the Cache entries ordered by time of insertion.
         */
        final Queue<CacheEntry> _queue;

        /**
         * The lock used to synchronize access to this CacheSet.
         */
        final ReentrantReadWriteLock _lock;

        /**
         * This map provides quick checking of whether an entry is present in the cache.
         * Not necessary for correctness, but provides efficiency gains at the cost
         * of increased memory usage.
         */
        final Map<String, CacheEntry> _lookupTable;

        /**
         * Default constructor.
         * @param id  The id of this CacheSet.
         * @param size The size of this CacheSet.
         */
        CacheSet(final int id, final int size) {
            _id = id;
            _size = size;
            _queue = new LinkedList<CacheEntry>();
            _lookupTable = new HashMap<String, CacheEntry>();
            _lock = new ReentrantReadWriteLock();
        }

        WriteLock getWriteLock() {
            return _lock.writeLock();
        }

        Collection<CacheEntry> getCacheEntries() {
            return _queue;
        }

        int getId() {
            return _id;
        }

        /**
         * Inserts or entry into this CacheSet, or updates the value if their is already an entry for the given key.
         * @param key The key to insert.
         * @param value The value to insert.
         */
        void put(final String key, final String value) {
            assert _lock.isWriteLockedByCurrentThread();
            if (_lookupTable.containsKey(key)) {
                // we already have an entry for this key, so we update the cache entry with the new value,
                // and set the refBit to true
                _lookupTable.get(key).setIsReferenced(true).setValue(value);

            } else {

                if (_queue.size() == _size) {
                    performEviction();
                }

                assert _queue.size() < _size;
                // we have room in the cache so we simply add a new entry to the queue
                final CacheEntry newEntry = new CacheEntry(false, true, key, value);
                _lookupTable.put(key, newEntry);
                _queue.add(newEntry);
            }
        }

        String get(final String key) {
            assert _lock.isWriteLockedByCurrentThread();
            if (_lookupTable.containsKey(key)) {
                return _lookupTable.get(key).setIsReferenced(true).getValue();
            } else {
                return null;
            }


        }

        String del(final String key) {
            assert _lock.isWriteLockedByCurrentThread();
            if (_lookupTable.containsKey(key)) {
                final CacheEntry toRemove = _lookupTable.remove(key);
                _queue.remove(toRemove);
                return toRemove.getValue();
            } else {
                return null;
            }
        }

        void performEviction() {
            assert _lock.isWriteLockedByCurrentThread();
            CacheEntry toRemove = null;
            while (toRemove == null) {
                for (final CacheEntry cacheEntry : _queue) {
                    if (!cacheEntry.getIsReferenced()) {
                        toRemove = cacheEntry;
                        break;
                    } else {
                        cacheEntry.setIsReferenced(false);
                    }
                }
            }
            this.del(toRemove.getKey());
        }
    }

}
