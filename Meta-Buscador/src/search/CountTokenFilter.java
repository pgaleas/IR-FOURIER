package search;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


/**
 * 
 * Clase de filtro para el conteo de la ubicac&oacute;n de las palabras en un TokenStrem
 * 
 * 
 * 
 * @author javier
 * @version 1.0
 * @since 1.0
 */
public class CountTokenFilter extends TokenFilter{
	
	private HashMap<String, LinkedList<Integer>> lista;
	private CharTermAttribute term;
	private int pos;
	
	/**
	 * Constructor de la clase.
	 * 
	 * @param input stream de entrada.
	 */
	protected CountTokenFilter(TokenStream input) {
		super(input);
		/**
		 * Acceso a los atributos de los tokens
		 */
		term = addAttribute(CharTermAttribute.class);
		lista = new HashMap<String, LinkedList<Integer>>();
		pos = 0;
	}

	
	
	/**
	 * 
	 * Genera un mapa de la ubicaci&oacute;n de cada token en el documento.
	 * 
	 * <p>Genera un {@link java.util.HashMap} con las ubicaciones de las palabras en el documento.
	 * 
	 * @throws IOException
	 */
	@Override
	public boolean incrementToken() throws IOException
	{
		if (!input.incrementToken()) return false;
		
		/**
		 * Obtencion de el termino correspondiente a cada token
		 */
		StringBuilder builder = new StringBuilder();
		builder.append(Arrays.copyOfRange(term.buffer(), 0, term.length()));
		
		
		/**
		 * Asocia un token a una lista de sus ubicaciones en el documento.
		 */
		if (lista.containsKey(builder.toString()))
		{
			lista.get(builder.toString()).add(pos);
		}
		else
		{
			lista.put(builder.toString(), new LinkedList<Integer>());
			lista.get(builder.toString()).add(pos);
		}
		pos++;
		return true;
	}
	
	
	/**
	 * Obtiene el mapa de las palabras dentro del documento.
	 * @return lista
	 */
	public HashMap<String, LinkedList<Integer>> getLista()
	{
		return lista;
	}

}
