/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import common.CommonVariables;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author dwaipayan
 */
public class GetAllWikidataId {

    String      propPath;
    Properties  prop;               // prop of the init.properties file

    int docCount;
    File        indexFile;
    IndexReader indexReader;
    IndexSearcher indexSearcher;
    
    public GetAllWikidataId(String propPath) throws IOException {

        this.propPath = propPath;
        prop = new Properties();
        try {
            prop.load(new FileReader(propPath));
        } catch (IOException ex) {
            System.err.println("Error: prop file missing at: "+propPath);
            System.exit(1);
        }
        // ----- properties file set

        indexFile = new File(prop.getProperty("indexPath"));
        Directory indexDir = FSDirectory.open(indexFile.toPath());
        /* index path set */

        if (!DirectoryReader.indexExists(indexDir)) {
            System.out.println("Index doesn't exists in "+indexFile.getAbsolutePath());
            System.exit(0);
        }
        else {
            /* setting indexReader and indexSearcher */
            indexReader = DirectoryReader.open(FSDirectory.open(indexFile.toPath()));

            indexSearcher = new IndexSearcher(indexReader);
            docCount = indexReader.maxDoc();
        }
    }

    public void getAllwikidataId() throws IOException {

        Query query = new MatchAllDocsQuery();
        TopScoreDocCollector collector = TopScoreDocCollector.create((int) docCount);
        TopDocs allDocs;
        indexSearcher.search(query, collector);
        allDocs = collector.topDocs();
        ScoreDoc[] hits = allDocs.scoreDocs;
        int hits_length = hits.length;
        System.out.println(hits_length);

        for (int i = 0; i < hits_length; i++) {
            int luceneDocid = hits[i].doc;
            Document d = indexSearcher.doc(luceneDocid);
            System.out.print(luceneDocid+"\t"+d.get(CommonVariables.FIELD_ID)+ "\t" +d.get(CommonVariables.FIELD_TITLE) + "\t");

            // +++
            // Term vector for this document and field, or null if term vectors were not indexed
            Terms terms = indexReader.getTermVector(luceneDocid, CommonVariables.FIELD_CONTENT);
            if(null == terms) {
                System.out.println(0);
                continue;
//                System.err.println("Error:Term-vectors-not-indexed: "+luceneDocid);
            }

            //* unique term count
//            System.out.print(terms.size() + "\t");

            TermsEnum iterator = terms.iterator();
            BytesRef byteRef;

            int docSize = 0;
            while((byteRef = iterator.next()) != null) {
            //* for each word in the document
                String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
                long termFreq = iterator.totalTermFreq();    // tf of 't'
                docSize += termFreq;
            }
            //* document size
            System.out.println(docSize);
        }
        System.out.println("Completed");
        // ---
    }

    public static void main(String[] args) throws IOException {

        GetAllWikidataId getAllWikidataId;

        String usage = "Usage: java Wt10gIndexer <init.properties>\n"
        + "Properties file must contain:\n"
        + "1. indexPath = dir. path in which the index is stored\n";

        // for debuging purpose
        /*
        args = new String[1];
        args[0] = "/home/dwaipayan/Dropbox/programs/Wikipedia/init.properties";
        //*/
        if(args.length == 0) {
            System.out.println(usage);
            System.exit(1);
        }

        getAllWikidataId = new GetAllWikidataId(args[0]);

        getAllWikidataId.getAllwikidataId();
    }
}
