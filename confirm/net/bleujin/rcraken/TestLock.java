package net.bleujin.rcraken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import junit.framework.TestCase;

public class TestLock extends TestCase {

	public void testLock() throws Exception {
		RedissonClient redisson = Redisson.create();

		ExecutorService epool = Executors.newFixedThreadPool(10) ;
		AtomicInteger ai = new AtomicInteger() ;
		RReadWriteLock rwlock = redisson.getReadWriteLock("lock");
		
		Runnable r = () -> {
			RLock lock = rwlock.writeLock();
			try {
				lock.lock(1, TimeUnit.SECONDS);
				Thread.sleep(new Random().nextInt(100));
				ai.incrementAndGet(); // do something
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				lock.unlock();
			}
		};
		
		List<Future> list = new ArrayList() ;
		for (int i = 0; i < 10 ; i++) {
			list.add(epool.submit(r)) ;
		}
		
		for (Future f : list) {
			f.get() ;
		}
		
		System.out.println(ai.get()) ;
		redisson.shutdown();
	}

	
	public void testLock2() throws Exception {
		RedissonClient redisson = Redisson.create();

		ExecutorService epool = Executors.newFixedThreadPool(10) ;
		AtomicInteger ai = new AtomicInteger() ;
		ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock() ;
		
		Runnable r = () -> {
			WriteLock lock = rwlock.writeLock() ;
			try {
				lock.lock();  
				Thread.sleep(new Random().nextInt(100));
				ai.incrementAndGet(); // do something
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				lock.unlock();
			}
		};
		
		List<Future> list = new ArrayList() ;
		for (int i = 0; i < 10 ; i++) {
			list.add(epool.submit(r)) ;
		}
		
		for (Future f : list) {
			f.get() ;
		}
		
		System.out.println(ai.get()) ;
		redisson.shutdown();
	}

}