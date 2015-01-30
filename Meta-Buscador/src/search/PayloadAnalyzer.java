package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

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
	private float docValue;
	
	/**
	 * Constructor de la clase.
	 * @param query consulta.
	 */
	public PayloadAnalyzer(models.Query query, float doc) {
		this.query = query;
		this.docValue = doc;
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
		filter = new StopFilter(filter, getStopWords());
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
		filter = new StopFilter(filter, getStopWords());
		filter = new PayloadFilter(filter, lista, query, docValue);
		
		return new TokenStreamComponents(source, filter);
	}
	
	/**
	 * Obtiene la lista de stopwords
	 */
	@SuppressWarnings("resource")
	private CharArraySet getStopWords()
	{
		CharArraySet set = new CharArraySet(1000, true);
		File f = new File(Constants.PATH+"stop.txt");
		Scanner s;
		try {
			s = new Scanner(f);
			while (s.hasNextLine())
			{
				set.add(s.nextLine());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return set;
	}
}
