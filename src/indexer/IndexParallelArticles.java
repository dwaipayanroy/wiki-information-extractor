/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import common.CollectionStatistics;
import common.CommonMethods;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author dwaipayan
 */
public class IndexParallelArticles {

    String propPath;
    Properties prop;
    Analyzer analyzer;
    String pathEn;
    String pathNonEn;
    String pathTransEn;
    String indexPath;
    IndexWriter indexWriter;
    IndexReader indexReader;
    String nonEnLangName;
    File        indexFile;
    Boolean indexExists;
    int numArticles;
    /*protected */BufferedReader docReaderEn, docReaderNonEn, docReaderTransEn; // document reader

    public IndexParallelArticles(String propPath) throws IOException {

        this.propPath = propPath;
        prop = new Properties();
        try {
            prop.load(new FileReader(propPath));
        } catch (IOException ex) {
            System.err.println("Error: prop file missing at: "+propPath);
            System.exit(1);
        }
        // ----- properties file set
        analyzer = new SimpleAnalyzer();

        nonEnLangName = prop.getProperty("nonEnLang");
        pathEn = prop.getProperty("pathEn");
        pathNonEn = prop.getProperty("pathNonEn");
        pathTransEn = prop.getProperty("pathTransEn");

        numArticles = countLines(pathEn);
        if((numArticles!=countLines(pathNonEn))||
            (numArticles!=countLines(pathTransEn))) {
            System.err.println("Number of article mismatch. Aborting...");
            System.exit(0);
        }

        docReaderEn = new BufferedReader(new FileReader(pathEn));
        docReaderNonEn = new BufferedReader(new FileReader(pathNonEn));
        docReaderTransEn = new BufferedReader(new FileReader(pathTransEn));

        /* index path setting */
        indexFile = new File(prop.getProperty("indexPath"));
        Directory indexDir = FSDirectory.open(indexFile.toPath());
        /* index path set */

        indexExists = false;
        if (DirectoryReader.indexExists(indexDir)) {
            System.out.println("Index exists in "+indexFile.getAbsolutePath());
            indexExists = true;
        }
        else {
            System.out.println("Creating the index in: " + indexFile.getAbsolutePath());
            IndexWriterConfig iwcfg = new IndexWriterConfig(new WhitespaceAnalyzer());
            iwcfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            indexWriter = new IndexWriter(indexDir, iwcfg);
        }
    }

    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));

        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        boolean empty = true;
        while ((readChars = is.read(c)) != -1) {
            empty = false;
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        }
        is.close();
        return (count == 0 && !empty) ? 1 : count;
    }

    public void closeFileAfterReading() throws IOException {
        docReaderEn.close();
        docReaderNonEn.close();
        docReaderTransEn.close();
    }

    public String readArticle(String article, String fieldname) throws IOException {
        return CommonMethods.analyzeText(analyzer, article, fieldname).toString();
    }

    public void readParallelArticle() throws IOException {

        Document doc;
        System.out.println("Number of articles to index: " + numArticles);
        for (int i = 0; i < numArticles; i++) {
            System.out.println("Reading: " + i);
            doc = new Document();

            doc.add(new StringField("id", Integer.toString(i), Field.Store.YES));
            doc.add(new StringField("non-en-lang", nonEnLangName, Field.Store.YES));

            String enArticle = readArticle(docReaderEn.readLine(), "en");
            doc.add(new Field("en", enArticle, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

            String nonEnArticle = readArticle(docReaderNonEn.readLine(), "non-en");
            doc.add(new Field("non-en", nonEnArticle, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

            String transEnArticle = readArticle(docReaderTransEn.readLine(), "trans-en");
            doc.add(new Field("trans-en", transEnArticle, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

            indexWriter.addDocument(doc);
        }
        closeFileAfterReading();
        indexWriter.close();
    }

    public void compareParallelArticles() throws IOException {
        indexReader = DirectoryReader.open(FSDirectory.open(indexFile.toPath()));

        CollectionStatistics cs = new CollectionStatistics(indexReader);
//        cs.buildCollectionStat();
        int docCount = indexReader.maxDoc();
        for (int i = 0; i < docCount; i++) {
            cs.showDocumentVector(i, indexReader, "en");
            cs.showDocumentVector(i, indexReader, "non-en");
            cs.showDocumentVector(i, indexReader, "trans-en");
            System.out.println("");
        }
    }

    public static void main(String[] args) throws IOException {
//        IndexParallelArticles obj = new IndexParallelArticles(args[0]);
        IndexParallelArticles obj = new IndexParallelArticles("/home/dwaipayan/wiki/final/hi.properties");
        if(!obj.indexExists)
            obj.readParallelArticle();
        obj.compareParallelArticles();
    }
}
