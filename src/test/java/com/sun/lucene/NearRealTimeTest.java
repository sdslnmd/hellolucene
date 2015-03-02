package com.sun.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class NearRealTimeTest {

    @Test
    public void testNearRealTime() throws Exception {
        Directory director = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(director, new IndexWriterConfig(Version.LUCENE_43, new StandardAnalyzer(Version.LUCENE_43)));
        for (int i = 0; i < 10; i++) {
            Document document = new Document();
            document.add(new StringField("id", String.valueOf(i), Field.Store.NO));
            document.add(new StringField("text", "aaa", Field.Store.NO));

            indexWriter.addDocument(document);
        }

    }

}
