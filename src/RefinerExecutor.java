import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RefinerExecutor {
	
	public static void main(String[] args) {
		ArrayList<File> listOfFiles = read_list("list.txt");
		int n = listOfFiles.size();
		
		System.out.println("Id,Time,Impr SP,Impr TC");

		for (int i = 0; i < n; i++) {
			if (listOfFiles.get(i).isFile()) {
				String MSA_name = listOfFiles.get(i).getName();
		        System.out.print(MSA_name);
				//align(MSA_name);
		        
		        long startTime = System.currentTimeMillis();
				convert_msa(MSA_name);
				String file = realign(MSA_name);
				System.out.print("," + (System.currentTimeMillis() - startTime));
				
				convert_new_msa(file, MSA_name);
				score(MSA_name);
				System.out.println();
		    }
		}
	}
	
	private static double round(double n){
		return (double) Math.round(n * 100) / 100;
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
	
	public static void score(String msa_name) {
		Runtime runtime = Runtime.getRuntime();
		double ori_q = 0, ori_tc = 0, new_q = 0, new_tc = 0;
		try {
			Process p1 = runtime.exec("D:/Kuliah/UNL/Project/resources/qscore/Debug/qscore"
					+ " -test msa/" + msa_name
					+ " -ref D:/Kuliah/UNL/Project/resources/bench1.0/bali3/ref/" + msa_name);
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = br.readLine();
		    String words[] = line.split(";");
		    ori_q = Double.parseDouble(words[2].substring(2, words[2].length()));
		    ori_tc = Double.parseDouble(words[3].substring(3, words[3].length()));
//		    System.out.print("," + ori_q);
//		    System.out.print("," + ori_tc);
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
		try {
			Process p1 = runtime.exec("D:/Kuliah/UNL/Project/resources/qscore/Debug/qscore"
					+ " -test msa_new/REFINER/" + msa_name
					+ " -ref D:/Kuliah/UNL/Project/resources/bench1.0/bali3/ref/" + msa_name);
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = br.readLine();
		    String words[] = line.split(";");
		    new_q = Double.parseDouble(words[2].substring(2, words[2].length()));
		    new_tc = Double.parseDouble(words[3].substring(3, words[3].length()));
//		    System.out.print("," + new_q);
//		    System.out.print("," + new_tc);
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
		System.out.print("," + round(100 * (new_q - ori_q)));
		System.out.print("," + round(100 * (new_tc - ori_tc)));
	}
	
	public static void align(String msa_name) {
		Runtime runtime = Runtime.getRuntime();
		try {
			Process p1 = runtime.exec("D:/Kuliah/UNL/Project/resources/clustal-omega-1.2.0-win32/clustalo.exe -i input/" 
		    				+ msa_name + " -o msa/" + msa_name + " --force --guidetree-out=msa/" + msa_name + ".gt");
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = null;
		    while( (line = br.readLine()) != null) {
		        System.out.println(line);
		    }
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
	}
	
	public static void convert_msa(String msa_name) {
		Runtime runtime = Runtime.getRuntime();
		
		try {
		    Process p1 = runtime.exec("D:/Kuliah/UNL/Project/resources/REFINER/fa2cd -i msa/" + msa_name + " -parseIds -o msa/cd/" + msa_name);
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = null;
		    while( (line = br.readLine()) != null) {
		    	//System.out.println(line);
		    }
		    
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
	}
	
	public static void convert_new_msa(String file, String msa_name) {
		Runtime runtime = Runtime.getRuntime();
		
		String msa = "";
		try {
		    Process p1 = runtime.exec("D:/Kuliah/UNL/Project/resources/REFINER/cddalignview -type FASTA -lefttails -righttails " + file);
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = null;
		    while( (line = br.readLine()) != null) {
		    	//System.out.println(line);
		    	if((line.length()>=4) && line.substring(0, 4).equals(">lcl")) msa += (">" + line.substring(5, line.length()));
		    		else msa += line;
		    	msa += "\n";
		    }
		    //System.out.println(msa);
		    
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
		write_to_file("msa_new/REFINER/" + msa_name, msa);
	}
	
	public static String realign(String msa_name) {
		
		Runtime runtime = Runtime.getRuntime();
		String fileName = "";
		
		try {
		    Process p1 = runtime.exec(""
		    		+ "D:/Kuliah/UNL/Project/resources/REFINER/bma_refiner_Aug2015 -i msa/cd/" 
		    		+ msa_name + ".cn3 -so 2 -o msa_new/REFINER/cd/" + msa_name);
		    InputStream is = p1.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    
		    String line = null;
		    
		    while( (line = br.readLine()) != null) {
		    	//System.out.println(line);
		    	String[] words = line.split(" ");
//		    	if(words[0].equals("Rows") && words[1].equals("in"))
//		    		System.out.print("," + words[4]);
//		    	else if(words[0].equals("Alignment") && words[1].equals("width"))
//		    		System.out.print("," + words[5]);
//		    	else if(words[0].equals("****"))
//		    		System.out.print("," + words[5]);
//		    	else 
		    	if((words.length>=5) && words[4].equals("(written"))
		    		fileName = words[7].substring(1, words[7].length()-2);
		    }
		} catch(IOException ioException) {
		    System.out.println(ioException.getMessage());
		}
		
		return fileName;
	}
	
	public static void write_to_file(String file_name, String content){
		try {
            FileWriter fileWriter = new FileWriter(file_name);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.close();
        } catch(IOException ex) {
            System.out.println("Error writing to file " + file_name);
        }
	}
	
}


