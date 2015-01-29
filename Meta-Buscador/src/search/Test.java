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

public class Test {
	public Test() throws Exception
	{
		models.Query query = new models.Query("javier");
		String docs[] = {
			"1 Lorem ipsum javier ad his scripta blandit partiendo, eum fastidii accumsan euripidis in, eum liber hendrerit an. Qui ut wisi vocibus suscipiantur, quo dicit ridens inciderint id. Quo mundi lobortis reformidans eu, legimus senserit definiebas an eos. Eu sit tincidunt incorrupte definitionem, vis mutat affert percipit cu, eirmod consectetuer signiferumque eu per. In usu latine equidem dolores. Quo no falli viris intellegam, ut fugit veritus placerat per",
			"2 Lorem ipsum ad his scripta blandit partiendo, eum fastidii accumsan javier euripidis in, eum liber hendrerit an. Qui ut wisi vocibus suscipiantur, quo dicit ridens inciderint id. Quo mundi lobortis reformidans eu, legimus senserit definiebas an eos. Eu sit tincidunt incorrupte definitionem, vis mutat affert percipit cu, eirmod consectetuer signiferumque eu per. In usu latine equidem dolores. Quo no falli viris intellegam, ut fugit veritus placerat per",
			"3 Lorem ipsum ad his scripta blandit partiendo, eum fastidii accumsan euripidis in, eum liber hendrerit an. Qui ut wisi vocibus suscipiantur, quo dicit ridens inciderint id. Quo mundi lobortis reformidans eu, legimus senserit definiebas an eos. Eu sit tincidunt incorrupte definitionem, vis mutat affert javier percipit cu, eirmod consectetuer signiferumque eu per. In usu latine equidem dolores. Quo no falli viris intellegam, ut fugit veritus placerat per",
			"4 Lorem ipsum ad his scripta blandit partiendo, eum fastidii accumsan euripidis in, eum liber hendrerit an. Qui ut wisi vocibus suscipiantur, quo dicit ridens inciderint id. Quo mundi lobortis reformidans eu, legimus senserit definiebas an eos. Eu sit tincidunt incorrupte definitionem, vis mutat affert percipit cu, eirmod consectetuer signiferumque eu per. In usu latine equidem dolores. Quo no falli viris intellegam, ut fugit veritus placerat per javier"
		};
		
		Directory dir = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new PayloadAnalyzer(query,0));
		IndexWriter writer = new IndexWriter(dir, config);
		
		query.setTerms(new String[9]);
		query.setValues(new float[9]);
		
		for (int i=0; i < 9; i++)
		{
			query.getTerms()[i] = "";
			query.getValues()[i] = 0.0F;
		}
		
		for (int i=0; i < docs.length; i++)
		{
			Document doc = new Document();
			
			doc.add((IndexableField) new org.apache.lucene.document.TextField("body", docs[i], Store.YES));
			
			/**
			 * Calculo del orden del documento.
			 */
			float value =(float)(-Math.pow((double)i, 2.0)/Math.pow(4.0, 2.0) + 1.F);
			System.out.println("value:" + value);
			writer.addDocument(doc, new PayloadAnalyzer(query,value));
			System.out.println();
			System.out.println(Arrays.toString(query.getTerms()));
			System.out.println(Arrays.toString(query.getValues()));
			System.out.println("---------");
			writer.commit();
		}
		
		writer.close();
		
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
