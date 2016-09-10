import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ComputeStats {

	public static void main(String[] args) throws IOException {
		String dir = "msa_new/GONNET_ILNS_0/";
		String result_name = "GONNET_ILNS_0_";
		
		for(int i=1; i<=10; i++)
			execute(dir + (100+i*10) + "/", result_name + (100+i*10), 5);
//		execute(dir, result_name, 1);
	}
	
	public static void execute(String dir, String result_name, int runs) throws IOException{
		double[] total_stats = new double[]{0,0,0,0};
		double[] min_stats = new double[]{Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
		double[] max_stats = new double[]{Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE};
		
		
		FileWriter fileWriter = new FileWriter("result/" + result_name + ".csv");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write("Id,Impr Obj,Impr SP,Impr TC,Time\n");
		
		ArrayList<File> listOfFiles = read_list("list.txt");
		int n = listOfFiles.size();
		
		for (int i = 0; i < n; i++) {
			if (listOfFiles.get(i).isFile()) {
		        String MSA_name = listOfFiles.get(i).getName();
		        
		        for(int k=1; k<=runs; k++){
			        double[] stats;
			        if(runs==1)
			        	stats = compute_stats(dir, MSA_name);
			        else stats = compute_stats(dir + k + "/", MSA_name);
			        
			        for(int j = 0; j < 4; j++){
			        	total_stats[j] += stats[j];
			        	if(stats[j] < min_stats[j])
			        		min_stats[j] = stats[j];
			        	if(stats[j] > max_stats[j])
			        		max_stats[j] = stats[j];
			        }
			        bufferedWriter.write(MSA_name
			        		+ "," + round(stats[0]) 
			        		+ "," + round(stats[1])
			        		+ "," + round(stats[2])
			        		+ "," + round(stats[3]) + "\n");
		        }
			}
		}
		
		bufferedWriter.close();
		
		n = n * runs;
		
		fileWriter = new FileWriter("result/" + result_name + ".summary");
        bufferedWriter = new BufferedWriter(fileWriter);
        
        bufferedWriter.write("Obj score imprv\n");
        bufferedWriter.write("Min " + round(min_stats[0]) + "%\n");
        bufferedWriter.write("Max " + round(max_stats[0]) + "%\n");
        bufferedWriter.write("Avg " + round(total_stats[0]/n) + "%\n");
        
        bufferedWriter.write("\nSP score imprv\n");
        bufferedWriter.write("Min " + round(min_stats[1]) + "%\n");
        bufferedWriter.write("Max " + round(max_stats[1]) + "%\n");
        bufferedWriter.write("Avg " + round(total_stats[1]/n) + "%\n");
        
        bufferedWriter.write("\nTC score imprv\n");
        bufferedWriter.write("Min " + round(min_stats[2]) + "%\n");
        bufferedWriter.write("Max " + round(max_stats[2]) + "%\n");
        bufferedWriter.write("Avg " + round(total_stats[2]/n) + "%\n");
		
        bufferedWriter.write("\nTime\n");
        bufferedWriter.write("Min " + round(min_stats[3]) + " ms\n");
        bufferedWriter.write("Max " + round(max_stats[3]) + " ms\n");
        bufferedWriter.write("Avg " + round(total_stats[3]/n) + " ms\n");
        
        bufferedWriter.close();
        System.out.println("Done");
	}
	
	private static double round(double n){
		return (double) Math.round(n * 100) / 100;
	}
	
	public static double[] compute_stats(String dir, String name){
		double[] stats = new double[4];
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dir + name + ".log"));
			float ori_obj_score, new_obj_score;
			ori_obj_score = Float.parseFloat(br.readLine());
			new_obj_score = Float.parseFloat(br.readLine());
			stats[0] = ((new_obj_score - ori_obj_score) / (float)Math.abs(ori_obj_score)) * 100;
			stats[3] = Integer.parseInt(br.readLine());
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		br = null;
		try {
			br = new BufferedReader(new FileReader(dir + name + ".qscore.log"));
			double ori_sp_score, new_sp_score, ori_tc_score, new_tc_score;
			ori_sp_score = Double.parseDouble(br.readLine());
			new_sp_score = Double.parseDouble(br.readLine());
			ori_tc_score = Double.parseDouble(br.readLine());
			new_tc_score = Double.parseDouble(br.readLine());
			stats[1] = (new_sp_score - ori_sp_score) * 100;  
			stats[2] = (new_tc_score - ori_tc_score) * 100;
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return stats;
	}
	
	public static ArrayList<File> read_list(String list_file) {
		ArrayList<File> files = new ArrayList<File>();
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(list_file));
			while ((line = br.readLine()) != null) {
				if(!line.equals("")){
					files.add(new File("msa/" + line));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return files;
	}

}
