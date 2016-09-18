package vnreal.io;

import java.util.HashMap;

public class ExportIDSource {

	long nextId;
	HashMap<Object, Long> ids;

	public ExportIDSource() {
		this.nextId = 0l;
		this.ids = new HashMap<Object, Long>();
	}

	public String getId(Object v) {
		Long exportid = this.ids.get(v);
		if (exportid == null) {
			exportid = this.nextId++;
			this.ids.put(v, exportid);
		}
		return exportid + "";
	}

}
