package com.polarlight.commons.basenio.filterchain.bound;

/**
 * 
 * @author DJ
 * @since 2.0
 */
public interface IoBoundaryFilterChain extends IoBoundaryFilter {
	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean removeFilter(String name);

	/**
	 * add filter to filter chain
	 * 
	 * @param name
	 * @param filter
	 * @return
	 */
	public boolean addFilter(String name, IoBoundaryFilter filter);
}