package net.bleujin.google.gdrive;

import java.io.IOException;

import com.google.api.services.drive.model.File;

public class MetaInfoFile {

	private String name;
	private String description;
	private GDrive gdrive;
	private GFile gfile;

	public MetaInfoFile(GDrive gdrive, GFile gfile) {
		this.gdrive = gdrive;
		this.gfile = gfile;
	}

	public MetaInfoFile name(String name) {
		this.name = name;
		return this;
	}

	public MetaInfoFile description(String description) {
		this.description = description;
		return this;
	}

	public boolean update() throws IOException {
		File mod = gdrive.forMod(gfile.id()).inner();
		if (name != null)
			mod.setName(name);
		if (description != null)
			mod.setDescription(description);

		gfile.driveFiles().update(gfile.id(), mod).execute();
		return true;
	}

}
