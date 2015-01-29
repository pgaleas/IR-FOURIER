package search;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import math.Fourier;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

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
	private Fourier fourier;
	private double[] queryCoef;
	private models.Query query;
	
	/**
	 * Aumenta el tamaño del documento (al comienzo de este),
	 *  para evitar los efectos que produce la funcion de Fourier en 
	 *  los bordes del documento por la periodicidad de las funciones seno y coseno.
	 */
	private int largo_extra_doc;

	/**
	 * Constructor de la clase
	 * <p> Permite inicializar el filtro, recibiendo la lista de palabras con sus ubicaciones por documento.
	 * @param input
	 * @param lista
	 */
	protected PayloadFilter(TokenStream input, HashMap<String, LinkedList<Integer>> lista, models.Query query) {
		super(input);
		this.lista = lista;
		term = addAttribute(CharTermAttribute.class);
		payload = addAttribute(PayloadAttribute.class);
		fourier = new Fourier();
		this.query = query;
		this.query.setTerms(new String[9]);
		this.query.setValues(new float[9]);
		
		
		for (int i=0; i < 9; i++)
		{
			query.getTerms()[i] = "";
			query.getValues()[i] = 0.0F;
		}
		/**
		 * Calculo del tamaño del doc.
		 */
		Collection<LinkedList<Integer>> col = lista.values();
		Iterator<LinkedList<Integer>> iter = col.iterator();
		int cont = 0;
		while (iter.hasNext())
		{
			cont += iter.next().size();
		}
		
		/**
		 * Calcula el largo extra del documento.
		 */
		largo_extra_doc = (int)(cont*Constants.PORCENTAJE_LARGO_EXTRA_DOCUMENTO);
		
		fourier.setScale(cont + largo_extra_doc);
		fourier.setMaxOrder(6);
		this.queryCoef = this.getCoefs(query.getQuery());
		
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
		 * Calculo similitud.
		 */
		double[] coefs = this.getCoefs(builder.toString());
		double similitud = this.calculoSimilitud(queryCoef, coefs);
		
		//System.out.print(builder.toString()+"|"+similitud + ", ");
		
		similitud = similitud*1;
		/**
		 * Generacion de los terminos mas cercanos,
		 * Almacenados dentro de la query.
		 */
		if (!builder.toString().equals(query.getQuery()))
		{
			int min = 0;
			float mindiff = 5.0F;
			for (int i=0; i < query.getTerms().length; i++)
			{
				float diff = Math.abs(query.getValues()[i] - (float)similitud);
				if (diff < mindiff)
					continue;
				
				if (query.getValues()[i] < (float)similitud)
				{
					min = i;
				}
				
			}
			if (similitud > 0){
				query.getValues()[min] = (float)similitud;
				query.getTerms()[min] = builder.toString();
			}
		}
		
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
		
		return true;
	}
	
	/**
	 * 
	 * Calculo de similitud
	 * @param c1 coeficientes 
	 * @param c2 coeficientes 
	 * @return double similitud
	 */
	private double calculoSimilitud(double[] c1, double[] c2)
	{
		if (c1.length == c2.length)
		{
			double out = 0.0;
			double auxC1 = 0.0;
			double auxC2 = 0.0;
			for (int i=0; i < c2.length; i++)
			{
				out += c1[i]*c2[i];
				auxC1 += c1[i]*c1[i];
				auxC2 += c2[i]*c2[i];
			}
			
			out = out / (auxC1*auxC2);
			
			return out;
		}
		else
		{
			throw new RuntimeException("Largo de coeficientes distinto");
		}
	}
	
	/**
	 * 
	 * Obtencion de los coefs. 
	 * 
	 * @param query
	 * @return coeficientes
	 */
	private double[] getCoefs(String query)
	{
		/**
		 * Obtencion de las ubicaciones de la palabra en el documento
		 */
		Integer[] wordPositionsInteger = this.lista.get(query).toArray(new Integer[0]);
		
		/**
		 * Casting de Integer[] a int[]
		 */
		int[] wordPositions = new int[wordPositionsInteger.length];
		for (int i=0; i < wordPositions.length; i++)
		{
			wordPositions[i] = (int)wordPositionsInteger[i] + largo_extra_doc;
		}
		
		/**
		 * Calculo de los coeficientes.
		 */
		this.fourier.setWordPositions(wordPositions);
		
		return fourier.calculateCoeffs();
		
	}
}
