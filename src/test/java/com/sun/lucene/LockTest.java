package com.sun.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class LockTest {

    Directory directory;

    @Before
    public void setUp() throws IOException {
        String indexDir = "/Users/sunluning/tmp/luceneIndex";
        directory = FSDirectory.open(Paths.get(indexDir).toFile());
    }

    @Test
    public void testWriteLock() throws Exception {
        IndexWriter indexWriter2 = null;
        try (IndexWriter indexWriter1 = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_43, new StandardAnalyzer(Version.LUCENE_43)))) {
            indexWriter2 = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_43, new StandardAnalyzer(Version.LUCENE_43)));
            Assert.fail("we should never reach this point ");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Assert.assertNull(indexWriter2);
        }

    }

}
