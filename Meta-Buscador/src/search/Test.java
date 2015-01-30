package search;

import java.awt.TextField;
import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.MaxPayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import util.MathUtil;

public class Test {
	public Test() throws Exception
	{
		models.Query query = new models.Query("queue");
		String docs[] = {
			"An unbounded priority queue based on a priority heap. The elements of the priority queue are ordered according to their natural ordering, or by a Comparator provided at queue construction time, depending on which constructor is used. A priority queue does not permit null elements. A priority queue relying on natural ordering also does not permit insertion of non-comparable objects (doing so may result in ClassCastException).",
			"The head of this queue is the least element with respect to the specified ordering. If multiple elements are tied for least value, the head is one of those elements -- ties are broken arbitrarily. The queue retrieval operations poll, remove, peek, and element access the element at the head of the queue.",
			"This class and its iterator implement all of the optional methods of the Collection and Iterator interfaces. The Iterator provided in method iterator() is not guaranteed to traverse the elements of the priority queue in any particular order. If you need ordered traversal, consider using Arrays.sort(pq.toArray())."
		};
		
		Directory dir = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new PayloadAnalyzer(query,0));
		IndexWriter writer = new IndexWriter(dir, config);
		
		for (int i=0; i < docs.length; i++)
		{
			Document doc = new Document();
			
			doc.add((IndexableField) new org.apache.lucene.document.TextField("body", docs[i], Store.YES));
			
			/**
			 * Calculo del orden del documento.
			 */
			
			writer.addDocument(doc, new PayloadAnalyzer(query,MathUtil.ordenDocumento(i)));
			System.out.println();
			writer.commit();
		}
		
		writer.close();
		System.out.println(Arrays.toString(query.getValues()));
		System.out.println(Arrays.toString(query.getTerms()));
		IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
		searcher.setSimilarity(new PayloadSimilarity(query));
		
		TermQuery term1 = new TermQuery(new Term("body", query.getQuery()));
		PayloadTermQuery term2 = new PayloadTermQuery(new Term("body", "javier"), new MaxPayloadFunction());
		BooleanQuery q = new BooleanQuery();
		q.add(term1,Occur.MUST);
		q.add(term2, Occur.SHOULD);
		
		TopDocs topDocs = searcher.search(q, 10);
		
		for (int i=0; i < topDocs.scoreDocs.length; i++)
		{
			System.out.println(searcher.doc(topDocs.scoreDocs[i].doc).getField("body"));
			System.out.println(topDocs.scoreDocs[i].score);
			System.out.println("------------------------------------------------------");
		}
		
	}
}
