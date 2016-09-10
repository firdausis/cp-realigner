import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class MSAManipulator {
	String[] names, seqs;
	
	MSAManipulator(){
	}
	
	MSAManipulator(String[] _names, String[] _seqs){
		names = _names;
		seqs = _seqs;
	}
	
	void updateMSA(ArrayList<Integer[]> regions, MSA[] msas){
		int m = names.length;
		String[] new_seqs = new String[m];
		for(int i = 0; i < m; i++)
			new_seqs[i] = "";
		
		for(int i = 0; i < msas.length; i++){
			int a = 0; 
			if(i>0) a = regions.get(i-1)[1];
			int b = regions.get(i)[0];
			String[] msa = msas[i].getString();
			
			for(int j = 0; j < m; j++){
				String ori_s = seqs[j];
				String new_s = ori_s.substring(a, b-1);
				new_s += msa[j];
				new_seqs[j] += new_s;
			}
		}
		for(int j = 0; j < m; j++){
			int a = regions.get(msas.length-1)[1];
			String ori_s = seqs[j];
			String new_s = ori_s.substring(a);
			new_seqs[j] += new_s;
		}
		
		seqs = new_seqs;
	}
	
	String[] getSubMSA(int a, int b){
		String[] msa = new String[seqs.length];
		for(int i = 0; i < msa.length; i++){
			msa[i] = seqs[i].substring(a-1, b);
		}
		return msa;
	}
	
	String[] getMSA(){
		String[] msa = new String[seqs.length];
		for(int i = 0; i < msa.length; i++){
			msa[i] = seqs[i];
		}
		return msa;
	}
	
	ArrayList<Integer[]> findPotentialRegions(){
		ArrayList<Integer[]> regions = new ArrayList<Integer[]>();
		
		int m = names.length;
		int n = seqs[0].length();
		
		// find gapped columns
		boolean in = false;
		int a = 0, b = 0;
		for(int i = 0; i < n; i++){
			boolean gap_contained = false;
			for(int j = 0; j < m; j++)
				if(seqs[j].charAt(i) == '-')
					gap_contained = true;
			if(gap_contained){
				if(!in){
					in = true;
					a = i;
					b = i;
				}else{
					b = i;
					if(i == n-1){
						regions.add(new Integer[]{a + 1, b + 1});
					}
				}
			}else{
				if(in){
					in = false;
					regions.add(new Integer[]{a + 1, b + 1});
				}
			}
		}
		
//		for(int i = 0; i < regions.size(); i++){
//			System.out.print(regions.get(i)[0] + " ");
//			System.out.println(regions.get(i)[1]);
//		}
		
		// remove small regions
		ArrayList<Integer[]> new_regions = new ArrayList<Integer[]>();
		int nr = regions.size();
		for(int i = 0; i < nr; i++){
			if((regions.get(i)[1] - regions.get(i)[0]) > 0)
				new_regions.add(regions.get(i));
		}
		regions = new_regions;
		
		// remove small non regions
		new_regions = new ArrayList<Integer[]>();
		new_regions.add(regions.get(0));
		nr = regions.size();
		for(int i = 1; i < nr; i++){
			if((regions.get(i)[0] - regions.get(i-1)[1] - 1) < 7){
				int nnr = new_regions.size();
				int aa = new_regions.get(nnr-1)[0];
				new_regions.set(nnr-1, new Integer[]{aa, regions.get(i)[1]});
			}else new_regions.add(regions.get(i)); 
		}
		
		return new_regions;
//		return regions;
	}
	
	void print(){
		int m = names.length;
		for(int i = 0; i < m; i++){
			System.out.println(names[i]);
			System.out.println(seqs[i]);
		}
	}
	
	void printFile(String file_name, float ori_score, float new_score, 
			long total_time_elapsed, int region_n){
		try {
            FileWriter fileWriter = new FileWriter(file_name);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            int n = names.length;
    		for(int i = 0; i < n; i++){
    			bufferedWriter.write(names[i] + "\n");
    			bufferedWriter.write(seqs[i] + "\n");
    		}
            bufferedWriter.close();
        } catch(IOException ex) {
            System.out.println("Error writing to file " + file_name);
        }
		try {
            FileWriter fileWriter = new FileWriter(file_name+ ".log");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
    		bufferedWriter.write(ori_score + "\n");
    		bufferedWriter.write(new_score + "\n");
    		bufferedWriter.write(total_time_elapsed + "\n");
    		bufferedWriter.write(region_n + "\n");
            bufferedWriter.close();
        } catch(IOException ex) {
            System.out.println("Error writing to file " + file_name + ".log");
        }
	}
	
	void readFile(String file){
		ArrayList<String> name_list = new ArrayList<String>();
		ArrayList<String> seq_list = new ArrayList<String>();
		
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(file));
			while((line = br.readLine()) != null){
				if(line.charAt(0) == '>'){
					name_list.add(line);
					line = br.readLine();
					seq_list.add(line);
				}else {
					int last_index = seq_list.size() - 1; 
					seq_list.set(last_index, seq_list.get(last_index) + line);
				}
			};
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		int m = name_list.size();
		names = new String[m];
		seqs = new String[m];
		for(int i=0; i < m; i++){
			names[i] = name_list.get(i);
			seqs[i] = seq_list.get(i);
		}
		
	}
}
