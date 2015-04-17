package profiler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.lang.InterruptedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import soen487.wscat.service.*;

public class AnalyzeProfiler {
	
	public static final int TEST_RUNS = 10;
	public static final int REQUESTS  = 100;
	public static final long TIMEOUT  = 20; // seconds
	
	public static final String FILE   = "Hello";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		WSCatService service = new WSCatService();
		WSCat wscat = service.getWSCatPort();
		double[] avg  = new double[TEST_RUNS];
		
		Page wsdlPage = new Page(
				(HttpURLConnection) new URL("http://data.serviceplatform.org/wsdl_grabbing/opossum-wsdls/valid_WSDLs/4371_6320_AvailableVideoService.wsdl")
				.openConnection()
			);
		AnalyzeFile task = new AnalyzeFile(wscat, wsdlPage.getContent());
		
		// Executing
		for (int i = 0; i < TEST_RUNS; i++) {
			avg[i] = makeRequest(REQUESTS, task);
		}
		
		// Printing
		for (double avgTime : avg) {
			System.out.println("AVG Time for "+REQUESTS+" requests : "+avgTime/1000.0+" s");
		}
	}

	public static class AnalyzeFile implements Runnable {
		private WSCat service;
		private String wsdl;
		
		public AnalyzeFile(WSCat wscat, String wsdl) {
			this.service = wscat;
			this.wsdl = wsdl;
		}
		
		@Override
		public void run() {
			try {
				service.analyzeFile(wsdl);
			} catch (IOException_Exception e) {
				e.printStackTrace();
			} catch (InterruptedException_Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static class Request extends Thread {
		private Semaphore sem;
		private Runnable task;
		private Long time;
		
		public Request(Semaphore sem, Runnable task) {
			this.sem = sem;
			this.task = task;
		}
		
		@Override
		public void run() {
			try {
				sem.acquire();
				time = System.currentTimeMillis();
				task.run();
				time = System.currentTimeMillis() - time;
				sem.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public synchronized Long getElapsedTime(){
			return time;
		}
	}
	
	public static double makeRequest(int nRequests, Runnable task) throws InterruptedException{
		Semaphore sem = new Semaphore(nRequests);
		Request rq[] = new Request[nRequests];
		for (int i = 0; i < nRequests; i++) {
			rq[i] = new Request(sem, task);
			rq[i].start();
		}
		Thread.sleep(1000); // Yield
		sem.tryAcquire(nRequests, TIMEOUT, TimeUnit.SECONDS);
		
		long avg = 0;
		for (int i = 0; i < nRequests; i++) {
			Long time = rq[i].getElapsedTime();
			if(time != null)
				avg += time;
			else
				nRequests--;					
		}
		return ((double) avg) / nRequests; 
	}
}
