package com.sun.lucene;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IndexFilesTest {

    private Directory directory;

    protected String[] ids = {"1", "2"};
    protected String[] unindexed = {"Netherlands", "italy"};
    protected String[] unstrored = {"Amsterdam has a lot of bridges", "Venice has a lot of canals"};
    protected String[] text = {"Amsterdam", "Venice"};

    @Before
    public void setUp() throws Exception {
        directory = new RAMDirectory();
        IndexWriter indexWriter = getWriter();
        for (int i = 0; i < ids.length; i++) {
            Document document = createDocument(ids[i],unindexed[i],unstrored[i],text[i]);
            indexWriter.addDocument(document);
        }
        indexWriter.commit();//可以不调用close 继续commit
        indexWriter.close();
    }

    private Document createDocument(String id,String country,String contents,String city) {
        Document document = new Document();
        StringField idField = new StringField("id", id, Field.Store.YES);
        idField.setBoost(1.0f);//设置加权因子 改变加权因子必须删除旧的doc,或者做update操作
        document.add(idField);
        document.add(new StringField("country", country, Field.Store.YES));
        document.add(new StringField("contents",contents, Field.Store.NO));
        document.add(new StringField("city",city, Field.Store.YES));
        return document;
    }

    private IndexWriter getWriter() throws IOException {
        if (IndexWriter.isLocked(directory)) {
            IndexWriter.unlock(directory);
        }
        return new IndexWriter(directory,
                new IndexWriterConfig(Version.LUCENE_43, new WhitespaceAnalyzer(Version.LUCENE_43)));
    }

    protected int getHitCount(String fileName, String searchString) throws IOException {
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
        Term term = new Term(fileName, searchString);
        Query termQuery = new TermQuery(term);
        return TestUtil.hitCount(indexSearcher, termQuery);
    }

    @Test
    public void testIndexWriter() throws Exception {
        IndexWriter writer = getWriter();
        assertEquals(ids.length, writer.numDocs());
        writer.close();
    }

    @Test
    public void testIndexReader() throws Exception {
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        assertEquals(ids.length, directoryReader.maxDoc());
    }

    @Test
    public void testDeleteBeforeOptimize() throws Exception {
        IndexWriter writer = getWriter();
        assertEquals(2, writer.maxDoc());
        writer.deleteDocuments(new Term("id", "1"));
        writer.commit();
        assertTrue(writer.hasDeletions());
        assertEquals(2, writer.maxDoc());
        assertEquals(1, writer.numDocs());
        writer.close();
    }
    @Test
    public void testDeleteAfterOptimize() throws Exception {
        IndexWriter writer = getWriter();
        assertEquals(2, writer.maxDoc());
        writer.deleteDocuments(new Term("id", "1"));
        writer.commit();
        assertTrue(writer.hasDeletions());
        writer.forceMergeDeletes();
        assertEquals(1, writer.maxDoc());
    }

    @Test
    public void testUpdate() throws Exception {
        assertEquals(1,getHitCount("city","Amsterdam"));

        IndexWriter writer = getWriter();
        Document document = createDocument("1", "Netherlands", "Den haag has a lot of museums", "Den haag");
        writer.updateDocument(new Term("id", "1"), document);
        writer.commit();
        writer.close();
        assertEquals(0,getHitCount("city","Amsterdam"));
        assertEquals(1,getHitCount("city","Den haag"));
    }

    @Test
    public void testQueryParser() throws Exception {
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
        QueryParser queryParser = new QueryParser(Version.LUCENE_43, "contents", new StandardAnalyzer(Version.LUCENE_43));
        Query query = queryParser.parse("+JUnit +Ant -Mock");
        TopDocs docs = indexSearcher.search(query, 10);
        assertNotNull(docs);
        directory.close();
    }

    @Test
    public void testReopen() throws Exception {
        DirectoryReader directoryReader = DirectoryReader.open(directory);

    }
}