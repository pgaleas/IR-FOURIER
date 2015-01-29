package search;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

/**
 * 
 * Clase que analiza el strem de tokens.
 * 
 *  <p>Esta clase permite analizar y generar filtros para stopwords, stemming y
 *   conteo de palabras, {@link search.CountTokenFilter}, dentro del documento.
 * 
 * @author Javier Fuentes (j.fuentes06@ufromail.cl)
 * @version 1.0
 * @since 1.0
 *
 */
public class PayloadAnalyzer extends Analyzer{
	private models.Query query;
	private float doc;
	
	/**
	 * Constructor de la clase.
	 * @param query consulta.
	 */
	public PayloadAnalyzer(models.Query query, float doc) {
		this.query = query;
		this.doc = doc;
	}
	
	/**
	 * Metodo que realiza el analisis token por token
	 * 
	 */
	@Override
	protected TokenStreamComponents createComponents(String field, Reader reader) {
		TokenFilter filter;
		Tokenizer source;
		
		/**
		 * Se obtiene una copia del stream original (reader).
		 */
		StringBuilder builder = new StringBuilder();
		char[] charBuffer = new char[2048];
		try {
			int i = 0;
			while ((i = reader.read(charBuffer)) != -1)
			{
				builder.append(Arrays.copyOfRange(charBuffer, 0, i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		/**
		 * Se generan dos streams para duplicar el analisis.
		 */
		Reader r1 = new StringReader(builder.toString());
		Reader r2 = new StringReader(builder.toString());
		
		/**
		 * En el primer analisis se obtienen las posiciones de las palabras en el documento en general.
		 * Se obtiene una vision completa.
		 */
		source = new WhitespaceTokenizer(r1);
		filter = new LowerCaseFilter(source);
		//filter = new StopFilter(filter, stopWords);
		/**
		 * CountTokenFilter realiza el conteo de palabras y sus posiciones en tiempo de indexacion,
		 * para luego procesar esa informacion y almacenarla en un payload para cada token.
		 */
		filter = new CountTokenFilter(filter);
		
		/**
		 * Se recorre todo el stream, para ejecutar el analizador.
		 */
		try {
			filter.reset();
			while (filter.incrementToken());
			filter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		/**
		 * Se obitiene la lista con el mapa de las palabras y sus ubicaciones en el documento.
		 */
		CountTokenFilter aux = (CountTokenFilter) filter;
		HashMap<String, LinkedList<Integer>> lista = aux.getLista();
		
		/**
		 * Luego se usa el 2do stream para realizar nuevamente la misma operacion,
		 *  pero ahora con los datos ya obtenidos del analisis,
		 *  permitiendo hacer un segundo analisis y almacenamiento en su respectivo payload 
		 */
		source = new WhitespaceTokenizer(r2);
		filter = new LowerCaseFilter(source);
		//filter = new StopFilter(filter, stopWords);
		filter = new PayloadFilter(filter, lista, query);
		
		return new TokenStreamComponents(source, filter);
	}

}
