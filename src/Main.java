import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
	/* params: 
	 * 	list_file msa_in_dir msa_out_dir matrix_file gop gep mode(CP/ILNS/HGA) mode_params_separated_by_underscore
	 * examples: 
	 * 	list.txt msa/ msa_new/ matrix/GONNET -12 -1 CP 2_120000
	 * 	list.txt msa/ msa_new/ matrix/GONNET -12 -1 ILNS 100_10000_1
	 * 	list.txt msa/ msa_new/ matrix/GONNET -12 -1 HGA 100_10000_1_5_10_10
	 */
	public static void main(String[] args) {
		ArrayList<File> listOfFiles = read_list(args[0]);
		int n = listOfFiles.size();
		for (int i = 0; i < n; i++) {
			if (listOfFiles.get(i).isFile()) {
		        String MSA_name = listOfFiles.get(i).getName();
		        run(args[1] + MSA_name, args[2] + MSA_name, 
		        		args[3], Float.parseFloat(args[4]), 
		        		Float.parseFloat(args[5]), args[6], args[7],
		        		(i+1)+"/"+n);
			}
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
	
	public static void run(String msa_input_file, String msa_output_file, 
			String matrix_file, float gop, float gep, String mode, String mode_params, String info){
		System.out.println("Realigning " + msa_input_file + "... (" + info + ")");
		
		Scoring scoring = new Scoring(gop, gep);
		scoring.readMatrix(matrix_file);
		
		MSAManipulator manipulator = new MSAManipulator();
		manipulator.readFile(msa_input_file);
		float ori_score = scoring.compute(manipulator.getMSA());
//		System.out.println("Ori Score: " + scoring.compute(manipulator.getMSA()));
		
		ArrayList<Integer[]> regions = manipulator.findPotentialRegions();
		int nr = regions.size();
		MSA[] msas = new MSA[nr];
		
		long total_time_elapsed = 0;
		
		for(int i=0; i<nr; i++){
			int a = regions.get(i)[0];
			int b = regions.get(i)[1];
			MSA msa = new MSA(manipulator.getSubMSA(a,b), scoring);
	        
			String[] params = mode_params.split("_");
	        long startTime = System.currentTimeMillis();
	        
	        if(mode.equals("CP")){
	        	int grouping = Integer.parseInt(params[0]);
	        	int timeout_per_region = Integer.parseInt(params[1]);
	        	if(grouping == 1)
	        		msa.group(MSA.GROUP_VERTICALLY);
	        	else if(grouping == 2)
	        		msa.group(MSA.GROUP_HORIZONTALLY);
	        	else if(grouping == 3)
	        		msa.group(MSA.GROUP_BOTH);
	        	msa.realign(timeout_per_region);
	        }else if(mode.equals("ILNS")){
	        	int n_point = Integer.parseInt(params[0]);
	        	int timeout = Integer.parseInt(params[1]);
	        	int lns_type = Integer.parseInt(params[2]);
	        	msa = LocalSearch.ilns(msa, n_point, timeout, (lns_type==0)? MSA.LNS_SINGLE : MSA.LNS_GROUP);
	        }else if(mode.equals("HGA")){
	        	int n_point = Integer.parseInt(params[0]);
	        	int timeout = Integer.parseInt(params[1]);
	        	int lns_type = Integer.parseInt(params[2]);
	        	int n_population = Integer.parseInt(params[3]);
	        	int n_generation = Integer.parseInt(params[4]);
	        	int max_no_change_generation = Integer.parseInt(params[5]);
	        	msa = LocalSearch.bohes(msa, n_point, timeout, (lns_type==0)? MSA.LNS_SINGLE : MSA.LNS_GROUP, 
	        			n_population, n_generation, max_no_change_generation);
	        }
	        
			long time_elapsed = (System.currentTimeMillis() - startTime);
	        total_time_elapsed += time_elapsed;
	        
	        msas[i] = msa;
		}
		
		manipulator.updateMSA(regions, msas);
		
		float new_score = scoring.compute(manipulator.getMSA());
//		System.out.println("New Score: " + scoring.compute(manipulator.getMSA()));
		manipulator.printFile(msa_output_file, ori_score, new_score, total_time_elapsed, nr);
		if(new_score > ori_score)
			System.out.println("Improved!");
		else System.out.println("Nothing improved!");
	}
	
}



