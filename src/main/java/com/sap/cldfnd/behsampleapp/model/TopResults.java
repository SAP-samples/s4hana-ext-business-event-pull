package com.sap.cldfnd.behsampleapp.model;

import java.util.List;

/**
 * A model to store the response that contains limited number of results
 *
 * @param <T>
 *            the type of requested entity
 */
public class TopResults<T> {
	
	private boolean limited;	

	private List<T> results;

	/**
	 * @return {@code true} if not all results are returned
	 */
	public boolean isLimited() {
		return limited;
	}

	public void setLimited(boolean limited) {
		this.limited = limited;
	}

	/**
	 * @return limited number of results
	 * @see TopResults#getTotalCount()
	 */
	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}
	
	public static <T> TopResults<T> of(List<T> results, boolean limited) {
		 return new TopResults<>(results, limited);
	}
	
	protected TopResults(List<T> results, boolean limited) {
		this.results = results;
		this.limited = limited;
	}
}
