package search;

import java.io.IOException;

import models.Page;
import models.Query;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import util.DBManager;
import util.MathUtil;


/**
 * 
 * Clase de indexaci&oacute;n de las p&aacute;ginas.
 * 
 * @author javier
 * @version 1.0
 * @since 1.0
 */
public class Indexer {

	private IndexWriter writer;
	private IndexWriterConfig config;
	private Query query;
	private Directory indexDir;
	
	/**
	 * Constructor de la clase.
	 * 
	 * <p>El constructor recibe como parametro una {@link models.Query} que permite generar o buscar un &iacute;ndice
	 * asociado a esa consulta. Se genera un indice para cada consulta.
	 * 
	 * @param query
	 */
	public Indexer(Query query)
	{
		this.query = query;
		try {
			/**
			 * Configuracion del indice.
			 * Las versiones son exclusivas de cada indice.
			 * Para un indice generado con la version 4.10.3 no sera compatible con una version distinta.
			 */
			this.indexDir = FSDirectory.open(DBManager.getIndexFile(query));
			
			/**
			 * La version actual usada es 4.10.3
			 */
			this.config = new IndexWriterConfig(Version.LATEST, new PayloadAnalyzer(query,0));
			this.writer = new IndexWriter(indexDir, config);
			
			/**
			 * Indexacion de las paginas al indice de lucene.
			 */
			this.indexPages();
			System.out.println(writer.numDocs());
			this.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cierra la conexi&oacute;n al &iacute;ndice.
	 * 
	 * 
	 * @throws IOException
	 * 
	 */
	public void close() throws IOException
	{
		writer.close();
	}

	
	/**
	 * Ingresa los contenidos de las p&aacute;ginas asociadas a la consulta al &iacute;ndice.
	 * 
	 * Solo se almacenan las p&aacute;ginas no nulas ni vac&iacute;as
	 * 
	 * @throws IOException
	 */
	
	
	private void indexPages() throws IOException 
	{
		Page[] pages = DBManager.getPagesFromQuery(this.query);
		
		/**
		 * Inicializacion en 0 de las listas de terminos.
		 * Necesario para calcular el grafo de terminos relacionados.
		 */
	
		query.setTerms(new String[Constants.MAX_TERMS_SIZE]);
		query.setValues(new float[Constants.MAX_TERMS_SIZE]);
		for (int i=0; i < Constants.MAX_TERMS_SIZE; i++)
		{
			query.getTerms()[i] = "";
			query.getValues()[i] = 0.0F;
		}
	
		for (int i=0; i < pages.length; i++)
		{
			System.out.println(pages[i]);
			
			/**
			 * Evitamos documentos nulos o vacios.
			 */
			if (pages[i].getContent() != "")
				if (pages[i].getContent() != null)
				{
					Document doc = getDocument(pages[i]);
					doc.add(new IntField(Constants.DOCID, i, Field.Store.YES));
					
					//Es necesario usar un nuevo PayloadAnalyzer para cada documento.
					writer.addDocument(doc, new PayloadAnalyzer(query, MathUtil.ordenDocumento(i)));
					
					// Fundamental despues de haber incluido un documento.
					writer.commit();
				}
		}
	}

	private Document getDocument(Page p) throws IOException
	{
		Document document = new Document();
		IndexableField content = (IndexableField) 
				new org.apache.lucene.document.TextField(Constants.CONTENTS, p.getContent(), Field.Store.YES);
		IndexableField url = (IndexableField) 
				new org.apache.lucene.document.StringField(Constants.URL, p.getUrl(), Field.Store.YES);
		IndexableField title = (IndexableField) 
				new org.apache.lucene.document.StringField(Constants.TITLE, p.getTitle(), Field.Store.YES);
		IndexableField snippet = (IndexableField) 
				new org.apache.lucene.document.StringField(Constants.SNIPPET, p.getSnippet(), Field.Store.YES);

		document.add(content);
		document.add(url);
		document.add(title);
		document.add(snippet);
		return document;
	}
}