package net.bleujin.google.gdrive;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.activation.FileTypeMap;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;

public class GDrive {

	private static final String defaultFields = "id,name,mimeType,parents,size,modifiedTime,webContentLink";
	private static final String defaultFieldsItems = "files(" + defaultFields + ")";

	private Drive drive;

	public GDrive(Drive drive) {
		this.drive = drive;
	}

	public boolean exists(GFile parent, String name) throws IOException {
		return child(parent, name) != null;
	}

	Drive drive() {
		return drive ;
	}
	
	public List<GFile> list(String query, String fields, boolean includeTrashed) throws IOException {
		Drive.Files.List list = drive.files().list();

		if (fields != null)
			list.setFields(fields);
		if (query != null)
			list.setQ(query);
		if (!includeTrashed)
			list.setQ(query + (query != null ? " AND" : "") + " trashed = false");

		return list(list.execute());
	}

	private List<GFile> list(FileList fileList) {
		if (fileList == null)
			return null;

		List<GFile> list = ListUtil.newList();
		for (int i = 0; i < fileList.getFiles().size(); i++) {
			list.add(new GFile(this, fileList.getFiles().get(i), false));
		}

		return list;
	}

	public void delete(GFile gfile) throws IOException {
		drive.files().delete(gfile.id()).execute();
	}

	public GFile child(GFile parent, String name) {
		try {
			return first("'" + parent.id() + "' in parents AND name = '" + name + "'", defaultFieldsItems, false);
		} catch (IOException e) {
			return null;
		}
	}

	private GFile first(String query, String fields, boolean includeTrash) throws IOException {
		List<GFile> list = list(query, fields, includeTrash);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}

	public List<GFile> list(GFile parent) throws IOException {
		return list("'" + parent.id() + "' in parents", defaultFieldsItems, false);
	}

	public GFile create(GFile parent, String childName) throws IOException {
		File file = new File();
		file.setName(childName);
		file.setMimeType(GFile.MIME_TYPE_FOLDER);

		if (parent != null)
			file.setParents(Arrays.asList(parent.id()));

		file = drive.files().create(file).execute();
		return id(file.getId());
	}

	public GFile id(String id) throws IOException {
		return new GFile(this, drive.files().get(id).setFields(defaultFields).execute(), false);
	}

	public GFile forMod(String id) throws IOException {
		return new GFile(this, drive.files().get(id).setFields("name,mimeType,description").execute(), false);
	}


	public GFile create(GFile parent, java.io.File child) throws IOException {
		File file = new File();
		file.setName(child.getName());

		if (parent != null)
			file.setParents(Arrays.asList(parent.id()));

		file = drive.files().create(file, new FileContent(fileType(child), child)).execute();
		return id(file.getId());
	}

	private String fileType(java.io.File child) {
		return FileTypeMap.getDefaultFileTypeMap().getContentType(child);
	}

	public GFile update(GFile gfile, java.io.File localFile) throws IOException {
		File file = drive.files().update(gfile.id(), gfile.inner(), new FileContent(fileType(localFile), localFile)).execute();
		return new GFile(this, file, gfile.hasDetails());
	}

	
	public List<GFile> sharedWithMe() throws IOException {
		com.google.api.services.drive.Drive.Files.List list = drive.files().list().setFields(defaultFieldsItems) ;
		
		return list.execute().getFiles().stream().filter(file -> file.getParents() == null).map(ifile -> new GFile(this, ifile, false)).collect(Collectors.toList()) ;
	}
}
