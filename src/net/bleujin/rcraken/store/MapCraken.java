package net.bleujin.rcraken.store;

import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker.Maker;

import net.bleujin.rcraken.Craken;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.util.MapUtil;

public class MapCraken extends Craken {

	private CrakenNode cnode;
	private Maker maker;
	private DB db;
	private Map<String, MapWorkspace> wss = MapUtil.newMap() ;
	private MapNode mnode;

	public MapCraken(Maker maker) {
		this.maker = maker;
	}

	public Craken start() {
		return start(true);
	}

	public MapCraken start(boolean doStartNodeService) {
		this.db = maker.make() ;
		if (doStartNodeService) this.mnode = new MapNode(db, maker).start();
		return this;
	}

	
	public ReadSession login(String wname) {
		return findWorkspace(wname).readSession();
	}

	protected MapWorkspace findWorkspace(String wname) {
		if (wname.startsWith("_"))
			throw new IllegalAccessError("illegal worksapce name");

		wss.putIfAbsent(wname, (MapWorkspace)new MapWorkspace(wname, mnode, db).init());
		return wss.get(wname);
	}

	public void shutdownSelf() {
		if (cnode != null) cnode.shutdown();
		
		db.close();
	}
	
	@Deprecated // test only
	public void removeAll() {
		
	}

	public MapNode node() {
		if (this.db == null || this.mnode == null) throw new IllegalStateException("craken node not started");
		return mnode ;
	}
	
}