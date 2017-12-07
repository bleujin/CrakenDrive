package net.bleujin.rcraken;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import net.ion.framework.session.Session;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;

public class TestProperty extends TestBaseCrakenRedis{

	
	public void testBlob() throws Exception {
		FileInputStream fis = new FileInputStream(new File("./resource/helloworld.txt")) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("data", fis).merge() ;
			return null ;
		}) ;
		
		rsession.pathBy("/emp/bleujin").properties().forEach(System.out::println);
		
		InputStream input = rsession.pathBy("/emp/bleujin").property("data").asStream() ;
		Debug.line(IOUtil.toStringWithClose(input));

		rsession.workspace().destorySelf() ; // removed all lob 
		Debug.line(rsession.workspace().client().getKeys().findKeysByPattern(rsession.workspace().name() + "/*")) ;
	}
	
	public void testBlobWhenRemove() throws Exception {
		FileInputStream fis = new FileInputStream(new File("./resource/helloworld.txt")) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").property("data", fis).merge() ;
			return null ;
		}) ;
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").removeSelf();
			return null ;
		}) ;
		
		assertEquals(0, rsession.workspace().client().getKeys().findKeysByPattern(rsession.workspace().name() + "/*").size()) ;
		
	}
}
