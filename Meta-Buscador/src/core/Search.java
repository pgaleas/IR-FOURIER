package core;

import java.io.IOException;

import search.Constants;
import search.Indexer;
import search.Searcher;
import util.DBManager;
import util.PageRetrieval;
import web_services.BingSearchAPI;
import web_services.GoogleSearchAPI;
import web_services.Provider;
import web_services.WebSearcher;
import models.Page;
import models.Query;
import models.ResultRank;

/**
 * 
 * Clase principal para el proceso completo de b&uacute;squeda.
 * 
 * @author      Javier Fuentes (j.fuentes06@ufromail.cl)
 * @version 1.0
 * @since 1.0
 */
public class Search {
	
	/**
	 * Cantidad de p&aacute;ginas a buscar.
	 */
	
	
	/**
	 * M&eacute;todo que realiza la b&uacute;squeda, indexa y retorna el resultado de la b&uacute;squeda 
	 * en base a los parametros de distancia, {@link ResultRank}
	 * @param query consulta a ejecutar.
	 * @param provider proveedor de servicios de internet.
	 * @param getGraph si esta como true, el resultado no
	 *  tendra en consideracion los terminos relacionados ni con sus pesos y generara el grafo de las palabras relacionadas.
	 * @return {@link ResultRank}
	 */
	public static ResultRank initSearch(Query query, Provider provider, boolean getGraph)
	{
		System.out.println("Buscando por: " + query.getQuery());
		
		WebSearcher searchEngine;
		ResultRank rr = new ResultRank();
		Searcher searcher;
		
		
		/**
		 * Seleccion del proveedor de busquedas. Solo disponible google y bing
		 */
		if (provider == Provider.BING) searchEngine = new BingSearchAPI();
		else if (provider == Provider.GOOGLE) searchEngine = new GoogleSearchAPI();
		else searchEngine = new GoogleSearchAPI();
		
		/**
		 * Si la query no se encuentra en la DB, se descargara.
		 */
		if (!DBManager.isQueryInCache(query))
		{
			
			try {
				
				
				System.out.println("Buscando paginas");
				
				/**
				 * Se buscan las paginas desde el proveedor de busquedas. 
				 */
				Page[] pages = searchEngine.getPages(query, Constants.PAGES/10);
				System.out.println("Paginas encontradas");
				
				/**
				 * Se descargan los contenidos, y se limpian (raw content)
				 * El proceso utiliza threads.
				 */
				Thread[] threads = new Thread[pages.length];
				for (int i=0; i < pages.length; i++)
				{
					int temp = i;
					threads[i] = new Thread(new Runnable() {
						@Override
						public void run() {
							
							/**
							 * Los contenidos seran descargados unicamente si no se encuentran en el cache.
							 */
							if (!DBManager.isPageInCache(pages[temp]))
							{
								System.out.println("Descargando contenidos crudos... ("+temp+")");
								PageRetrieval.retrieveContentPage(pages[temp]);
								/**
								 * Los contenidos, luego de ser descargados, son asociados a la base de datos.
								 */
								DBManager.addPage(pages[temp]);
							}
						}
					});
					threads[i].start();
				}
				
				/**
				 * Es necesario esperar que todos los threads abiertos terminen de realizar las descargas.
				 */
				for (int i=0; i < threads.length; i++) {
					try {
						while (threads[i].isAlive()) Thread.sleep(10);
						System.out.println("Thread: " + i + " terminado");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				/**
				 * La query se asocia a la base de datos.
				 */
				DBManager.addQuery(query, pages);
				System.out.println("Adding queries");
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			/**
			 * Se indexan las paginas.
			 */
			new Indexer(query);
		}
		
		/**
		 * Si se encuentra en el cache la query, simplemente se realiza la consulta.
		 */
		searcher = new Searcher(query, getGraph);
		
		try {
			rr = searcher.result();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/**
		 * Se retorna los resultados.
		 */
		return rr;
	}
}
