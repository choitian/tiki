package tiki.uitls.collection;

import java.util.Collection;
import java.util.LinkedHashMap;

public class HashSet<T extends IHash> implements IHash{
	private LinkedHashMap<String,T> items = new LinkedHashMap<>();

	@Override
	public String hashString() {
		return String.join(".",items.keySet());
	}
	public Collection<T> set()
	{
		return items.values();
	}
	public boolean add(T item) {
		if(!items.containsKey(item.hashString()))
		{
			items.put(item.hashString(), item);
			return true;
		}
		return false;
	}
	public void add(HashSet<T> set) {
		for(T it:set.items.values())
		{
			add(it);
		}
	}
	public T get(String hashString)
	{
		return items.get(hashString);
	}
	public T get(T item)
	{
		if(item == null)
			return null;
		
		return get(item.hashString());
	}
	public boolean has(String hashString)
	{
		return items.containsKey(hashString);
	}
	public boolean has(T item)
	{
		if(item == null)
			return false;
		return has(item.hashString());
	}
	public int size()
	{
		return items.size();
	}
}
