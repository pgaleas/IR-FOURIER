package search;

import java.io.IOException;

import models.ResultRank;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import util.DBManager;


/**
 * 
 * Clase que maneja la b&uacute;squeda de los documentos.
 * 
 * @author Javier Fuentes (j.fuentes06@ufromail.cl)
 * @version 1.0
 * @since 1.0
 *
 */
public class Searcher {
	
	private Directory dir;
	private IndexSearcher searcher;
	private models.Query query;
	/**
	 * Constructor de la clase.
	 * 
	 * <p>Inicia la instancia de b&uacute;squeda, utiliza la base de datos ya creada, para rescatar los documentos.
	 * @param query
	 */
	public Searcher(models.Query query)
	{
		this.query = query;
		try {
			
			/**
			 * Configuracion del indice.
			 */
			dir = FSDirectory.open(DBManager.getIndexFile(query));
			searcher = new IndexSearcher(DirectoryReader.open(dir));
			
			/**
			 * Analisis del score de cada payload.
			 * Recibe como parametro la query para poder analizar los pesos de los terminos.
			 */
			searcher.setSimilarity(new PayloadSimilarity(query));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Metodo que calcula los primeros elemetos relevantes de la busqueda.
	 * 
	 * <p>Retorna una lista con la informacion relevante para la consulta.
	 * 
	 * 
	 * @return ResultRank que contiene los resultados de la busqueda.
	 * @throws IOException
	 */
	public ResultRank result() throws IOException
	{
		/**
		 * Seleccion de la consulta principal.
		 */
		TermQuery term = new TermQuery(new Term(Constants.CONTENTS, query.getQuery()));
		
		/**
		 * Consultas secundarias.
		 */
		//PayloadTermQuery term1 = new PayloadTermQuery(new Term("body", "chao"), new AveragePayloadFunction());
		//PayloadTermQuery term2= new PayloadTermQuery(new Term("body", "chao"), new AveragePayloadFunction());
		BooleanQuery bq = new BooleanQuery();
		bq.add(term, Occur.MUST);
		//bq.add(term1, Occur.SHOULD);
		//bq.add(term2, Occur.SHOULD);
		
		/**
		 * Obtencion de los documentos con mejor score.
		 */
		TopDocs docs = searcher.search(bq, 10);
		printResults(searcher, bq, docs);
		return null;
	}

	/**
	 * Imprime los resultados de la busqueda.
	 * 
	 * @param searcher
	 * @param query
	 * @param topDocs
	 * @throws IOException
	 */
	private void printResults(IndexSearcher searcher, Query query, TopDocs topDocs) throws IOException {
	
		System.out.println("-----------");
	
		System.out.println("Results for " + query + " of type: " + query.getClass().getName());
	
		for (int i = 0; i < topDocs.scoreDocs.length; i++) {
	
			ScoreDoc doc = topDocs.scoreDocs[i];
			System.out.println("----------->>>>>>>>>>>><<<<<<<<<<<--------------");
			System.out.println("Doc: " + doc.toString());
			System.out.println(searcher.doc(doc.doc).getField(Constants.DOCID).stringValue());
			System.out.println(searcher.doc(doc.doc).getField(Constants.TITLE).stringValue());
			System.out.println(searcher.doc(doc.doc).getField(Constants.SNIPPET).stringValue());
			System.out.println(searcher.doc(doc.doc).getField(Constants.URL).stringValue());
			//System.out.println(Arrays.toString(searcher.getIndexReader().document(doc.doc).getValues("body")));
			System.out.println();
			//System.out.println("Explain: " + searcher.explain(query, doc.doc));
		}
		
	}
}
