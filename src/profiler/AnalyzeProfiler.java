package profiler;

import soen487.wscat.service.*;

public class AnalyzeProfiler {
	
	public static final int TEST_RUNS = 10;
	public static final int REQUESTS  = 100;
	
	public static final String FILE   = "Hello";
	
	public static void main(String[] args) {
		WSCatService service = new WSCatService();
		WSCat wscat = service.getWSCatPort();
		long[] times= new long[REQUESTS];
		long[] avg  = new long[TEST_RUNS];
		
		for (int i = 0; i < TEST_RUNS; i++) {
			for (int j = 0; j < REQUESTS; j++) {
				times[j] = timeAnalyze(wscat, FILE);
				avg[i]  += times[j];
			}
			avg[i] /= REQUESTS;
		}
		for (long avgTime : avg) {
			System.out.println("AVG Time for "+REQUESTS+" requests : "+avgTime/1000.0+" s");
		}
	}
	
	public static long timeAnalyze(WSCat wscat, String file){
		long time = System.currentTimeMillis();
		try {
			wscat.analyzeFile(file);
		} catch (IOException_Exception e) {
		} catch (InterruptedException_Exception e) {
		}
		return System.currentTimeMillis() - time;
	}

}
