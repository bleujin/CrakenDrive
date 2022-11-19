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
		this.root = GBuilder.login("bleujin").root() ;
	}
	
	
	@Test
	public void sharedWithMe() throws IOException {
		List<GFile> sharedList = root.gdrive().sharedWithMe() ;
		sharedList.stream().map(GFile.wrap(gfile -> gfile.consume())).forEach(gfile ->{
			Debug.line(gfile.inner().getOwners(), gfile.inner().getDescription(), gfile.getName()) ;
		});
		
	}
}
