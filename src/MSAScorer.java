import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class MSAScorer {

	public static void main(String[] args) {
		Scoring scoring = new Scoring((float)-15.0, (float)-1);
		scoring.readMatrix("matrix/PAM250");
		
		ArrayList<File> listOfFiles = read_list("list.txt");
		int n = listOfFiles.size();
		for (int i = 0; i < n; i++) {
			if (listOfFiles.get(i).isFile()) {
		        String MSA_name = listOfFiles.get(i).getName();
				
				MSAManipulator manipulator = new MSAManipulator();
				manipulator.readFile("ref/" + MSA_name);
				float ref_score = scoring.compute(manipulator.getMSA());
				
				System.out.println(ref_score);
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

	
	
}
