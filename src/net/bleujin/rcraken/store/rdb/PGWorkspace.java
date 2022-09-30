package net.bleujin.rcraken.store.rdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import net.bleujin.rcraken.BatchJob;
import net.bleujin.rcraken.BatchSession;
import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.ExceptionHandler;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.Workspace;
import net.bleujin.rcraken.WriteJob;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.extend.Sequence;
import net.bleujin.rcraken.extend.Topic;
import net.ion.framework.db.DBController;
import net.ion.framework.db.Rows;
import net.ion.framework.db.bean.ResultSetHandler;
import net.ion.framework.db.procedure.IParameterQueryable;
import net.ion.framework.db.procedure.IUserProcedure;
import net.ion.framework.util.Debug;
import net.ion.framework.util.StringUtil;

public class PGWorkspace extends Workspace {

	
	private DBController dc;
	private PGConfig config;
	private File wrootDir ;
	protected PGWorkspace(DBController dc, PGConfig config, File wrootDir, CrakenNode cnode, String wname) {
		super(cnode, wname);
		this.dc = dc ;
		this.config = config ;
		this.wrootDir = wrootDir ;
	}

	@Override
	protected PGWriteSession writeSession(ReadSession rsession) {
		return new PGWriteSession(this, rsession);
	}

	@Override
	protected PGBatchSession batchSession(ReadSession rsession) {
		return new PGBatchSession(this, writeSession(rsession));
	}

	@Override
	protected PGReadSession readSession() {
		return new PGReadSession(this);
	}

	@Override
	protected <T> CompletableFuture<T> tran(WriteSession wsession, WriteJob<T> tjob, ExecutorService eservice, ExceptionHandler ehandler) {
		if (eservice.isTerminated() || eservice.isShutdown()) return CompletableFuture.completedFuture(null) ;
		
		return CompletableFuture.supplyAsync(() -> {
			wsession.attribute(WriteJob.class, tjob);
			wsession.attribute(ExceptionHandler.class, ehandler);

			try {
				T result = tjob.handle(wsession);
				wsession.endTran();
				return result;
			} catch (Throwable ex) {
				ehandler.handle(wsession, tjob, ex);
				throw new IllegalStateException(ex) ; 
			} finally {
			}
		}, eservice) ;
	}

	@Override
	protected <T> CompletableFuture<T> batch(BatchSession bsession, BatchJob<T> bjob, ExecutorService eservice, ExceptionHandler ehandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sequence sequence(String name) {
		return new PGSequence(dc, name) ;
	}

	@Override
	public <T> Topic<T> topic(String name) {
		throw new UnsupportedOperationException("current fn not supported in pg-store") ;
	}

	@Override
	protected OutputStream outputStream(String path) throws FileNotFoundException {
		File target = new File(workspaceRootDir(), StringUtil.replaceChars(path, '$', '.')) ;
		return new FileOutputStream(target) ;
	}

	@Override
	protected InputStream inputStream(String path) throws FileNotFoundException {
		File target = new File(workspaceRootDir(), StringUtil.replaceChars(path, '$', '.')) ;
		return new FileInputStream(target) ;
	}
	
	DBController dc() {
		return dc ;
	}

	IUserProcedure createUserProcedure(String procSQL) {
		return dc.createUserProcedure(procSQL) ;
	}

	
	int execUpdate(IParameterQueryable iParameterQueryable) {
		return dc.execUpdate(iParameterQueryable) ;
	}

	Rows execQuery(IParameterQueryable upt) {
		return dc.getRows(upt) ;
	}

	<T> T execQuery(IParameterQueryable upt, ResultSetHandler<T> handler) {
		return dc.execHandlerQuery(upt, handler) ;
	}

	File workspaceRootDir() {
		return wrootDir ;
	}
}