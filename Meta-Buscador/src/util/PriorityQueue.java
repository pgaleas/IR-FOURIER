package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import models.Query;
import search.Constants;

/**
 * 
 * Clase utilitaria para ordenar terminos
 * 
 * @author Javier Fuentes (j.fuentes06@ufromail.cl)
 * @version 1.0
 * @since 1.0
 *
 */
public class PriorityQueue {
	private int maxSize;
	private Query query;
	private HashMap<String, Float> terms;
	
	public PriorityQueue(Query query) {
		maxSize = Constants.MAX_TERMS_SIZE;
		this.query = query;
		init();
	}
	
	public PriorityQueue(Query query, int maxSize)
	{
		this.maxSize = maxSize;
		this.query = query;
		init();
	}
	
	private void init()
	{
		terms = new HashMap<>();
	}
	
	public void add(String term, float value)
	{
		if (value < 0.2F) return;
		if (terms.containsKey(term))
		{
			float f = terms.get(term);
			terms.remove(term);
			terms.put(term, Math.max(f, value));
		}
		else
		{
			if (terms.size() < maxSize)
			{
				terms.put(term, value);
			}
			else
			{
				Set<String> set = terms.keySet();
				Iterator<String> iter = set.iterator();
				
				float min=10.0F;
				String key = "";
				
				while(iter.hasNext())
				{
					key = iter.next();
					float temp = terms.get(key);
					if (min > temp)
					{
						min = temp;
					}
				}
				
				if (min < value && !key.equals(""))
				{
					terms.remove(key);
					terms.put(term, value);
				}
			}
		}
		toArray();
	}
	private void toArray()
	{
		int size = terms.size();
		query.setTerms(new String[size]);
		query.setValues(new float[size]);
		
		Set<String> set = terms.keySet();
		Iterator<String> iter = set.iterator();
		int i=0;
		while (iter.hasNext())
		{
			String t = iter.next();
			query.getTerms()[i] = t;
			query.getValues()[i] = terms.get(t);
			i++;
		}
	}
}
