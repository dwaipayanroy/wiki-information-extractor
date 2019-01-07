/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;

/**
 * Dump the content of the articles which are common in both Language A and B.
 * A = En
 * B = Es / Hi / Pt
 * 
 * Input: 
 *  1. Lucene index of A
 *  2. Lucene index of B
 *  3. 7 column file: hi-en_wikidataid-enId-enTitle-enDoclen-hiId-hiTitle-hiDoclen
 * 
 * Output:
 *  1. Articles in A common in both A and B
 *  2. Articles in B common in both A and B
 * 
 * @author dwaipayan
 */
public class DumpCommonArticles {

    IndexReader     readerA, readerB;
    IndexSearcher   searcherA, searcherB;
    String          dumpPathA, dumpPathB;
    PrintWriter     pwDumpA, pwDumpB;
    BufferedReader  br;

    public DumpCommonArticles(String indexPathA, String indexPathB, 
            String filePathSevenCol,
            String dumpPathA, String dumpPathB
    ) throws IOException {

        readerA = DirectoryReader.open(FSDirectory.open(new File(indexPathA).toPath()));
        readerB = DirectoryReader.open(FSDirectory.open(new File(indexPathB).toPath()));

        searcherA = new IndexSearcher(readerA);
        searcherB = new IndexSearcher(readerB);

        searcherA.setSimilarity(new DefaultSimilarity());
        searcherB.setSimilarity(new DefaultSimilarity());

	//Construct BufferedReader from InputStreamReader
	br = new BufferedReader(new InputStreamReader(new FileInputStream(filePathSevenCol)));
 
        pwDumpA = new PrintWriter(dumpPathA);
        pwDumpB = new PrintWriter(dumpPathB);
    }

    void closeAll() throws IOException {

        readerA.close();
        readerB.close();

        br.close();

        pwDumpA.close();
        pwDumpB.close();
    }

    public static void main(String[] args) throws IOException, QueryNodeException {

        DumpCommonArticles object;
        
        if(5 != args.length) {
            System.out.println("Usage: java DumpCommonArticle "
                    + "1. indexPathA,\n2. indexPathB, \n" +
                    "3. filePathSevenCol,\n" +
                    "4. dumpPathA,\n5. dumpPathB");
            System.exit(0);
        }

        String indexPathA, indexPathB, filePathSevenCol, dumpPathA, dumpPathB;
        indexPathA = args[0];
        indexPathB = args[1];
        filePathSevenCol = args[2];
        dumpPathA = args[3];
        dumpPathB = args[4];

        object = new DumpCommonArticles(indexPathA, indexPathB, filePathSevenCol, dumpPathA, dumpPathB);
        object.dumpCommonArticle();

        object.closeAll();
    }

    private void dumpCommonArticle() throws IOException, QueryNodeException {

        String line;
        String wikidataId, wikipediaIdA, wikipediaIdB;
        TopScoreDocCollector collectorA, collectorB;
        ScoreDoc[] hitsA, hitsB;
        Document d;
        StandardQueryParser queryParser = new StandardQueryParser();

        int count = 0;

        while ((line = br.readLine()) != null) {

            System.out.println(++count);
//            System.out.println(line);
            String tokens[] = line.split("\t");
            wikidataId = tokens[0];
            wikipediaIdA = tokens[1];
            wikipediaIdB = tokens[4];
//            System.out.println(wikidataId+ "\t" + wikipediaIdA + "\t" + wikipediaIdB);

            collectorA = TopScoreDocCollector.create(1);
            collectorB = TopScoreDocCollector.create(1);

            Query qA, qB;

            qA = queryParser.parse(wikipediaIdA, "docid");
            System.out.print(qA.toString() + " \t");
            // "Searching in first index: "
            searcherA.search(qA, collectorA);
            hitsA = collectorA.topDocs().scoreDocs;
            d = searcherA.doc(hitsA[0].doc);
//            System.out.println(d.get("title"));
            pwDumpA.println(d.get("content"));

            qB = queryParser.parse(wikipediaIdB, "docid");
            // "Searching in second index: "
            System.out.println(qB.toString());
            searcherB.search(qB, collectorB);
            hitsB = collectorB.topDocs().scoreDocs;
            d = searcherB.doc(hitsB[0].doc);
//            System.out.println(d.get("title"));
            pwDumpB.println(d.get("content"));

	}

    }
}
