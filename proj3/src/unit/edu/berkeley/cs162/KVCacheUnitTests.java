package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.Integer;

public final class KVCacheUnitTests {

    /**
     * Sanity test. Checks whether put and get actually work.
     */
    @Test public final void testBasicPutAndGet() throws  Exception {
        final KVCache.CacheSet set = new KVCache.CacheSet(0, 10);
        final String key1 = "key1";
        final String value1 = "value1";
        final String key2 = "key2";
        final String value2 = "value2";
        set.put(key1, value1);
        set.put(key2, value2);

        assertEquals(value1, set.get(key1));
        assertEquals(value2, set.get(key2));
    }

    /**
     * Makes sure that eviction is actually occurring.
     */
    @Test public final void testSizeInvariantIsMaintained() throws Exception{
        final KVCache.CacheSet set = new KVCache.CacheSet(0, 10);
        for (int i = 0; i < 11; ++i) {
            final String strNum = i + "";
            set.put(strNum, strNum);
        }
        // since 0 is the first element inserted, it should be evicted.
        assertNull(set.get("0"));
    }

    /**
     * Simple test of eviction ordering.
     */
    @Test public final void testSimpleEvictionOrder() throws Exception {
        final KVCache.CacheSet set = new KVCache.CacheSet(0, 5);

        for (int i = 0; i < 5; ++i) {
            final String strNum = i + "";
            set.put(strNum, strNum);
        }

        // lets reference all the entries now.
        assertEquals(set.get("0"), "0");
        assertEquals(set.get("1"), "1");
        assertEquals(set.get("2"), "2");
        assertEquals(set.get("3"), "3");
        assertEquals(set.get("4"), "4");

        // now we insert a new entry. 0 should be evicted.
        set.put("NEW0", "NEW0");
        assertNull(set.get("0"));

        // insert another entry. 1 should be evicted.
        set.put("NEW1", "NEW1");
        assertNull(set.get("1"));

        // insert another entry. 2 should be evicted.
        set.put("NEW2", "NEW2");
        assertNull(set.get("2"));

        // reference 3
        assertEquals(set.get("3"), "3");

        // insert again. 4 should be evicted
        set.put("NEW4", "NEW4");
        assertNull(set.get("4"));
    }

    @Test public final void testSimpleToXml() throws Exception {
        final KVCache kvCache = new KVCache(2, 2);
        kvCache.put("1", "1");
        kvCache.put("2", "2");
        kvCache.put("3", "3");
        kvCache.put("4", "4");

        final StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"  );
        sb.append(  "<KVCache>"                                                   );
        sb.append(      "<Set Id=\"0\">"                                          );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"true\">");
        sb.append(              "<Key>2</Key>"                                    );
        sb.append(              "<Value>2</Value>"                                );
        sb.append(          "</CacheEntry>"                                       );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"true\">");
        sb.append(              "<Key>4</Key>"                                    );
        sb.append(              "<Value>4</Value>"                                );
        sb.append(          "</CacheEntry>"                                       );
        sb.append(      "</Set>"                                                  );
        sb.append(      "<Set Id=\"1\">"                                          );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"true\">");
        sb.append(              "<Key>1</Key>"                                    );
        sb.append(              "<Value>1</Value>"                                );
        sb.append(          "</CacheEntry>"                                       );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"true\">");
        sb.append(              "<Key>3</Key>"                                    );
        sb.append(              "<Value>3</Value>"                                );
        sb.append(          "</CacheEntry>"                                       );
        sb.append(      "</Set>"                                                  );
        sb.append(  "</KVCache>"                                                  );

        assertEquals(sb.toString(), kvCache.toXML());


    }

    @Test public final void testXMLPrintsEmptyCacheValues() throws Exception {
        final KVCache kvCache = new KVCache(2, 2);

        final StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"   );
        sb.append(  "<KVCache>"                                                    );
        sb.append(      "<Set Id=\"0\">"                                           );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"false\">");
        sb.append(              "<Key/>"                                           ); //  elems are self closing if empty
        sb.append(              "<Value/>"                                         );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"false\">");
        sb.append(              "<Key/>"                                           );
        sb.append(              "<Value/>"                                         );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(      "</Set>"                                                   );
        sb.append(      "<Set Id=\"1\">"                                           );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"false\">");
        sb.append(              "<Key/>"                                           );
        sb.append(              "<Value/>"                                         );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"false\">");
        sb.append(              "<Key/>"                                           );
        sb.append(              "<Value/>"                                         );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(      "</Set>"                                                   );
        sb.append(  "</KVCache>"                                                   );
        assertEquals(sb.toString(), kvCache.toXML());
    }

    @Test public final void testXMLPrintsSomeEmptyCacheValues() throws Exception {
        final KVCache kvCache = new KVCache(2, 2);
        kvCache.put("1", "1");
        kvCache.put("2", "2");

        final StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"   );
        sb.append(  "<KVCache>"                                                    );
        sb.append(      "<Set Id=\"0\">"                                           );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"true\">" );
        sb.append(              "<Key>2</Key>"                                     );
        sb.append(              "<Value>2</Value>"                                 );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"false\">");
        sb.append(              "<Key/>"                                           );
        sb.append(              "<Value/>"                                         );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(      "</Set>"                                                   );
        sb.append(      "<Set Id=\"1\">"                                           );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"true\">" );
        sb.append(              "<Key>1</Key>"                                     );
        sb.append(              "<Value>1</Value>"                                 );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(          "<CacheEntry isReferenced=\"false\" isValid=\"false\">");
        sb.append(              "<Key/>"                                           );
        sb.append(              "<Value/>"                                         );
        sb.append(          "</CacheEntry>"                                        );
        sb.append(      "</Set>"                                                   );
        sb.append(  "</KVCache>"                                                   );

        assertEquals(sb.toString(), kvCache.toXML());

    }

}
