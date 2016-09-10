import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class QScore {
	
	private static String out_dir = "msa_new/GONNET_ILNS_0/";
	private static String[] params = 
			new String[] {"110/", "120/", "130/", "140/", "150/",
							"160/", "170/", "180/", "190/", "200/"};
	private static int runs = 5;

	public static void main(String[] args) {
		ArrayList<File> listOfFiles = read_list("list.txt");
		int n = listOfFiles.size();
		for (int i = 0; i < n; i++) {
			if (listOfFiles.get(i).isFile()) {
		        String MSA_name = listOfFiles.get(i).getName();
		        if((runs==1) || (params.length==0)){
		        	double[] scores = score("msa/", out_dir, MSA_name);
		        	writeScore(out_dir + MSA_name + ".qscore.log", scores);
		        }else{
		        	for(int j=0; j<params.length; j++)
			        	for(int k=1; k<=runs; k++){
			        		String out = out_dir + params[j] + k + "/";
			        		double[] scores = score("msa/", out, MSA_name);
				        	writeScore(out + MSA_name + ".qscore.log", scores);
			        	}
		        }
			}
		}
		System.out.println("Done");
	}
	
	public static double[] score(String dir1, String dir2, String msa_name) {
		Runtime runtime = Runtime.getRuntime();
		double ori_q = 0, ori_tc = 0, new_q = 0, new_tc = 0;
		try {
			Process p1 = runtime.exec("D:/Kuliah/UNL/Project/resources/qscore/Debug/qscore"
					+ " -test " + dir1 + msa_name
					+ " -ref D:/Kuliah/UNL/Project/resources/bench1.0/bali3/ref/" + msa_name);
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = br.readLine();
		    String words[] = line.split(";");
		    ori_q = Double.parseDouble(words[2].substring(2, words[2].length()));
		    ori_tc = Double.parseDouble(words[3].substring(3, words[3].length()));
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
		try {
			Process p1 = runtime.exec("D:/Kuliah/UNL/Project/resources/qscore/Debug/qscore"
					+ " -test " + dir2 + msa_name
					+ " -ref D:/Kuliah/UNL/Project/resources/bench1.0/bali3/ref/" + msa_name);
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = br.readLine();
		    String words[] = line.split(";");
		    new_q = Double.parseDouble(words[2].substring(2, words[2].length()));
		    new_tc = Double.parseDouble(words[3].substring(3, words[3].length()));
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
		return new double[]{ori_q,new_q,ori_tc,new_tc};
	}
	
	public static void writeScore(String file_name, double[] scores){
		try {
            FileWriter fileWriter = new FileWriter(file_name);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
    		bufferedWriter.write(scores[0] + "\n");
    		bufferedWriter.write(scores[1] + "\n");
    		bufferedWriter.write(scores[2] + "\n");
    		bufferedWriter.write(scores[3] + "\n");
            bufferedWriter.close();
        } catch(IOException ex) {
            System.out.println("Error writing to file " + file_name + ".log");
        }
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
