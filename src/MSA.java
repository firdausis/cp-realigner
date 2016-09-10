import java.util.ArrayList;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

import static org.chocosolver.solver.search.strategy.Search.*;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;


public class MSA {
	private String[] seqs, pre_seqs, post_seqs;
	private float score;
	private Scoring scoring;
	private int[][] grouping_matrix;
	
	public static int NO_GROUPING = -1;
	public static int GROUP_VERTICALLY = 0;
	public static int GROUP_HORIZONTALLY = 1;
	public static int GROUP_BOTH = 2;
	
	public static int LNS_SINGLE = 0;
	public static int LNS_GROUP = 1;
	
	MSA(String[] _seqs, Scoring _scoring){
		this(_seqs, null, null, _scoring);
	}
	
	MSA(String[] _seqs, String[] _pre_seqs, String[] _post_seqs, Scoring _scoring){
		seqs = _seqs;
		if(_pre_seqs == null){
			pre_seqs = new String[_seqs.length];
			for(int i=0; i<_seqs.length; i++)
				pre_seqs[i] = "";
		}else pre_seqs = _pre_seqs;
		
		if(_post_seqs == null){
			post_seqs = new String[_seqs.length];
			for(int i=0; i<_seqs.length; i++)
				post_seqs[i] = "";
		}else post_seqs = _post_seqs;
		
		scoring = _scoring;
		score = scoring.compute(getFullSeqs(seqs));		
		
		grouping_matrix = new int[seqs.length][seqs[0].length()];
		for(int i=0; i<grouping_matrix.length; i++)
			for(int j=0; j<grouping_matrix[0].length; j++)
				grouping_matrix[i][j] = 0;
	}
	
	private String[] getFullSeqs(String[] _seqs){
		String[] full_seqs = new String[_seqs.length];
		for(int i=0; i<_seqs.length; i++){
			full_seqs[i] = pre_seqs[i] + _seqs[i] + post_seqs[i];
		}
		return full_seqs;
	}
	
	public void print(){
		for(int i = 0; i < seqs.length; i++)
        	System.out.println(seqs[i]);
	}
	
	public Scoring getScoring(){
		return scoring;
	}
	
	public String[] getString(){
		return seqs;
	}
	
	public float getScore(){
		return score;
	}
	
	public int getLength(){
		return seqs[0].length();
	}
	
	public int getNumber(){
		return seqs.length;
	}
	
	public int[][] group(int type){
		int last_group = 1;
		int m = seqs.length;
		int n = seqs[0].length();
		
		grouping_matrix = new int[seqs.length][seqs[0].length()];
		for(int i=0; i<grouping_matrix.length; i++)
			for(int j=0; j<grouping_matrix[0].length; j++)
				grouping_matrix[i][j] = 0;
		
		if(type == GROUP_HORIZONTALLY){
			// horizontal grouping
			boolean hold;
			
			for(int i = 0; i < m; i++){
				hold = false;
		       	for(int j = 0; j < n; j++)
		       		if(seqs[i].charAt(j) != '-'){
		       			if(!hold){
		       				hold = true;
		       				last_group++;
		       			}
		       			grouping_matrix[i][j] = last_group;
		       		}else{
		       			if(hold) hold = false;
		       		}
			}
			
		}else if(type == GROUP_VERTICALLY){
			// vertical grouping
			boolean hold = false;
				
			for(int j=0; j<n; j++){
				int n_ngap = 0;
				for(int i=0; i<m; i++)
					if(seqs[i].charAt(j) != '-')
						n_ngap++;
					
				if(n_ngap > m/2){
					if(!hold){
	        			hold = true;
	        			last_group++;
	        		}
					for(int i=0; i<m; i++)
						if(seqs[i].charAt(j) != '-')
							grouping_matrix[i][j] = last_group;
				}else{
	        		if(hold) hold = false;
	        	}
			}
		}else if(type == GROUP_BOTH){
			// vertical grouping
			boolean hold = false;
			
			for(int j=0; j<n; j++){
				int n_ngap = 0;
				for(int i=0; i<m; i++)
					if(seqs[i].charAt(j) != '-')
						n_ngap++;
					
				if(n_ngap > m/2){
					if(!hold){
	        			hold = true;
	        			last_group++;
	        		}
					for(int i=0; i<m; i++)
						if(seqs[i].charAt(j) != '-')
							grouping_matrix[i][j] = last_group;
				}else{
	        		if(hold) hold = false;
	        	}
			}
			
			// horizontal grouping
			for(int i = 0; i < m; i++){
				hold = false;
		       	for(int j = 0; j < n; j++)
		       		if((seqs[i].charAt(j) != '-') && (grouping_matrix[i][j]==0)){
		       			if(!hold){
		       				hold = true;
		       				last_group++;
		       			}
		       			grouping_matrix[i][j] = last_group;
		       		}else{
		       			if(hold) hold = false;
		       		}
			}
			
		}
		
//		for(int i=0; i<m; i++){
//			for(int j=0; j<n; j++)
//				System.out.print(grouping_matrix[i][j]);
//			System.out.println();
//		}
		
		return grouping_matrix;
	}
	
	public void realign(long time_limit){
        int m = seqs.length;
        int[] l = new int[m];
        
        // construct unaligned seqs
        String[] unseqs = new String[m];
        for(int i = 0; i < m; i++){
        	unseqs[i] = "";
        	for(int j = 0; j < seqs[i].length(); j++)
        		if(seqs[i].charAt(j) != '-')
        			unseqs[i] += seqs[i].charAt(j);
        }
        
        // range of alignment length
        for(int i = 0; i < m; i++)
        	l[i] = unseqs[i].length();
        int sum_l = l[0], max_l = l[0];
        for(int i = 1; i < m; i++){
        	sum_l += l[i];
        	if(l[i] > max_l) max_l = l[i];
        }
        
        // initial alignment score
        score = scoring.compute(getFullSeqs(seqs));
        
        Model model = new Model("realigner");
        IntVar[][] x = model.intVarMatrix("x", m, max_l, 1, sum_l);
        IntVar size = model.intVar("n", max_l, sum_l);
        
        for(int i = 0; i < m; i++){
        	// increasing constraints
        	for(int j = 0; j < l[i] - 1; j++)
        		model.arithm(x[i][j], "<", x[i][j+1]).post();
        	// fix dummy vars
        	for(int j = l[i]; j < max_l; j++)
        		model.arithm(x[i][j], "=", 1).post();
        }
        
        // grouping constraints
        int len = seqs[0].length();
        for(int i = 0; i < m; i++){
        	int g = 0;
        	for(int k = 0; k < len-1; k++){
    			if(seqs[i].charAt(k) == '-')
    				g++;
    			if(grouping_matrix[i][k] != 0)
        			if(grouping_matrix[i][k] == grouping_matrix[i][k+1])
        				model.arithm(model.intOffsetView(x[i][k-g],1), "=", x[i][k-g+1]).post();
        	}
        }
        for(int i = 0; i < m - 1; i++)
        	for(int j = i + 1; j < m; j++){
        		int ig = 0, jg = 0;
        		for(int k = 0; k < len-1; k++){
        			if(seqs[i].charAt(k) == '-')
        				ig++;
        			if(seqs[j].charAt(k) == '-')
        				jg++;
	        		if((grouping_matrix[i][k] != 0) 
	        				&& (grouping_matrix[i][k] == grouping_matrix[j][k]))
	        			model.arithm(x[i][k-ig], "=", x[j][k-jg]).post();
        		}
        	}
        
        // set of all positions
        IntVar[] all_p = new IntVar[sum_l];
        int i_all = 0;
        for(int i = 0; i < m; i++)
        	for(int j = 0; j < l[i]; j++)
        		all_p[i_all++] = model.intOffsetView(x[i][j],0);
        
        // avoid making a column only containing gaps
    	Constraint avoid_c = model.atLeastNValues(all_p, size, false);
    	model.post(avoid_c);
    	
    	// limit domains
    	for(int i = 0; i < m; i++)
        	for(int j = 0; j < l[i]; j++)
        		model.arithm(x[i][j], "<=", size).post();
    	
    	// search strategy
    	model.getSolver().setSearch(intVarSearch( new FirstFail(model), new IntDomainMin(), all_p));
    	
    	// adjust max length of MSA because of vertical grouping
    	int dec_length = 0;
    	for(int i=0; i<len; i++){
    		int[] c = new int[m-1];
    		for(int j=0; j<m-1; j++){
    			c[j] = 0;
    			for(int k=j+1; k<m; k++)
    				if((grouping_matrix[j][i]!=0) 
    						&& (grouping_matrix[j][i]==grouping_matrix[k][i]))
    					c[j]++;
    		}
    		int max_c = c[0];
    		for(int j=1; j<m-1; j++)
    			if(c[j] > max_c)
    				max_c = c[j];
    		dec_length += max_c;
    	}
    	sum_l -= dec_length;
    	
//    	for(int i=0; i<m; i++){
//    		for(int j=0; j<len; j++)
//    			System.out.print(grouping_matrix[i][j]);
//    		System.out.println();
//    	}
    	
    	model.getSolver().limitTime(time_limit);
    	
    	long startTime = System.currentTimeMillis();
    	boolean time2break = false;
        for(int n = max_l; n <= sum_l; n++){
        	// setting the size
        	Constraint size_c = model.arithm(size, "=", n);
        	model.post(size_c);
	        
	        // solving
	        while(model.getSolver().solve()){
		        String[] new_s = form_MSA(unseqs, x, l, m, n);
		        float new_score = scoring.compute(getFullSeqs(new_s));
		        
//		        for(int i = 0; i < columns_to_relax.length; i++)
//		        	System.out.print(columns_to_relax[i] + " ");
//		        System.out.println();
		        	
//		        for(int i = 0; i < new_s.length; i++)
//		        	System.out.println(pre_seqs[i] + new_s[i] + post_seqs[i]);
//		        System.out.println(new_score);
//		        System.out.println();
		        
		        if(new_score > score){
		        	score = new_score;
		        	seqs = new_s;
		        }
		        
		        long elapsed_time = System.currentTimeMillis() - startTime;
		        if(elapsed_time >= time_limit){
		        	time2break = true;
		        	break;
		        }
	        }
	        if(time2break) break;
	        
	        model.unpost(size_c);
        	model.getSolver().reset();
        }
	}
	
	public void lns(ArrayList<int[]> relax_set, long time_limit, int lns_type){
		int m = seqs.length;
		int n = seqs[0].length();
		int[] l = new int[m];
		
		// construct unaligned seqs
        String[] unseqs = new String[m];
        for(int i = 0; i < m; i++){
        	unseqs[i] = "";
        	for(int j = 0; j < seqs[i].length(); j++)
        		if(seqs[i].charAt(j) != '-')
        			unseqs[i] += seqs[i].charAt(j);
        }
        
        // sum of sequence length
        for(int i = 0; i < m; i++)
        	l[i] = unseqs[i].length();
        int max_l = l[0];
        for(int i = 1; i < m; i++){
        	if(l[i] > max_l) max_l = l[i];
        }
        
        // initial alignment score
        score = scoring.compute(getFullSeqs(seqs));
        
        Model model = new Model("realigner");
        IntVar[][] x = model.intVarMatrix("x", m, max_l, 1, n);
        
        for(int i = 0; i < m; i++){
        	// increasing constraints
        	for(int j = 0; j < l[i] - 1; j++)
        		model.arithm(x[i][j], "<", x[i][j+1]).post();
        	// fix dummy vars
        	for(int j = l[i]; j < max_l; j++)
        		model.arithm(x[i][j], "=", 1).post();
        }
        
        // set of all positions
//        IntVar[] all_p = new IntVar[sum_l];
//        int i_all = 0;
//        for(int i = 0; i < m; i++)
//        	for(int j = 0; j < l[i]; j++)
//        		all_p[i_all++] = model.intOffsetView(x[i][j],0);
        
        // avoid making a column only containing gaps
//    	Constraint avoid_c = model.atLeastNValues(all_p, size, false);
//    	model.post(avoid_c);
    	
    	// fix variables
    	if(lns_type == LNS_SINGLE){
	    	for(int i = 0; i < m; i++){
	    		int g = 0;
	        	for(int j = 0; j < n; j++){
	        		if(seqs[i].charAt(j)=='-')
	        			g++;
	        		else {
	        			boolean skip = false;
	        			int s = relax_set.size();
	        			for(int k = 0; k < s; k++)
	        				if((i==relax_set.get(k)[0]) && (j==relax_set.get(k)[1])){ 
	        					skip = true;
	        					break;
	        				}
	        			if(!skip) model.arithm(x[i][j-g], "=", j + 1).post();
	        		}
	        	}
	    	}
    	}else if(lns_type == LNS_GROUP){
    		for(int i = 0; i < m; i++){
	    		int g = 0;
	        	for(int j = 0; j < n; j++){
	        		if(seqs[i].charAt(j)=='-')
	        			g++;
	        		else {
	        			boolean skip = false;
	        			int s = relax_set.size();
	        			for(int k = 0; k < s; k++)
	        				if((i==relax_set.get(k)[0]) 
	        						&& (j>=relax_set.get(k)[1])
	        						&& (j<=relax_set.get(k)[2])){ 
	        					skip = true;
	        					if(j > relax_set.get(k)[1])
	        						model.arithm(model.intOffsetView(x[i][j-g-1],1), "=", x[i][j-g]).post();
	        					break;
	        				}
	        			if(!skip) model.arithm(x[i][j-g], "=", j + 1).post();
	        		}
	        	}
	    	}
    	}
    	
    	model.getSolver().limitTime(time_limit);
    	
    	// solving
        while(model.getSolver().solve()){
	        String[] new_s = form_MSA(unseqs, x, l, m, n, true);
	        float new_score = scoring.compute(getFullSeqs(new_s));
	        	
//	        for(int i = 0; i < new_s.length; i++)
//	        	System.out.println(pre_seqs[i] + new_s[i] + post_seqs[i]);
//	        System.out.println(new_score);
//	        System.out.println();
	        
	        if(new_score > score){
	        	score = new_score;
	        	seqs = new_s;
	        }
        }
    	
	}
	
	private String[] form_MSA(String[] unseqs, IntVar[][] x, int[] l, int m, int n){
		return form_MSA(unseqs, x, l, m, n, false);
	}
	
	private String[] form_MSA(String[] unseqs, IntVar[][] x, int[] l, int m, int n, boolean is_pseudo){
		String[] new_s = new String[m];
		for(int i = 0; i < m; i++)
			new_s[i] = "";
		
    	for(int i = 0; i < m; i++){
    		int j = 0, k = 0;
    		while(j < n){
	        	if(k < l[i] && (x[i][k].getValue()-1) == j)
	        		new_s[i] += unseqs[i].charAt(k++);
	        	else new_s[i] += "-";
	        	j++;
	        }
    	}
    	
    	if(is_pseudo){
    		String[] t_s = new String[m];
    		for(int i = 0; i < m; i++){
    			t_s[i] = new_s[i];
    			new_s[i] = "";
    		}
    		int len = t_s[0].length();
    		for(int i=0; i < len; i++){
    			boolean all_gap = true;
    			for(int j=0; j < m; j++)
    				if(t_s[j].charAt(i) != '-'){
    					all_gap = false;
    					break;
    				}
    			if(!all_gap)
    				for(int j=0; j < m; j++)
    					new_s[j] += t_s[j].charAt(i);
    		}
    	}
    	
        return new_s;
	}

}
