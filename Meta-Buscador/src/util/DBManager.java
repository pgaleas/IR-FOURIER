package util;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

import search.Constants;
import models.Page;
import models.Query;


/**
 *	Clase con utilidades para el manejo de los datos. 
 * 
 * @author Javier Fuentes (j.fuentes06@ufromail.cl)
 * @version 1.0
 * @since 1.0
 * 
 */
public class DBManager {
	/**
	 * Parametros de configuracion de la base de datos.
	 */
	private static String baseurl = Constants.PATH;
	private static Scanner s;
	
	/**
	 * Permite revisar si la query ya fue buscada previamente.
	 * 
	 * @param q
	 * @return boolean true en caso de encontrar una consulta, false en caso contrario.
	 */
	public static boolean isQueryInCache(Query q)
	{
		File f= new File(baseurl+"queries/"+toSHA2(q.getQuery()));
		return f.exists();
	}
	
	/**
	 * Permite revisar si la pagina ya fue descargada.
	 * 
	 * @param page
	 * @return boolean true en caso de encontrar la pagina, false en caso contrario.
	 */
	public static boolean isPageInCache(Page page)
	{
		File f = new File(baseurl + "pages/" + toSHA2(page.getUrl()));
		return f.exists();
	}
	
	/**
	 * Elimina una query completa, incluyendo sus paginas y su indice relacionado.
	 * 
	 * @param q
	 */
	public static void removeQuery(Query q)
	{
		
		try {
			File f;
			
			/**
			 * Remover paginas.
			 */
			ArrayList<String> urls;
			urls = getUrls(q);
			for (String url : urls) {
				f = new File(baseurl + "pages/" + toSHA2(url));
				f.delete();
			}
			
			/**
			 * Remover queries
			 */
			f = new File(baseurl + "queries/" + toSHA2(q.getQuery()));
			f.delete();
			
			/**
			 * Remover el indice
			 */
			removeIndex(q);
			
			/**
			 * Remueve el grafo
			 */
			removeGraph(q);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Remueve solamente el indice.
	 * @param q
	 */
	public static void removeIndex(Query q)
	{
		File f;
		f = new File(baseurl + "indexes/" + toSHA2(q.getQuery()));
		File[] files = f.listFiles();
		for (File file : files) {
			file.delete();
		}
		f.delete();
	}
	
	/**
	 * Remueve solamente el grafo generado.
	 * @param q
	 */
	public static void removeGraph(Query q)
	{
		File f = new File(baseurl + "graphs/" + toSHA2(q.getQuery()));
		f.delete();
	}
	
	
	/**
	 * Ingresa una query a la base de datos, incluyendo las paginas relacionadas a la misma.
	 * @param q
	 * @param pages
	 */
	public static void addQuery(Query q, Page[] pages) 
	{
		try 
		{
			File f= new File(baseurl+"queries/"+toSHA2(q.getQuery()));
			f.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			for (int i=0; i < pages.length; i++) {
				bw.write(pages[i].getUrl());
				if (i+1 < pages.length) bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Obtiene la ruta del indice.
	 * @param q
	 * @return File que contiene la ruta al indice.
	 */
	public static File getIndexFile(Query q)
	{
		File f = null;
		try 
		{
			f = new File(baseurl + "indexes/" + toSHA2(q.toString()));
			if (!f.exists()) 
				f.mkdir();
			return f;
		}catch(SecurityException e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Permite ingresar una pagina al indice.
	 * 
	 * @param p
	 */
	public static void addPage(Page p)
	{
		File file = new File(baseurl + "pages/" + toSHA2(p.getUrl()));
		if (file.exists())
		{
			throw new RuntimeException("Archivo ya existe");
		}
		else
		{
			try
			{
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
				oos.writeObject(p);
				oos.close();
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Obtiene una lista con las paginas relacionadas a la query.
	 * @param q
	 * @return lista con paginas.
	 */
	public static Page[] getPagesFromQuery(Query q)
	{
		try {
			ArrayList<String> urls = getUrls(q);
			Page[] pages = new Page[urls.size()];
			for (int i=0; i < urls.size(); i++)
			{
				File f = new File(baseurl +  "pages/" + toSHA2(urls.get(i)));
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
				pages[i] = (Page) ois.readObject();
				ois.close();
			}
			return pages;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Incluye el grafo generado a la BD
	 * 
	 * @param query del grafo generado
	 */
	public static void addGraph(Query query)
	{
		try 
		{
			File f= new File(baseurl+"graphs/"+toSHA2(query));
			f.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			for (int i=0; i < query.getTerms().length; i++) {
				bw.write(query.getTerms()[i]+" "+query.getValues()[i]);
				if (i+1 < query.getTerms().length) bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Se ingresan los valores originales del grafo calculado.
	 * 
	 * @param query
	 * @throws FileNotFoundException 
	 */
	public static void setGraph(Query query)
	{
		File f = new File(baseurl + "graphs/" + toSHA2(query));
		try {
			s = new Scanner(f);
			ArrayList<String> terms = new ArrayList<>();
			while (s.hasNext()) terms.add(s.nextLine());
			
			query.setTerms(new String[terms.size()]);
			query.setValues(new float[terms.size()]);
			
			for (int i=0; i < terms.size(); i++)
			{
				String t[] = terms.get(i).split(" ");
				query.getTerms()[i] = t[0];
				query.getValues()[i] = Float.parseFloat(t[1]);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtiene un arreglo con las urls de una query.
	 *  
	 * @param q
	 * @return array con las urls.
	 * @throws FileNotFoundException
	 */
	private static ArrayList<String> getUrls(Query q) throws FileNotFoundException
	{
		File f = new File(baseurl + "queries/" + toSHA2(q.getQuery()));
		s = new Scanner(f);
		ArrayList<String> urls = new ArrayList<>();
		while (s.hasNext()) urls.add(s.nextLine());
		return urls;
	}
	
	/**
	 * Genera un SHA-2 de una query, como funcion hash para almacenar indices.
	 * @param q
	 * @return String con el hash SHA-2
	 */
	public static String toSHA2(String q)
	{
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(q.getBytes());
			byte[] byteData = digest.digest();
			
			StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Metodo sobrecargado
	 * 
	 * @param q
	 * @return String con el hash SHA-2
	 */
	public static String toSHA2(Query q)
	{
		return toSHA2(q.getQuery());
	}
	
	
	
	/**
	 * Genera las carpetas del sistema
	 * 
	 * <p>Genera las carpetas usadas por el sistema para almacenar en cache las paginas e indices.
	 */
	public static void createFolders()
	{
		File f = new File(baseurl + "queries/");
		f.mkdir();
		f = new File(baseurl + "pages/");
		f.mkdir();
		f = new File(baseurl + "indexes/");
		f.mkdir();
		f = new File(baseurl + "graphs/");
		f.mkdir();
	}


}
