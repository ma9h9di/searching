import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.text.ParseException;

public class SearchMain {
    public static void main(String[] args) {
        try {
            university_quarry();
        } catch (IOException | ParseException | org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
    }

    private static void university_quarry() throws ParseException, org.apache.lucene.queryparser.classic.ParseException, IOException {
        for (int i = 0; i < PublicValue.university_quarry.length; i++) {
            String q = PublicValue.university_quarry[i];
            System.out.println("quarry : " + q);
            search(q);

        }
    }

    private static void search(String searchQuery) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        Searcher searcher = new Searcher(PublicValue.indexDir, Searcher.Mood.lm);
        long startTime = System.currentTimeMillis();

        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();

        System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("File: " + doc.get(PublicValue.FILE_PATH));
        }
        searcher.close();
    }
}
