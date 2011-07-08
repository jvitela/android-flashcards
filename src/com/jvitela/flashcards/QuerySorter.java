/**
 * 
 */
package com.jvitela.flashcards;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author jonathan
 *	Utility class to create sort filters for a query
 */
public class QuerySorter {
	private Vector<String>	mFields;
	
	public QuerySorter(){
		mFields = new Vector<String>();
	}
	
	/** 
	 * Adds ascending filter 
	 * @param field:	Name of the sort field (String)
	 * @return	this, to enable chain calls
	 */
	public QuerySorter asc(String field) {
		mFields.add(field+" asc");
		return this;
	}
	
	/**
	 * Adds descending filter
	 * @param field:	Name of the sort field (String)
	 * @return	this, to enable chain calls
	 */
	public QuerySorter desc(String field) {
		mFields.add(field+" desc");
		return this;
	}

	/** 
	 * Adds random sort function
	 * @return	this, to enable chain calls
	 */
	public QuerySorter rand() {
		mFields.add("RANDOM()");
		return this;
	}

	/** 
	 * Clear
	 */
	public void clear() {
		mFields.clear();
	}
	
	/**
	 * Returns string representing sort clause
	 */
	public String toString() {
		Iterator<String> itr = mFields.iterator();
		String res = itr.hasNext()?itr.next():"";
		while( itr.hasNext() ) {
			res += ", "+itr.next();
		}
		return res;
	}
}
