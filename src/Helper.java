import java.util.Random;

public class Helper {
	
	public static void main(String[] args) {
		int[] r = generateNRandomNumber(1, 5, 3);
		for(int i=0; i < r.length; i++)
			System.out.println(r[i]);
	}
	
	public static int[] generateNRandomNumber(int start, int end, int count){
		Random rng = new Random();

	    int[] result = new int[count];
	    int cur = 0;
	    int remaining = end - start;
	    for (int i = start; i < end && count > 0; i++) {
	        double probability = rng.nextDouble();
	        if (probability < ((double) count) / (double) remaining) {
	            count--;
	            result[cur++] = i;
	        }
	        remaining--;
	    }
	    return result;
	}
	
	public static int[] generateNRandomNumberAndShuffle(int start, int end, int count){
	    int[] result = generateNRandomNumber(start, end, count);
	    for(int i=0; i < result.length; i++){
	    	int r1 = generateRandomNumber(0, result.length);
	    	int r2 = generateRandomNumber(0, result.length);
	    	int t = result[r1];
	    	result[r1] = result[r2];
	    	result[r2] = t;
	    }
	    return result;
	}
	
	public static int generateRandomNumber(int start, int end){
		Random rnd = new Random();
		return start + rnd.nextInt(end-start);
	}
}
