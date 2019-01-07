/**
 * For Wikipedia data.
 */

package indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author dwaipayan
 */
public class WikiDocIterator implements Iterator<StringBuffer> {

    protected BufferedReader docReader; // document reader
    protected boolean at_eof = false;   // whether at the end of file or not

    public WikiDocIterator(File file) throws FileNotFoundException {
        docReader = new BufferedReader(new FileReader(file));
        at_eof = false;
    }

    public void closeFileAfterReading() throws IOException {
        docReader.close();
        at_eof = true;
    }

    @Override
    public boolean hasNext() {
        return !at_eof;
    }

    /**
     * Returns the next document in the collection, setting FIELD_ID, FIELD_BOW, and FIELD_FULL_BOW.
     * @return 
     */
    @Override
    public StringBuffer next() {

        StringBuffer doc = new StringBuffer();

        try {
            String line;

            boolean in_doc = false;

            while (true) {
                line = docReader.readLine();

                if (line == null) {
                // EOF read
                    at_eof = true;
                    break;
                }
                else if (line.isEmpty())
                // Empty line read
                    continue;       // read next line

                // +++ <DOC>
                if (!in_doc) {
                    if (line.toLowerCase().startsWith("<doc")) {
                        in_doc = true;
                    }
//                    continue;
                }
                if (line.toLowerCase().contains("</doc>")) {
                // Document ends
                    if(in_doc) {
                        doc.append(line);
                        in_doc = false;
                    }
                    break;
                }
                // --- </DOC>

                doc.append(line).append(" ");
            } // ends while; a document is read.

        } catch (IOException e) {
            doc = null;
        }
        return doc;
    } // end next()

    @Override
    public void remove() {
    }
}
