import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
//import org.apache.lucene.queryParser.ParseException;
//import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.queryParser.QueryParser;
//import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    IndexSearcher indexSearcher;
    QueryParser queryParser;
    Query query;
    Mood mood;


    enum Mood {
        lm, tf
    }

    public Searcher(String indexDirectoryPath, Mood mood)
            throws IOException {
        this.mood = mood;
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());
                // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                ts = new TokenStreamComponents(ts.getTokenizer(), new LowerCaseFilter(ts.getTokenStream()));
                // Step 3: whether to remove stop words
                // Uncomment the following line to remove stop words
                ts = new TokenStreamComponents(ts.getTokenizer(), new StopFilter(ts.getTokenStream(), StandardAnalyzer.ENGLISH_STOP_WORDS_SET));
                // Step 4: whether to apply stemming
                // Uncomment the following line to apply Krovetz or Porter stemmer
                // ts = new TokenStreamComponents( ts.getTokenizer(), new KStemFilter( ts.getTokenStream() ) );
                // ts = new TokenStreamComponents( ts.getTokenizer(), new PorterStemFilter( ts.getTokenStream() ) );
                return ts;
            }
        };


        Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());

        IndexReader index = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(index);

        queryParser = new QueryParser(PublicValue.CONTENTS, analyzer);
    }

    public TopDocs search(String searchQuery)
            throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        query = queryParser.parse(searchQuery);
        switch (mood) {
            case lm:
                indexSearcher.setSimilarity(new LMDirichletSimilarity());
                break;
            case tf:
                indexSearcher.setSimilarity(new ClassicSimilarity());
                break;
        }
        return indexSearcher.search(query, PublicValue.MAX_SEARCH_INDEX);
    }

    public Document getDocument(ScoreDoc scoreDoc)
            throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

    public void close() throws IOException {

    }
}