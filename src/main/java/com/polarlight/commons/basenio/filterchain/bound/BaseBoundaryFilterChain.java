package com.polarlight.commons.basenio.filterchain.bound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.polarlight.commons.basenio.io.session.stateless.AbstractStatelessSession;
import com.polarlight.commons.basenio.io.session.stateless.DirectSendBean;

/***
 * IO Filter CoR. in [basenio] module,this CoR use only
 * IoBoundaryFilter(bound-read ) if U need extra filter control. define a new
 * class implements IoBoundaryFilterChain, then add a class object to
 * BaseFilterChain
 * 
 * @author DJ
 * @since 2.0
 */

public class BaseBoundaryFilterChain implements IoBoundaryFilterChain {

	private volatile List<IoBoundaryFilter> filterCoR = new ArrayList<IoBoundaryFilter>();
	private volatile Map<String, IoBoundaryFilter> nameMap = new HashMap<String, IoBoundaryFilter>();

	@Override
	public boolean addFilter(String key, IoBoundaryFilter filter) {
		if (nameMap.containsKey(key)) {
			return false;
		} else {
			this.nameMap.put(key, filter);
			this.filterCoR.add(filter);
			return true;
		}
	}

	@Override
	public byte[] filterReceive(AbstractStatelessSession session, byte[] preFilteBytes) throws IOException {

		for (IoBoundaryFilter filter : filterCoR) {
			preFilteBytes = filter.filterReceive(session, preFilteBytes);
		}
		return preFilteBytes;
	}

	@Override
	public void filterWrite(AbstractStatelessSession session, byte[] writeBytes) throws IOException {
		for (IoBoundaryFilter filter : filterCoR) {
			filter.filterWrite(session, writeBytes);
		}
	}

	@Override
	public void filterWriteDirect(AbstractStatelessSession session, DirectSendBean dsb) throws IOException {
		for (IoBoundaryFilter filter : filterCoR) {
			filter.filterWriteDirect(session, dsb);
		}
	}

	@Override
	public void removeFilter() {
		if (nameMap != null) {
			nameMap.clear();
			nameMap = null;
		}
		if (filterCoR != null) {
			filterCoR.clear();
			filterCoR = null;
		}
	}

	@Override
	public boolean removeFilter(String name) {
		if (this.nameMap.containsKey(name)) {
			IoBoundaryFilter removed = this.nameMap.remove(name);
			this.filterCoR.remove(removed);
			return true;
		} else {
			return false;
		}
	}
}
