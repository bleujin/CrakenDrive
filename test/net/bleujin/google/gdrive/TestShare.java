package net.bleujin.google.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.ion.framework.util.Debug;

public class TestShare {

	private GFile root;

	@BeforeEach
	void setUp() throws GeneralSecurityException, IOException {
		this.root = GBuilder.create().root("bleujin") ;
	}
	
	
	@Test
	public void sharedWithMe() throws IOException {
		List<GFile> sharedList = root.gdrive().allFields().sharedWithMe() ;
		sharedList.stream().forEach(gfile ->{
			Debug.line(gfile.inner().getOwners(), gfile.inner().getDescription(), gfile.getName()) ;
		});
		
	}
}
