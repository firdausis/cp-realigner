import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Scoring {
	float gop, gep;
	int[][] matrix;
	
	Scoring(float _gop, float _gep){
		gop = _gop;
		gep = _gep;
		matrix = new int['Z'-'A' + 1]['Z'-'A' + 1];
		for(int i=0; i < matrix.length; i++)
			for(int j=0; j < matrix[0].length; j++)
				matrix[i][j] = 0;
	}
	
	void readMatrix(String file){
		char[] char_order;
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			String[] w = line.trim().split(" +");
			char_order = new char[w.length - 1];
			for(int i = 0; i < w.length - 1; i++){
				char_order[i] = w[i].charAt(0);
			}
			int i = 0;
			while (i < char_order.length) {
				line = br.readLine();
				w = line.trim().split(" +");			
				for(int j = 0; j <  char_order.length; j++){
					int n = Integer.parseInt(w[j]);
					setMatrixScore(char_order[i], char_order[j], n);
				}
				i++;
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
	}
	
	void setMatrixScore(char x, char y, int z){
		matrix[x - 'A'][y - 'A'] = z;
	}
	
	int getMatrixScore(char x, char y){
		return matrix[x - 'A'][y - 'A'];
	}
	
	float getScore(char x, char y){
		if((x != '-') && (y != '-')) return getMatrixScore(x,y);
		else if((x == '-') && (y == '-')) return 0;
		else return gop;
	}
	
	float getScore(char x, char y, char xb, char yb){
		if((xb != '-') && (yb != '-')) return getScore(x, y);
		else if((x != '-') && (y != '-')) return getMatrixScore(x,y);
		else if((x == '-') && (y != '-')) return (xb == '-')? gep: gop;
		else if((x != '-') && (y == '-')) return (yb == '-')? gep: gop;
		else return 0;
	}
	
	public float compute(String[] s){
		int n = s[0].length();
		int m = s.length;
		
		float score = 0;
		// sum of pairs
		for(int i = 0; i < n; i++)
			for(int j = 0; j < m - 1; j++)
				for(int k = j + 1; k < m; k++){
					score += (i==0)? getScore(s[j].charAt(i), s[k].charAt(i))
							: getScore(s[j].charAt(i), s[k].charAt(i), s[j].charAt(i-1), s[k].charAt(i-1));
				}
		
		return score;
	}
	
}
