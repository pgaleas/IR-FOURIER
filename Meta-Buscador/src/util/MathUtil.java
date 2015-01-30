package util;

import search.Constants;


/**
 * Clase con funciones matematicas
 * 
 * 
 * @author Javier Fuentes (j.fuentes06@ufromail.cl
 * @version 1.0
 * @since 1.0
 *
 */
public class MathUtil {
	
	/**
	 * Calcula la distancia entre dos puntos.
	 * 
	 * 
	 * @param pos1 posicion 1
	 * @param pos2 posicion 2
	 * @return distancia normalizada entre 1 a 0
	 */
	public static float distancia(int pos1, int pos2)
	{
		int distancia = pos1 - pos2;
		distancia = Math.abs(distancia);
		if (distancia == 0)
		{
			return 1.0F;
		}
		return 1.0F/(float)distancia;
	}
	
	/**
	 * Calculo de la relevancia de un documento
	 * 
	 * <p>Ver: http://www.wolframalpha.com/input/?i=-x^2%2F%282*50%29^2+%2B+1+from+0+to+50
	 * 
	 * @param ranking
	 * @return relevancia de un documento segun su ranking valor entre 1 y 0
	 */
	public static float ordenDocumento(int ranking)
	{
		return (float)((float)(-ranking*ranking)/(float)((2*Constants.PAGES)*(Constants.PAGES)) + 1.0F);
	}
}
