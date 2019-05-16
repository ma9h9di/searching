import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.document.Document;
//import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class IndexMain {
    static Indexer indexer ;

    public static void main(String[] args) {
        try {
            indexer = new Indexer(PublicValue.dataDir);
            createIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void createIndex() throws IOException {
        indexer = new Indexer(PublicValue.indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(PublicValue.dataDir, new HTMLFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File indexed, time taken: " +(endTime-startTime)+" ms");
    }
}