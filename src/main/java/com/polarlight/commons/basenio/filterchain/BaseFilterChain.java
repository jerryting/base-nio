package com.polarlight.commons.basenio.filterchain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.polarlight.commons.basenio.exception.NioBaseWriteException;
import com.polarlight.commons.basenio.io.session.state.AbstractStateSession;

/***
 * IO Filter CoR. in [basenio] module,this CoR use only DefaultIoFilter(short-two-read
 * ) if U need extra filter control. define a new class implements
 * IoFitler, then add a class object to BaseFilterChain
 * 
 * @author DJ
 */

public class BaseFilterChain implements IoFilterChain {

	private volatile List<IoFilter> filterCoR = new ArrayList<IoFilter>();
	private volatile Map<String, IoFilter> nameMap = new HashMap<String, IoFilter>();

	@Override
	public boolean addFilter(String key, IoFilter filter) {
		if (nameMap.containsKey(key)) {
			return false;
		} else {
			this.nameMap.put(key, filter);
			this.filterCoR.add(filter);
			return true;
		}
	}

	@Override
	public byte[] filterReceive(AbstractStateSession session, byte[] preFilteBytes) throws IOException {

		for (IoFilter filter : filterCoR) {
			preFilteBytes = filter.filterReceive(session, preFilteBytes);
		}
		return preFilteBytes;
	}

	@Override
	public void filterWrite(AbstractStateSession session, byte[] writeBytes) throws NioBaseWriteException, IOException {

		for (IoFilter filter : filterCoR) {
			filter.filterWrite(session, writeBytes);
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
			IoFilter removed = this.nameMap.remove(name);
			this.filterCoR.remove(removed);
			return true;
		} else {
			return false;
		}
	}
}
