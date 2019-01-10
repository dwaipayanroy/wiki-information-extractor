
import java.io.IOException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dwaipayan dwaipayan.roy@gmail.com
 */
public class WikiInfobox {
    public static void main(String[] args) throws IOException {
 Response res = Jsoup.connect("http://en.wikipedia.org/wiki/Carbon").execute();

    String html = res.body();

    Document doc = Jsoup.parseBodyFragment(html);
    Element body = doc.body();
    Elements tables = body.getElementsByTag("table");// hasClass("infobox bordered");

    for (Element table : tables) {

        if (table.className().equalsIgnoreCase("infobox bordered")) {
            System.out.println(table.outerHtml());
            break;
        }
    }
    }
}
