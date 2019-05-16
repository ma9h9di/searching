import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    private IndexWriter writer;

    Indexer(String indexDirectoryPath) throws IOException {

        Directory dir = FSDirectory.open(new File(indexDirectoryPath).toPath());

// Analyzer specifies options for text processing
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
//                // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());
                // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                ts = new TokenStreamComponents(ts.getTokenizer(), new LowerCaseFilter(ts.getTokenStream()));
                // Step 3: whether to remove stop words
                // Uncomment the following line to remove stop words
                ts = new TokenStreamComponents(ts.getTokenizer(), new StopFilter(ts.getTokenStream(), StandardAnalyzer.ENGLISH_STOP_WORDS_SET));
                // Step 4: whether to apply stemming
                // Uncomment the following line to apply Krovetz or Porter stemmer
//                 ts = new TokenStreamComponents( ts.getTokenizer(), new KStemFilter( ts.getTokenStream() ) );
//                 ts = new TokenStreamComponents( ts.getTokenizer(), new PorterStemFilter( ts.getTokenStream() ) );
                return ts;
            }
        };

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
// Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        writer = new IndexWriter(dir, config);
//         writer.close();
//         dir.close();


    }


    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }

    private Document getDocument(File file) throws IOException {
        Document document = new Document();

        FieldType fieldTypeText = new FieldType();
        fieldTypeText.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        fieldTypeText.setStoreTermVectors(true);
        fieldTypeText.setStoreTermVectorPositions(true);
        fieldTypeText.setTokenized(true);
        fieldTypeText.setStored(true);
        fieldTypeText.freeze();

        String txt;

        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            txt = String.valueOf(sb);
        } finally {
            br.close();
        }


        //index file contents
        Field contentField = new Field(PublicValue.CONTENTS, txt, fieldTypeText);

        //index file name
        Field fileNameField = new Field(PublicValue.FILE_NAME, file.getName(), fieldTypeText);
        //index file path
        Field filePathField = new Field(PublicValue.FILE_PATH, file.getCanonicalPath(), fieldTypeText);


        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);

        return document;
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing " + file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    int createIndex(String dataDirPath, FileFilter filter)
            throws IOException {
        //get all files in the data directory
        File mFile = new File(dataDirPath);
        File[] files = new File(dataDirPath).listFiles();

        for (File file : files) {
            if (!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                    && filter.accept(file)
                    ) {
                indexFile(file);
            }
        }
        return writer.numDocs();
    }
}