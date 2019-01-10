/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

/**
 *
 * @author dwaipayan
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CallWikipediaAPI {

    private String BaseUrl;
    private String ResponderUrl;
    private String responderParameters;
    PrintWriter pw;

    /*
     *Constructor sets BaseUrl, ResponderUrl and ResponderParameters properties
     */

    // Example API call
    public CallWikipediaAPI(String lang, String outputFilename) throws FileNotFoundException, IOException {
        this.BaseUrl = "https://"+lang+".wikipedia.org/w/";
        this.ResponderUrl = "api.php?";
        pw = new PrintWriter(new File(outputFilename));
    }

    public void setSearchPageids(String pageids) {
        this.responderParameters = "action=query&format=xml&prop=pageprops&pageids="+pageids;
    }

//    public CallWikipediaAPI() {
//        this.BaseUrl = "https://hi.wikipedia.org/w/";
////        this.ResponderUrl = "api.php?action=query&format=xml&prop=pageprops&pageids=180029|180030|180032|180034|180035";
//        this.ResponderUrl = "api.php?";
//        this.responderParameters = "action=query&format=xml&prop=pageprops&pageids=180029|180030|180032|180034|180035";
//        pw = new BufferedWriter(new OutputStreamWriter(System.out));
//    }
//
//    public CallWikipediaAPI(String lang, String pageids) {
//        this.BaseUrl = "https://"+lang+".wikipedia.org/w/";
//        this.ResponderUrl = "api.php?";
//        this.responderParameters = "action=query&format=xml&prop=pageprops&pageids="+pageids;
//        pw = new BufferedWriter(new OutputStreamWriter(System.out));
//    }
//
//    public CallWikipediaAPI(String lang, String pageids, File outFile) throws FileNotFoundException {
//        this.BaseUrl = "https://"+lang+".wikipedia.org/w/";
//        this.ResponderUrl = "api.php?";
//        this.responderParameters = "action=query&format=xml&prop=pageprops&pageids="+pageids;
//        pw = new BufferedWriter(new PrintWriter(outFile));
//    }
//

    public void ApiResponder() throws MalformedURLException, IOException {
        /*
         * Create a new HTTP Connection request to responder, pass along Session_ID Cookie
         */
        HttpURLConnection httpcon = (HttpURLConnection) ((new URL(this.BaseUrl+this.ResponderUrl).openConnection()));
        httpcon.setDoOutput(true);
        httpcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpcon.setRequestProperty("Accept", "application/json");
//        httpcon.setRequestProperty("Cookie", cookie);
        httpcon.setRequestMethod("POST");
        httpcon.connect();

        byte[] outputBytes = responderParameters.getBytes("UTF-8");
        OutputStream os = httpcon.getOutputStream();
        os.write(outputBytes);
        os.close();

        /*
         * Read/Output response from server
         */
        BufferedReader inreader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
        String decodedString;
        while ((decodedString = inreader.readLine()) != null) {
//            System.out.println(decodedString);
            selectTagAttribute(decodedString);
        }

        inreader.close();
        httpcon.disconnect();
    }

    public static String returnAllPageids(File fin) throws IOException {

        String pageids = "";
        FileInputStream fis = new FileInputStream(fin);

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 
        String line = null;
        while ((line = br.readLine()) != null) {
            String pageid = line.split("\t")[0];
            if(pageids.isEmpty())
                pageids = pageid;
            else
                pageids += "|"+ pageid;
//            System.out.println(line);
        }
        br.close();
        return pageids;
    }

    public void selectTagAttribute(String htmlFile) throws IOException {

        Document doc = Jsoup.parse(htmlFile);//, "UTF-8", "http://example.com/");

        for (Element input : doc.select("page")){
            System.out.print(input.attr("pageid") + "\t");
            System.out.print(input.attr("title") + "\t");

            pw.print(input.attr("pageid") + "\t" + 
                    input.attr("title") + "\t");

            for (Element input2 : input.select("pageprops")) {
                System.out.print(input2.attr("wikibase_item"));
                pw.print(input2.attr("wikibase_item"));
            }
            System.out.println("");
            pw.println("");
        }
//        char ch = (char) System.in.read();
    }

    public static void main(String[] args) throws Exception {

        CallWikipediaAPI api;
        String lang;
        String outputFile;

        // /*
        if(args.length < 3 || args.length > 4) {
            System.out.println("Usage: java indexer.CallWikipediaAPI <language> <tsv-file, first-column containing wikipedia-pageids> "
                    + "<output-file-path>");
            System.exit(0);
        }
        // */

        /*
        args = new String[3];
        args[0] = "en";
        args[1] =  "/home/dwaipayan/collections/wikipedia-stats/en/en-missing-torun.tsv";
        args[2] =  "/home/dwaipayan/collections/wikipedia-stats/en/en-missing-torun.tsv.out";
        // */
//        pageids = returnAllPageids(new File(args[1]));
//        System.out.println(pageids);

        lang = args[0];

        FileInputStream fis = new FileInputStream(new File(args[1]));

        outputFile = args[2];
        
        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 
        api = new CallWikipediaAPI(lang, outputFile);

        String line = null;
        String pageids = "";
        int count = 0;
        while ((line = br.readLine()) != null) {

//            System.out.println(line);
            String pageid = line.split("\t")[0];
            if(pageids.isEmpty())
                pageids = pageid;
            else
                pageids += "|"+ pageid;
            count ++;

            if(count == 50) {
                api.setSearchPageids(pageids);
                api.ApiResponder();
                pageids = "";
                count = 0;
            }
        }
        if(!pageids.isEmpty()){
            api.setSearchPageids(pageids);
            api.ApiResponder();            
        }

        api.pw.close();
        br.close();
    }
}