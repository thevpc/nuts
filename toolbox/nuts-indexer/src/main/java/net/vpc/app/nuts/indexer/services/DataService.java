package net.vpc.app.nuts.indexer.services;

import net.vpc.app.nuts.indexer.NutsIndexerUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class DataService {

    public void indexData(Path dirPath, Map<String, String> data) {
        this.indexMultipleData(dirPath, Collections.singletonList(data));
    }

    public void indexMultipleData(Path dirPath, List<Map<String, String>> data) {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = null;
        IndexWriterConfig config = null;
        IndexWriter writer = null;
        try {
            index = FSDirectory.open(dirPath);
            config = new IndexWriterConfig(analyzer);
            writer = new IndexWriter(index, config);
            for (Map<String, String> entity : data) {
                Document document = new Document();
                for (Map.Entry<String, String> entry : entity.entrySet()) {
                    document.add(new StringField(entry.getKey(), NutsIndexerUtils.trim(entry.getValue()), Field.Store.YES));
                }
                try {
                    writer.addDocument(document);
                }catch (IllegalArgumentException e){
                    throw e;
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteData(Path dirPath, Map<String, String> data) {
        this.deleteMultipleData(dirPath, Collections.singletonList(data));
    }

    public void deleteMultipleData(Path dirPath, List<Map<String, String>> data) {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = null;
        IndexWriterConfig config = null;
        IndexWriter writer = null;
        try {
            index = FSDirectory.open(dirPath);
            config = new IndexWriterConfig(analyzer);
            writer = new IndexWriter(index, config);
            for (Map<String, String> entity : data) {
                Query query = NutsIndexerUtils.mapToQuery(entity);
                writer.deleteDocuments(query);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Map<String, Object>> getAllData(Path dirPath) {
        return searchData(dirPath, null);
    }

    public List<Map<String, Object>> searchData(Path dirPath, Map<String, String> data) {
        Directory index = null;
        IndexReader reader = null;
        IndexSearcher searcher = null;
        TopDocs topDocs = null;
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            index = FSDirectory.open(dirPath);
            reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);
            topDocs = searcher.search(data == null
                            ? new MatchAllDocsQuery()
                            : NutsIndexerUtils.mapToQuery(data),
                    Integer.MAX_VALUE);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = null;
                HashMap<String, Object> res = new HashMap<>();
                document = searcher.doc(scoreDoc.doc);
                for (IndexableField field : document.getFields()) {
                    res.put(field.name(), field.stringValue());
                }
                result.add(res);
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
        return result;
    }
}
