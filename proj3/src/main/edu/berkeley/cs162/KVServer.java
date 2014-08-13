/**
 * Slave Server component of a KeyValue store
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

/**
 * This class defines the slave key value servers. Each individual KVServer
 * would be a fully functioning Key-Value server. For Project 3, you would
 * implement this class. For Project 4, you will have a Master Key-Value server
 * and multiple of these slave Key-Value servers, each of them catering to a
 * different part of the key namespace.
 *
 */

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


public class KVServer implements KeyValueInterface {
    private final KVStore dataStore;
    private final KVCache dataCache;

    private static final int MAX_KEY_SIZE = 256;
    private static final int MAX_VAL_SIZE = 256 * 1024;

    KVCache getDataCache() {
        return dataCache;
    }

    KVStore getDataStore() {
        return dataStore;
    }

    /**
     * @param numSets number of sets in the data Cache.
     */
    public KVServer(int numSets, int maxElemsPerSet) {
        dataStore = new KVStore();
        dataCache = new KVCache(numSets, maxElemsPerSet);
        AutoGrader.registerKVServer(dataStore, dataCache);
    }


    private void validateKey(final String key) throws KVException {
        if (key == null || key.length() == 0) {
            throw new KVException(KVMessage.unknownError("Key cannot be empty!"));
        } else if (key.length() > MAX_KEY_SIZE) {
            throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.KEY_ERROR));
        }
    }

    private void validateValue(final String value) throws KVException {
        if (value == null || value.length() == 0) {
            throw new KVException(KVMessage.unknownError("Value cannot be empty!"));
        } else if (value.length() > MAX_VAL_SIZE) {
            throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.VALUE_ERROR));
        }

    }

    public boolean put(String key, String value) throws KVException {
        // Must be called before anything else
        AutoGrader.agKVServerPutStarted(key, value);
        final WriteLock lock = dataCache.getWriteLock(key);
        try {
            validateKey(key);
            validateValue(key);
            // trying to make critical section as small as possible
            lock.lock();
            synchronized (dataStore) {
                try {
                    dataStore.put(key, value); // If an exception is thrown here, we throw an IO error
                    dataCache.put(key, value); // this will only be reached if the prev line is successful
                } catch (final KVException e) {
                    throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.IO_ERROR));
                }
            }
        } finally {
            AutoGrader.agKVServerPutFinished(key, value);
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
        return true;
    }

    public String get(String key) throws KVException {
        // Must be called before anything else
        String value = null;
        AutoGrader.agKVServerGetStarted(key);

        final WriteLock lock = dataCache.getWriteLock(key);

        try {
            validateKey(key);
            lock.lock();

            // First we try to get the data from the cache.
            value = dataCache.get(key);
            // If it isn't in the cache, we need to get it from the store
            if (value == null) {

                synchronized (dataStore) {

                    try {
                        // try to get the value from the store
                        value = dataStore.get(key);

                    } catch (final KVException e) {
                        // This is because the skeleton throws an exception here
                        if (e.getMsg().getMsgType().equals("resp") &&
                                e.getMsg().getMessage().equals("key \"" + key + "\" does not exist in store")) {

                            throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.DNE_ERROR));

                        } else {
                            // otherwise, there was some other error with the store, so we abort
                            throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.IO_ERROR));
                        }
                    }

                    if (value == null) {
                        // just in case we have a store that can return null if the value is not there
                        throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.DNE_ERROR));

                    } else {
                        // we have retrieved the value from the store, so we insert it in the cache
                        dataCache.put(key, value);
                    }

                }
            }

        } finally {
            // Must be called before returning
            AutoGrader.agKVServerGetFinished(key);
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
        return value;
    }

    public void del(String key) throws KVException {
        // Must be called before anything else
        AutoGrader.agKVServerDelStarted(key);

        final WriteLock lock = dataCache.getWriteLock(key);

        String value = null;

        try {
            validateKey(key);
            lock.lock();
            synchronized (dataStore) {


                // First we check if the value is actually in the store
                try {
                    value = dataStore.get(key);

                } catch (final KVException e) {
                    // This is because the skeleton throws an exception here
                    if (e.getMsg().getMsgType().equals("resp") &&
                            e.getMsg().getMessage().equals("key \"" + key + "\" does not exist in store")) {

                        throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.DNE_ERROR));

                    } else {
                        // otherwise, there was some other error with the store, so we abort
                        throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.IO_ERROR));
                    }

                }
                // assuming we have gotten to this point, dataStore.get did not return an exception
                // however, since the spec is unclear, I will check if the value is null, just in case
                // the autograder KVStore does not throw an exception if you get a nonexistent key

                if (value == null) {
                    throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.DNE_ERROR));
                } else {
                    try {
                        // now we atomically perform the delete
                        dataStore.del(key); // if this fails we get an IOError
                        dataCache.del(key); // this will only happen if the above line is successful


                    } catch (final KVException e) {
                        throw new KVException(KVMessage.makeResponse(KVMessage.ResponseType.IO_ERROR));
                    }
                }
            }
        } finally {
            // Must be called before returning
            AutoGrader.agKVServerDelFinished(key);
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
