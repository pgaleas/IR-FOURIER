package search;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

import util.MathUtil;
import util.PriorityQueue;

/**
 * 
 * Clase que maneja el analisis de los tokens para ser almacenado en sus payloads.
 * 
 * @author Javier Fuentes (j.fuentes06@ufromail.cl)
 * @version 1.0
 * @since 1.0
 *
 */
public class PayloadFilter extends TokenFilter{

	private HashMap<String, LinkedList<Integer>> lista;
	private PayloadAttribute payload;
	private CharTermAttribute term;
	private models.Query query;
	private float docValue;
	private int pos;
	private PriorityQueue queue;
	
	/**
	 * Constructor de la clase
	 * <p> Permite inicializar el filtro, recibiendo la lista de palabras con sus ubicaciones por documento.
	 * @param input
	 * @param lista
	 */
	protected PayloadFilter(TokenStream input, HashMap<String, LinkedList<Integer>> lista, models.Query query, float docValue) {
		super(input);
		this.lista = lista;
		term = addAttribute(CharTermAttribute.class);
		payload = addAttribute(PayloadAttribute.class);
		this.query = query;
		this.docValue = docValue;
		//Conteo de palabras
		this.pos = 0;
		this.queue = new PriorityQueue(query, 15);
	}
	
	/**
	 * Filtro donde se analiza cada token, y se obtiene su ubicacion dentro del documento.
	 */
	@Override
	public boolean incrementToken() throws IOException {
		if (!input.incrementToken()) return false;
		
		/**
		 * String del token actual.
		 */
		StringBuilder builder = new StringBuilder();
		builder.append(Arrays.copyOfRange(term.buffer(), 0, term.length()));
		
		
		/**
		 * Calculo de distancia hacia la query.
		 */
		float similitud = 0.0F;
		Iterator<Integer> iter = lista.get(query.getQuery()).iterator();
		while (iter.hasNext())
		{
			int p = iter.next();
			float aux = MathUtil.distancia(pos, p);
			if (similitud < aux)
				similitud = aux;
		}
		/**
		 * Reduccion dada por el ranking.
		 * docValue = 1 -> 0
		 */
		similitud*=docValue;
		/*
		if (builder.toString().equals(query.getQuery()))
		{
			System.out.print(builder.toString().toUpperCase()+"|"+similitud+" ");
		}
		else
		{
			System.out.print(builder.toString()+"|"+similitud+" ");
		}
		*/
		if (builder.toString().equals(query.getQuery())) return true;
		queue.add(builder.toString(), similitud);
		
		/**
		 * El orden inicial de los documentos tiene relevancia para la seleccion de los terminos cercanos a la query.
		 */
		
		
		
		/**
		 * Conversion a bytes
		 */
		byte[] bytesSimilitud = ByteBuffer.allocate(8).putDouble(similitud).array();
		byte[] stringSize = ByteBuffer.allocate(4).putInt(builder.toString().length()).array();
		byte[] string = builder.toString().getBytes();
		
		
		byte[] bytes = new byte[bytesSimilitud.length + stringSize.length + string.length];
		
		System.arraycopy(bytesSimilitud, 0, bytes, 0, 8);
		System.arraycopy(stringSize, 0, bytes, 8, 4);
		System.arraycopy(string, 0, bytes, 12, string.length);

		
		/**
		 * Formato de almacenamiento en payload.
		 */
		BytesRef br = new BytesRef(bytes);
		payload.setPayload(br);
		pos++;
		return true;
	}
}
