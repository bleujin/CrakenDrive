package net.bleujin.rcraken.extend.rows;

import net.bleujin.rcraken.Property;

public interface NodeCommonMap<T extends NodeCommonMap<T>> {

	public Property property(String key);
	public T child(String fqn);
	
	public boolean hasChild(String fqn);
	public boolean hasProperty(String pid) ;
	public T parent();
	public boolean hasRef(String refName);
	public T ref(String refName);
}
