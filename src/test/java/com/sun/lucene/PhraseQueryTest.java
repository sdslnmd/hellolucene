package com.sun.lucene;

import junit.framework.Assert;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class PhraseQueryTest {
    Directory directory;
    IndexSearcher indexSearcher;

    @Before
    public void setUp() throws Exception {
        directory = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_43, new WhitespaceAnalyzer(Version.LUCENE_43)));

        Document document = new Document();
        document.add(new StringField("field", "the quick brown fox jumped over the lazy dog", Field.Store.YES));
        indexWriter.addDocument(document);
        indexWriter.close();

        indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    }

    private boolean matched(String[] parase, int slop) throws Exception {
        PhraseQuery phraseQuery = new PhraseQuery();
        phraseQuery.setSlop(slop);
        for (String s : parase) {
            phraseQuery.add(new Term("field", s));
        }
        TopDocs topDocs = indexSearcher.search(phraseQuery, 10);
        return topDocs.totalHits > 0;
    }

    @Test
    public void testSlopComparsion() throws Exception {
        String[] phrase = {"quick", "fox"};
        Assert.assertFalse(matched(phrase, 0));
        Assert.assertTrue(matched(phrase, 1));
    }


}
