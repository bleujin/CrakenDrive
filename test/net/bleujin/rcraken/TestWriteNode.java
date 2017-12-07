package net.bleujin.rcraken;

import java.awt.image.SampleModel;
import java.util.concurrent.Future;

import net.ion.framework.util.Debug;

public class TestWriteNode extends TestBaseCrakenRedis {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		rsession.workspace().destorySelf() ;
		super.tearDown();
	}
	
	public void testRefTo() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("name", "bleujin").refTo("friend", "/emp/hero").merge();
			wsession.pathBy("/emp/hero").property("name", "hero").merge();
			return null;
		}) ;
		
		
		ReadNode hero = rsession.pathBy("/emp/bleujin").ref("friend") ;
		hero.debugPrint();
	}

	public void testSpeed() throws Exception {
		long start = System.currentTimeMillis() ;
		for (int i = 0; i < 100; i++) {
			final int age = i ;
			rsession.tran(wsession -> {
				wsession.pathBy("/emp/bleujin").property("age", age).merge(); ;
				wsession.pathBy("/emp/hero").property("age", 30).merge(); ;
				return null;
			}) ;
		}
		Debug.line(System.currentTimeMillis() - start);
		rsession.pathBy("/emp/bleujin").debugPrint();
	}
	
	public void testOverwrite() throws Exception {
		rsession.workspace().destorySelf() ;
		long start = System.currentTimeMillis() ;
		rsession.tran(SAMPLE) ;
		Debug.line(System.currentTimeMillis() - start);
		rsession.tran(wsession -> {
			wsession.pathBy("/emp/bleujin").property("age", 40).merge(); ;
			wsession.pathBy("/emp/hero").property("age", 30).merge(); ;
			return null;
		}) ;
		Debug.line(System.currentTimeMillis() - start);
		
		
		rsession.pathBy("/emp/bleujin").debugPrint();
		rsession.pathBy("/emp/hero").debugPrint();
	}

}
