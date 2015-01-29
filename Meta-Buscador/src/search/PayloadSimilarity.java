package search;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.BytesRef;


/**
 * 
 * Clase que maneja el boosting a las palabras con payload.
 * 
 * 
 * <p>Permite el analisis de sus respectivos payloads, para las palabras relevantes, dentro del documento.
 * 
 * @author Javier Fuentes (j.fuentes06@ufromail.cl)
 * @version 1.0
 * @since 1.0
 *
 */
public class PayloadSimilarity extends DefaultSimilarity{
	private models.Query query;
	
	/**
	 * Constructor de la clase.
	 * @param query
	 */
	public PayloadSimilarity(models.Query query) {
		this.query = query;
	}
	
	
	/**
	 * Metodo que calcula el score para un payload.
	 * 
	 * @return score para un payload
	 */
	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		
		/**
		 * Obtencion de los bytes separando por trozos, se convierten a enteros (puede ser cualquier tipo de dato).
		 */
		
		
		byte[] bytes = Arrays.copyOfRange(payload.bytes, 0, 8);
		double similitud = ByteBuffer.wrap(bytes).getDouble();
		
		bytes = Arrays.copyOfRange(payload.bytes, 8, 12);
		int stringSize = ByteBuffer.wrap(bytes).getInt();
		
		bytes = Arrays.copyOfRange(payload.bytes, 12, 12 + stringSize);
		StringBuilder builder = new StringBuilder();
		for (int i=0; i < stringSize; i++)
		{
			builder.append((char)(bytes[i]));
		}
		
		int increment = 0;
		
		if ("javier".equals(builder.toString()))
		{
			increment = 1;
		}
		
		/**
		 * Valor retornado incrementa el boosting de un documento.
		 */
		//uso de las querys para obtener los pesos de los terminos relevantes.
		System.out.println(similitud);
		return (float)((similitud)*increment);
	}
}
