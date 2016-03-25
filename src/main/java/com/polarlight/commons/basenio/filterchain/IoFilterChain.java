package com.polarlight.commons.basenio.filterchain;

public interface IoFilterChain extends IoFilter {
	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean removeFilter(String name);
	/**
	 * add filter to filter chain
	 * @param name
	 * @param filter
	 * @return
	 */
	public boolean addFilter(String name, IoFilter filter);
}