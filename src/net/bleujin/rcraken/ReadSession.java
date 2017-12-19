package net.bleujin.rcraken;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.redisson.api.RMap;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;

import net.ion.framework.file.HexUtil;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.MapUtil;
import net.ion.nsearcher.config.Central;
import net.ion.nsearcher.search.Searcher;

public class ReadSession {

	public final static String EncryptKeyBytes = "KeyBytes";
	public final static String EncryptIvBytes = "IvBytes";

	private static ExceptionHandler ehandler = ExceptionHandler.PRINT;

	private Workspace wspace;
	private RedissonClient rclient;

	private RMap<String, String> dataMap;
	private RSetMultimap<String, String> struMap;
	private Map<String, Object> attrs = MapUtil.newMap();

	ReadSession(Workspace wspace, RedissonClient rclient) {
		this.wspace = wspace;
		this.rclient = rclient;
		this.dataMap = rclient.getMapCache(wspace.nodeMapName());
		this.struMap = rclient.getSetMultimapCache(wspace.struMapName());

		attribute(EncryptKeyBytes, "40674244".getBytes());
		attribute(EncryptIvBytes, "@1B2c3D4".getBytes());
	}

	public ReadNode pathBy(String path) {
		Fqn fqn = Fqn.from(path);
		return pathBy(fqn);
	}

	public ReadNode pathBy(Fqn fqn) {
		return new ReadNode(this, fqn, readDataBy(fqn));
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path);
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}

	public <T> CompletableFuture<T> tran(WriteJob<T> tjob) {
		return tran(tjob, ehandler);
	}

	public <T> CompletableFuture<T> tran(WriteJob<T> tjob, ExecutorService eservice) {
		return wspace.tran(wspace.writeSession(this), tjob, eservice, ehandler);
	}

	public <T> CompletableFuture<T> tran(WriteJob<T> tjob, ExceptionHandler ehandler) {
		return wspace.tran(wspace.writeSession(this), tjob, wspace.executor(), ehandler);
	}

	public <T> CompletableFuture<T> batch(BatchJob<T> bjob) {
		return batch(bjob, ehandler);
	}

	public <T> CompletableFuture<T> batch(BatchJob<T> bjob, ExecutorService eservice) {
		return wspace.batch(wspace.batchSession(this), bjob, eservice, ehandler);
	}

	public <T> CompletableFuture<T> batch(BatchJob<T> bjob, ExceptionHandler ehandler) {
		return wspace.batch(wspace.batchSession(this), bjob, wspace.executor(), ehandler);
	}

	
	
	
	private JsonObject readDataBy(Fqn fqn) {
		String jsonString = dataMap.get(fqn.absPath());
		return JsonObject.fromString(jsonString);
	}

	Set<String> readStruBy(Fqn fqn) {
		return struMap.getAll(fqn.absPath());
	}

	void descentantBreadth(Fqn fqn, List<String> fqns) {
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
			descentantBreadth(child, fqns);
		}
	}

	void descentantDepth(Fqn fqn, List<String> fqns) {
		for(String childName : readStruBy(fqn)) {
			Fqn child = Fqn.from(fqn, childName);
			fqns.add(child.absPath()) ;
		}

		for(String childName : readStruBy(fqn)) {
			descentantDepth(Fqn.from(fqn, childName), fqns);
		}
	}

	public void walkRef(ReadNode source, String relName, int limit, List<String> fqns) {
		if (limit == 0) return ; 
		for(String relPath : source.property(relName).asStrings()) {
			Fqn rel = Fqn.from(relPath);
			if (! source.session().exist(rel.absPath())) continue ;
			fqns.add(rel.absPath()) ;
			walkRef(source.session().pathBy(rel), relName, --limit, fqns);
		}
	}


	public Workspace workspace() {
		return wspace;
	}

	void reload() {
		// this.dataMap = rclient.getMapCache(wspace.name(), wspace.mapOption()) ;
	}

	@Deprecated
	RMap<String, String> dataMap() {
		return dataMap;
	}

	public Searcher newSearcher() throws IOException {
		Central central = workspace().central();
		if (central == null) throw new IllegalStateException("this workspace not indexed") ;
		return central.newSearcher();
	}

	
	public void attribute(String key, Object value) {
		attrs.put(key, value);
	}

	public Object attribute(String key) {
		return attrs.get(key);
	}

	
	
	
	String encrypt(String value) throws IOException {
		try {
			byte[] keyBytes = (byte[]) attribute(ReadSession.EncryptKeyBytes);
			byte[] ivBytes = (byte[]) attribute(ReadSession.EncryptIvBytes);

			SecretKeySpec skey = new SecretKeySpec(keyBytes, "DES"); // wrap key data in Key/IV specs to pass to cipher
			IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding"); // create the cipher with the algorithm you choose see javadoc for Cipher class for more info, e.g.

			byte[] input = value.getBytes("UTF-8");

			cipher.init(Cipher.ENCRYPT_MODE, skey, ivSpec);
			byte[] encrypted = new byte[cipher.getOutputSize(input.length)];

			int enc_len = cipher.update(input, 0, input.length, encrypted, 0);
			enc_len += cipher.doFinal(encrypted, enc_len);
			
			return HexUtil.toHex(encrypted) ;
		} catch(NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | ShortBufferException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException ex) {
			throw new IOException(ex) ;
		}
	}


}
