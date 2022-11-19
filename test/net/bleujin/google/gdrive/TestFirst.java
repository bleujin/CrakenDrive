package net.bleujin.google.gdrive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class TestFirst {

	private GFile root;

	@BeforeEach
	void setUp() throws GeneralSecurityException, IOException {
		this.root = GBuilder.create().root("bleujin") ;
	}
	
	
	@Test
	public void login() throws GeneralSecurityException, IOException {
		root.childDebugPrint();
	}
	
	@Test
	public void list() throws IOException, GeneralSecurityException {
		Debug.line(root.listNames());
		Debug.line(root.listFiles());
		Debug.line(root.child("이사"));
		
		root.listFiles().stream().filter(gfile -> {
			return gfile.getName().equals("working.sql") ;
		}).forEach(Debug::println);
	}
	
	@Test
	public void create() throws IOException {
		root.createFolder("abc").delete() ;
		root.createFile(new File("./resource/6.jpg")) ;
	}
	
	@Test
	public void modMetaInfo() throws IOException {
		root.child("cat.jpg").metaInfo().name("cute.jpg").description("hello world").update() ;
		
	}

	@Test
	public void parents() throws IOException {
		Debug.line(root.child("cute.jpg").absolutePaths()) ;
	}

	@Test
	public void copyTo() throws IOException {
		root.child("cute.jpg").copyTo(root.child("pics")) ;
	}
	
	@Test
	public void moveTo() throws IOException {
		root.child("cute.jpg").moveTo(root.child("pics")) ;
	}
	

	@Test
	public void inputStream() throws IOException {
		GFile file = root.child("cute.jpg");
		InputStream input = file.inputStream() ;
		OutputStream output = new FileOutputStream(new File("./resource/cute.jpg")) ;
		IOUtil.copyNClose(input, output);
		Debug.line(file.getWebContentLink()) ;
	}
	
	
	@Test
	public void metaData() throws IOException {
		Debug.line(root.exists("working.sql"), root.exists("working")) ;
		Debug.line(root.child("working.sql").parents()) ;
		Debug.line(root.child("working.sql").getSize()) ;
		Debug.line(root.child("working.sql").getModified()) ;
		
		Debug.line(root.child("working.sql").getWebContentLink()) ;
		Debug.line(root.child("working.sql").getModified()) ;
		Debug.line(root.child("working.sql").getType()) ;
	}
}
