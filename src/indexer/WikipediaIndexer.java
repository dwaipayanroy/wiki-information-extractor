/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import static common.CommonVariables.FIELD_CONTENT;
import static common.CommonVariables.FIELD_ID;
import static common.CommonVariables.FIELD_TITLE;
import static common.CommonVariables.FIELD_URL;
import common.EnglishAnalyzerWithSmartStopword;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 *
 * @author dwaipayan
 */
public class WikipediaIndexer {

    String      propPath;
    Properties  prop;               // prop of the init.properties file

    String      collPath;           // path of the collection
    String      collSpecPath;       // path of the collection spec file
    String      collLang;           // language of the wikipedia collection

    File        collDir;            // collection Directory
    File        indexFile;          // place where the index will be stored
    String      toStore;            // YES / NO; to be read from prop file; default - 'NO'
    String      storeTermVector;    // NO / YES / WITH_POSITIONS / WITH_OFFSETS / WITH_POSITIONS_OFFSETS; to be read from prop file; default - YES
    String      stopFilePath;
    Analyzer    analyzer;           // analyzer

    IndexWriter indexWriter;
    boolean     boolIndexExists;    // boolean flag to indicate whether the index exists or not
    boolean     boolIndexFromSpec;  // true; false if indexing from collPath
    int         docIndexedCounter;  // document indexed counter
    boolean     boolDumpIndex;      // true if want ot dump the entire collection
    String      dumpPath;           // path of the file in which the dumping to be done
    boolean     boolToDump;

    String docid;
    URL url;
    String title;
    String content;
    
    WikiDocIterator articles;

    public WikipediaIndexer(String propPath) throws IOException {

        this.propPath = propPath;
        prop = new Properties();
        try {
            prop.load(new FileReader(propPath));
        } catch (IOException ex) {
            System.err.println("Error: prop file missing at: "+propPath);
            System.exit(1);
        }
        // ----- properties file set

        // +++++ setting the analyzer using English Analyzer with Smart stopword list
        collLang = prop.getProperty("collLang");
        if(null==collLang) {
            System.err.println("Error: collLang not specified in .properties file\n"
                    + "Available options: en/es/hi/pt");
            System.exit(0);
        }
        switch(collLang) {
            case "en":
                analyzer = new EnglishAnalyzer();
                break;
            case "es":
                analyzer = new SpanishAnalyzer();
                break;
            case "hi":
                analyzer = new HindiAnalyzer();
                break;
            case "pt":
                analyzer = new PortugueseAnalyzer();
                break;
            default:
                analyzer = null;
                System.out.println("Error: Setting analyzer with the corresponding language");
                System.exit(0);
        }
        // ----- analyzer set: analyzer

        // +++++ collection path setting 
        if(prop.containsKey("collSpec")) {
            boolIndexFromSpec = true;
            collSpecPath = prop.getProperty("collSpec");
        }
        else if(prop.containsKey("collPath")) {
            boolIndexFromSpec = false;
            collPath = prop.getProperty("collPath");
            collDir = new File(collPath);
            if (!collDir.exists() || !collDir.canRead()) {
                System.err.println("Collection directory '" +collDir.getAbsolutePath()+ "' does not exist or is not readable");
                System.exit(1);
            }
        }
        else {
            System.err.println("Neither collPath nor collSpec is present");
            System.exit(1);
        }
        // ----- collection path setting 

        /* index path setting */
        indexFile = new File(prop.getProperty("indexPath"));
        Directory indexDir = FSDirectory.open(indexFile.toPath());
        /* index path set */

//        if (DirectoryReader.indexExists(indexDir)) {
//            System.out.println("Index exists in "+indexFile.getAbsolutePath());
//            boolIndexExists = true;
//        }
//        else 
        {
            System.out.println("Creating the index in: " + indexFile.getAbsolutePath());
            boolIndexExists = false;
            // +++++ setting the IndexWriterConfig
            IndexWriterConfig iwcfg = new IndexWriterConfig(analyzer);
            iwcfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            // ----- iwcfg set
            indexWriter = new IndexWriter(indexDir, iwcfg);
        }

        // +++ toStore or not
        toStore = "YES";
        // --- 

        // +++ storeTermVector or not
        if(prop.containsKey("storeTermVector")) {
            storeTermVector = prop.getProperty("storeTermVector", "YES");

            if(!storeTermVector.equals("YES")&&!storeTermVector.equals("NO")&&
                !storeTermVector.equals("WITH_POSITIONS")&&!storeTermVector.equals("WITH_OFFSETS")&&
                !storeTermVector.equals("WITH_POSITIONS_OFFSETS")) {
                System.err.println("prop file: storeTermVector=NO / YES(default)/ "
                    + "WITH_POSITIONS / WITH_OFFSETS / WITH_POSITIONS_OFFSETS "
                    + "(case-sensitive)");
                System.exit(1);
            }
        }
        else    // default value
            storeTermVector = "YES";
        // --- toStore or not

    }

    /**
     * Process the Wikipedia documents.
     * @param file 
     */
    private void processWikiFile(File file) throws IOException {

        try {

            articles = new WikiDocIterator(file);

            String article;
            while (articles.hasNext()) {
                if (articles != null) {
                    article = articles.next().toString();
                    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(article);
            //        jsoupDoc.select("ref").remove();
            //        System.out.println(doc.select(":not(ref)").text());
            //        char ch = (char) System.in.read();

                    for (Element input : jsoupDoc.select("doc")){
            //            /*
                        System.out.print(input.attr("id") + "\t");
            //            System.out.print(input.attr("url") + "\t");
                        System.out.println(input.attr("title") + "\t");
            //            System.out.println(input.text());
            //            char ch = (char) System.in.read();
                        //*/

                        Document doc = new Document();

                        docid = input.attr("id");
                        url = new URL(input.attr("url"));
                        title = input.attr("title");
                        content = input.text();
                        doc.add(new StringField(FIELD_ID, docid, Field.Store.YES));
                        doc.add(new StringField(FIELD_TITLE, title, Field.Store.YES));
                        doc.add(new StringField(FIELD_URL, url.toString(), Field.Store.YES));
                        doc.add(new Field(FIELD_CONTENT, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

                        System.out.println((++docIndexedCounter)+": Indexing doc: " + doc.getField(FIELD_ID).stringValue());
                        indexWriter.addDocument(doc);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Error: '"+file.getAbsolutePath()+"' not found");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("Error: IOException on reading '"+file.getAbsolutePath()+"'");
            ex.printStackTrace();
        }

    }

    /**
     * Process the directory containing the collection.
     * @param collDir File, of a directory containing the collection.
     */
    private void processDirectory(File collDir) throws IOException {

        File[] files = collDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file);  // recurse
            }
            else {
                processWikiFile(file);
            }
        }
    }

    /**
     * 
     * @throws Exception 
     */
    public void createIndex() throws Exception {

        System.out.println("Indexing started");

        if (boolIndexFromSpec) {
        /* if collectiomSpec is present, then index from the spec file */
            System.out.println("Reading collection file path from spec file at: "+collSpecPath);
            try (BufferedReader br = new BufferedReader(new FileReader(collSpecPath))) {
                String line;

                while ((line = br.readLine()) != null) {
                    //System.out.println(line);
                    // each line is a file containing documents
                    processWikiFile(new File(line));
                }
            }
        }
        else {
        /* index from collPath, i.e. the actual path of the root directory containing the collection */
            System.out.println("Reading collection considering collPath as root directory");
            if (collDir.isDirectory())
                processDirectory(collDir);
            else
                processWikiFile(collDir);
        }

        indexWriter.close();

        System.out.println("Indexing ends\n"+docIndexedCounter + " files indexed");
    }

    public static void main(String[] args) throws Exception {

        WikipediaIndexer wikiIndexer;

        String usage = "Usage: java Wt10gIndexer <init.properties>\n"
        + "Properties file must contain:\n"
        + "1. collSpec = path of the spec file containing the collection spec\n"
        + "2. indexPath = dir. path in which the index will be stored\n"
        + "3. stopFile = path of the stopword list file\n";

        // for debuging purpose
        /*
        args = new String[1];
//        args[0] = "/home/dwaipayan/Dropbox/programs/Wikipedia/init.properties";
        args[0] = "/home/dwaipayan/Dropbox/programs/Wikipedia/build/classes/wikipedia-indexer-1.properties";
        //*/
        if(args.length == 0) {
            System.out.println(usage);
            System.exit(1);
        }

        wikiIndexer = new WikipediaIndexer(args[0]);

        if(wikiIndexer.boolIndexExists == false) {
            wikiIndexer.createIndex();
            wikiIndexer.boolIndexExists = true;
        }
    }


}
