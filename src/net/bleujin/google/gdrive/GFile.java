package net.bleujin.google.gdrive;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;

import net.bleujin.fn.Fn;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;

public class GFile {

	public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

	private GDrive gdrive;
	private File ifile;
	private boolean hasDetail;

	public GFile(GDrive gdrive, File ifile, boolean hasDetail) {
		this.gdrive = gdrive;
		this.ifile = ifile;
		this.hasDetail = hasDetail;
	}

	public boolean isDirectory() {
		return MIME_TYPE_FOLDER.equals(ifile.getMimeType());
	};

	public String id() {
		return ifile.getId();
	}

	public String getName() {
		return ifile.getName();
	};

	public long getSize() {
		return ifile.getSize();
	}

	public String getModified() {
		return ifile.getModifiedTime().toStringRfc3339();
	}

	public String getType() {
		return ifile.getMimeType();
	}

	public String getWebContentLink() {
		return ifile.getWebContentLink() ;
	}

	
	

	public GFile createFolder(String name) throws IOException {
		return gdrive.create(this, name);
	}

	public GFile createFile(java.io.File child) throws IOException {
		return gdrive.create(this, child);
	};

	public List<GFile> listFiles() throws IOException {
		return gdrive.list(this);
	}

	public List<GFile> listFiles(String fields) throws IOException {
		return gdrive.list(this, fields);
	}

	
	public List<String> listNames() throws IOException {
		return listFiles().stream().map(gfile -> gfile.getName()).collect(Collectors.toList());
	}

	public GFile child(String name) {
		return gdrive.child(this, name);
	}

	public boolean delete() throws IOException {
		gdrive.delete(this);
		return true;
	}

	public InputStream inputStream() throws IOException {
		return drive().files().get(id()).executeMediaAsInputStream() ;
	}

	public List<String> absolutePaths() throws IOException {
		List<String> all = ListUtil.newList() ;
		recursiveParent(all, this.getName(), this);
		return all ;
	}

	private void recursiveParent(List<String> all, String cpath, GFile current) {
		List<GFile> parents = current.parents() ;
		if (parents == null || parents.size() == 0) {
			all.add(cpath) ;
			return ;
		} else {
			for(GFile parent : parents) {
				recursiveParent(all, parent.getName() + "/" + cpath, parent);
			}
		}
	}
	
	public List<GFile> parents() {
		List<String> parents = ifile.getParents();
		if (parents == null || parents.size() == 0) return ListUtil.EMPTY ;
		
		return parents.stream().map(Fn.wrap(pid -> gdrive().id(pid))).collect(Collectors.toList());
	}

	public boolean copyTo(GFile parent) throws IOException {
		if (! parent.isDirectory()) throw new IOException(parent.getName() + " is not dir") ;
		
		File toFile = new File() ;
		toFile.setName(getName()) ;
		toFile.setParents(ListUtil.create(parent.id())) ;
		
		return drive().files().copy(id(), toFile).execute() != null;
	}

	public boolean moveTo(GFile parent) throws IOException {
		copyTo(parent) ;
		return this.delete() ; 
	}

	public boolean exists(String name) throws IOException {
		return gdrive.exists(this, name);
	}
	

	public void childDebugPrint() throws IOException {
		listFiles().stream().forEach(Debug::println);
	}
	
	
	
	

	public GDrive gdrive() {
		return gdrive;
	}

	File inner() {
		return ifile;
	}
	
	Drive drive() {
		return gdrive.drive() ;
	}
	

	public boolean hasDetails() {
		return hasDetail;
	}

	public String toString() {
		return String.format("[%s]\t%s\t%s", getName(), getModified(), getType());
	}


	public MetaInfoFile metaInfo() {
		return new MetaInfoFile(gdrive, this);
	}

	public Files driveFiles() {
		return gdrive.drive().files();
	}
	


	public GFile consume() throws IOException {
		this.ifile = drive().files().get(id()).setFields("*").execute() ;
		this.hasDetail = true ;
		return this ;
	}

}
