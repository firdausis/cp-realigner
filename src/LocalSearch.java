import java.util.ArrayList;

public class LocalSearch {
	
	public static void main(String[] args) {
		Scoring scoring = new Scoring(-6, -1);
		scoring.readMatrix("matrix/GONNET");
		
		MSA msa = new MSA(
				new String[]{"G---",
							 "-GG-",
							 "-GGG"},
							 scoring);
		MSA[] msas = initPopulation(5, msa);
		
//		msa.print();
//		System.out.println(msa.getScore());
//		msa = lns(msa, 1, 30000, MSA.LNS_GROUP);
//		msa.print();
//		System.out.println(msa.getScore());
	}
	
	public LocalSearch(){
	}
	
	public static MSA bohes(MSA init_msa, int n_point, int timeout_per_point, 
			int lns_type, int np, int max_gens, int max_no_change){
		MSA[] population = initPopulation(np, init_msa);
		for(int i = 0; i < np; i++){
			population[i] = ilns(population[i], n_point, timeout_per_point, lns_type);
		}
		
		population = orderPopulation(population);
		
		float last_best_score = population[0].getScore();
		int no_change = 0;
		
		for(int i = 0; i < max_gens; i++){
			int[] r = Helper.generateNRandomNumber(0, np, 2);
			MSA[] children = crossover(population[r[0]], population[r[1]]);
			
			MSA best_child = children[0];
			for(int j = 1; j < children.length; j++){
				if(children[j].getScore() > best_child.getScore())
					best_child = children[j]; 
			}
			if((best_child.getScore() > population[r[0]].getScore())
					&& (best_child.getScore() > population[r[1]].getScore())){
				best_child = ilns(best_child, n_point, timeout_per_point, lns_type);
				population = updatePopulation(population, new MSA[]{best_child});
			}
			
			if(population[0].getScore() > last_best_score){
				last_best_score = population[0].getScore();
				no_change = 0;
			} else no_change++;
			if(no_change > max_no_change)
				break;
		}
		
		return population[0];
	}
	
	public static MSA hes(MSA init_msa, int n_point, int timeout_per_point, int lns_type,
			int np, int max_gens, int max_no_change){
		MSA[] population = initPopulation(np, init_msa);
		for(int i = 0; i < np; i++){
			population[i] = ilns(population[i], n_point, timeout_per_point, lns_type);
		}
		
		population = orderPopulation(population);
		
		float last_best_score = population[0].getScore();
		int no_change = 0;
		
		for(int i = 0; i < max_gens; i++){
			int r1 = Helper.generateRandomNumber(0, np/2);
			int r2 = Helper.generateRandomNumber(np/2, np);
			MSA[] children = crossover(population[r1], population[r2]);
			for(int j = 0; j < 2; j++){
				children[j] = ilns(children[j], n_point, timeout_per_point, lns_type);
//				System.out.println(ri[j] + " " + children[j].getScore());
			}
//			System.out.println();
			population = updatePopulation(population, children);
			
//			for(int j = 0; j < np; j++){
//				System.out.println(population[j].getScore());
//			}
//			System.out.println();
			
			if(population[0].getScore() > last_best_score){
				last_best_score = population[0].getScore();
				no_change = 0;
			} else no_change++;
			if(no_change > max_no_change)
				break;
		}
		
		return population[0];
	}
	
	public static MSA ilns(MSA msa, int n_point, long timeout_per_point, int lns_type){
		float last_score = Float.MIN_VALUE;
		
		while(n_point > 0){
			int m = msa.getNumber();
			int n = msa.getLength();
			String[] seqs = msa.getString();
			
			ArrayList<int[]> relax_cands = new ArrayList<int[]>();
			if(lns_type == MSA.LNS_SINGLE){
				for(int i=0; i<m; i++)
					for(int j=0; j<n; j++){
						boolean in = false;
						if(seqs[i].charAt(j) != '-'){
							if(j == 0){
								if(seqs[i].charAt(j+1) == '-')
									in = true;
							}else if(j == n-1){
								if(seqs[i].charAt(j-1) == '-')
									in = true;
							}else if((seqs[i].charAt(j+1) == '-') || (seqs[i].charAt(j-1) == '-'))
								in = true;
						}
						if(in) relax_cands.add(new int[]{i, j});
					}
			}else{
				for(int i=0; i<m; i++){
					boolean hold = false;
					int t = 0;
					for(int j=0; j<n; j++){
						if(seqs[i].charAt(j) != '-'){
			       			if(!hold){
			       				hold = true;
			       				t = j;
			       			}
			       			if(hold && (j==n-1))
			       				relax_cands.add(new int[]{i, t, j});
			       		}else{
			       			if(hold) {
			       				hold = false;
			       				relax_cands.add(new int[]{i, t, j-1});
			       			}
			       		}
					}
				}
//				for(int i=0; i<relax_cands.size(); i++)
//					System.out.println(relax_cands.get(i)[0] + " "
//										+ relax_cands.get(i)[1] + " "
//										+ relax_cands.get(i)[2]);
			}
			
			int n_cands = relax_cands.size();
			if(n_cands == 0) break;
			else{
				int[] r;
				if(n_point >= n_cands) 
					r = Helper.generateNRandomNumberAndShuffle(0, n_cands, n_cands);
				else r = Helper.generateNRandomNumberAndShuffle(0, n_cands, n_point);
				n_point -= n_cands;
				
				for(int i=0; i<r.length; i++){
					ArrayList<int[]> relax = new ArrayList<int[]>();
					relax.add(relax_cands.get(r[i]));
//					System.out.println(relax.get(0)[0] + " " + relax.get(0)[1]+ " " + relax.get(0)[2]);
					msa.lns(relax, timeout_per_point, lns_type);
				}
			}
			if(msa.getScore() <= last_score)
				break;
			last_score = msa.getScore();
		}
		return msa;
	}
	
	private static MSA ilns0(MSA msa, int max_sub_length, long timeout_per_part, int grouping){
		int len = msa.getLength();
		
		String[] s = msa.getString();
		int m = s.length;
		int part = len / max_sub_length;
		if(len % max_sub_length != 0)
			part++;
		int offset = 0;
		
		for(int k=0; k < part; k++){
			String[] sub_s = new String[m];
			String[] pre_sub_s = new String[m];
			String[] post_sub_s = new String[m];
			
			for(int i=0; i<m; i++){
				pre_sub_s[i] = s[i].substring(0, offset + k * max_sub_length);
				sub_s[i] = s[i].substring(offset + k * max_sub_length, Math.min((offset + k * max_sub_length) + max_sub_length, offset + len));
				if(((offset + k * max_sub_length) + max_sub_length) <= (offset + len-1))
					post_sub_s[i] = s[i].substring(offset + (k * max_sub_length) + max_sub_length);
				else post_sub_s[i] = "";
//				System.out.println(pre_sub_s[i] + " " + sub_s[i] + " " + post_sub_s[i]);
			}
//			System.out.println();
			
			MSA msa1 = new MSA(sub_s, pre_sub_s, post_sub_s, msa.getScoring());
//			msa1.print();
//			System.out.println(msa1.getScore());
//			System.out.println();
			int ori_n = msa1.getLength();
			msa1.group(grouping);
			msa1.realign(timeout_per_part);
//			msa1.print();
//			System.out.println(msa1.getScore());
//			System.out.println();
			
			offset += (msa1.getLength() - ori_n);
			
			s = msa1.getString();
			for(int i=0; i<m; i++){
				s[i] = pre_sub_s[i] + s[i] + post_sub_s[i];
			}
		}
		
		return new MSA(s, msa.getScoring());
	}
	
	private static MSA[] initPopulation(int np, MSA msa){
		MSA[] population = new MSA[np];
		for(int i = 0; i < np; i++){
			population[i] = msa;
		}
		return population;
	}
	
	private static MSA[] initPopulation0(int np, MSA msa){
		MSA[] population = new MSA[np];
		population[0] = msa;
		
		String[] seqs = msa.getString();
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
        for(int i = 0; i < m; i++)
        	l[i] = unseqs[i].length();
        int sum_l = l[0], max_l = l[0];
        for(int i = 1; i < m; i++){
        	sum_l += l[i];
        	if(l[i] > max_l) max_l = l[i];
        }
        
//        for(int i = 0; i < m; i++){
//        	System.out.println(seqs[i]);
//        }
//        System.out.println();
        
        // generate np-1 random MSAs
        for(int o = 1; o < np; o++){
        	// shuffle one sequence
        	int len = n;
        	String[] r_msa = new String[m];
        	for(int i = 0; i < m; i++){
        		r_msa[i] = seqs[i];
        	}
        	
        	int ir = Helper.generateRandomNumber(0, m);
//        	System.out.println(ir);
        	r_msa[ir] = "";
        	int[] r = Helper.generateNRandomNumber(0, len, l[ir]);
        	
        	int last_i = 0;
        	for(int j=0; j<l[ir]; j++){
        		for(int k=last_i; k<r[j]; k++)
        			r_msa[ir] += '-';
        		r_msa[ir] += unseqs[ir].charAt(j);
        		last_i = r[j] + 1;
        	}
        	for(int j=last_i; j<len; j++)
        		r_msa[ir] += '-';
	        
        	// generate a random MSA
//        	int len = Helper.generateRandomNumber(max_l, sum_l+1);
//	        String[] r_msa = new String[m];
//	        
//	        for(int i = 0; i < m; i++){
//	        	r_msa[i] = "";
//	        	int[] r = Helper.generateNRandomNumber(0, len, l[i]);
//	        	
//	        	int last_i = 0;
//	        	for(int j=0; j<l[i]; j++){
//	        		for(int k=last_i; k<r[j]; k++)
//	        			r_msa[i] += '-';
//	        		r_msa[i] += unseqs[i].charAt(j);
//	        		last_i = r[j] + 1;
//	        	}
//	        	for(int j=last_i; j<len; j++)
//	        		r_msa[i] += '-';
//	        }
	        
	        // remove columns only containing gaps
	        String[] rr_msa = new String[m];
	        for(int i = 0; i < m; i++)
	        	rr_msa[i] = "";
	        for(int i = 0; i < len; i++){
	        	boolean all_gap = true;
	        	for(int j = 0; j < m; j++)
	        		if(r_msa[j].charAt(i) != '-')
	        			all_gap = false;
	        	if(!all_gap)
	        		for(int j = 0; j < m; j++)
	        			rr_msa[j] += r_msa[j].charAt(i);
	        }
	        
	        population[o] = new MSA(rr_msa, msa.getScoring());
	        
//	        System.out.println("Init " + o);
//	        for(int i = 0; i < m; i++){
//	        	System.out.println(rr_msa[i]);
//	        }
//	        System.out.println();
        }
        return population;
	}
	
	private static MSA[] crossover(MSA msa1, MSA msa2){
		String[] s1 = msa1.getString();
		String[] s2 = msa2.getString();
		
		int m = s1.length;
		int n = s1[0].length();
		
		String[] s3_1 = new String[m];
		String[] s3_2 = new String[m];
		String[] s11 = new String[m];
		String[] s12 = new String[m];
		String[] s21 = new String[m];
		String[] s22 = new String[m];
		for(int i = 0; i < m; i++){
			s3_1[i] = s3_2[i] = s11[i] = s12[i] = s21[i] = s22[i] = "";
		}
		
		int cut = Helper.generateRandomNumber(0, n);
		
		// s11 s12
		for(int i = 0; i < cut; i++)
			for(int j=0; j<m; j++)
				s11[j] += s1[j].charAt(i);
		for(int i = cut; i < n; i++)
			for(int j=0; j<m; j++)
				s12[j] += s1[j].charAt(i);
		
		// number of chars in s11
		int[] nc = new int[m];
		for(int i=0; i<m; i++){
			nc[i] = 0;
			for(int j=0; j<cut; j++)
				if(s11[i].charAt(j)!='-')
					nc[i]++;
		}
		
		// build s21
		int max_l1 = 0;
		for(int i=0; i<m; i++){
			int k = 0;
			int j = 0;
			int len = s2[i].length();
			while((k < nc[i]) && (j < len)){
				if(s2[i].charAt(j)!='-')
					k++;
				j++;
			}
			s21[i] = s2[i].substring(0, j);
			if(j > max_l1)
				max_l1 = j;
		}
		
		// build s22
		int max_l2 = 0;
		for(int i=0; i<m; i++){
			s22[i] = s2[i].substring(s21[i].length());
			int len = s22[i].length();
			if(len > max_l2)
				max_l2 = len;
		}
				
		// adjust by adding some gaps
		for(int i=0; i<m; i++){
			int d1 = max_l1 - s21[i].length(); 
			for(int j=0; j < d1; j++){
				s21[i] += '-';
			}
			int d2 = max_l2 - s22[i].length(); 
			for(int j=0; j < d2; j++){
				s22[i] = ('-' + s22[i]);
			}
		}
		
		// remove columns only containing gaps
		String[] t_s21 = new String[s21.length];
		String[] t_s22 = new String[s22.length];
		int n1 = s21[0].length();
		int n2 = s22[0].length();
		for(int i = 0; i < m; i++){
			t_s21[i] = s21[i];
			t_s22[i] = s22[i];
			s21[i] = "";
			s22[i] = "";
		}
		
		for(int j=0; j<n1; j++){
			boolean all_gap = true;
			for(int i=0; i<m; i++){
				if(t_s21[i].charAt(j) != '-'){
					all_gap = false;
					break;
				}
			}
			if(all_gap)
				continue;
			for(int i=0; i<m; i++)
				s21[i] += t_s21[i].charAt(j);
		}
		for(int j=0; j<n2; j++){
			boolean all_gap = true;
			for(int i=0; i<m; i++){
				if(t_s22[i].charAt(j) != '-'){
					all_gap = false;
					break;
				}
			}
			if(all_gap)
				continue;
			for(int i=0; i<m; i++)
				s22[i] += t_s22[i].charAt(j);
		}
		
//		for(int j = 0; j < m; j++)
//			System.out.println(s11[j]);
//		System.out.println();
//		
//		for(int j = 0; j < m; j++)
//			System.out.println(s12[j]);
//		System.out.println();
//		
//		for(int j = 0; j < m; j++)
//			System.out.println(s21[j]);
//		System.out.println();
//		
//		for(int j = 0; j < m; j++)
//			System.out.println(s22[j]);
//		System.out.println();
		
		String[] child1 = new String[m];
		String[] child2 = new String[m];
		for(int i = 0; i < m; i++){
			child1[i] = s11[i] + s22[i];
			child2[i] = s21[i] + s12[i];
		}
		
//		System.out.println("Parent 1");
//		msa1.print();
//		System.out.println("Parent 2");
//		msa2.print();
//		System.out.println("Child 1");
//		for(int j = 0; j < m; j++)
//			System.out.println(child1[j]);
//		System.out.println();
//		System.out.println("Child 2");
//		for(int j = 0; j < m; j++)
//			System.out.println(child2[j]);
//		System.out.println();
		
		return new MSA[] {new MSA(child1, msa1.getScoring()), new MSA(child2, msa1.getScoring())};
	}
	
	private static MSA[] orderPopulation(MSA[] population){
		for(int i = 0; i < population.length - 1; i++){
			for(int j = i + 1; j < population.length; j++){
				if(population[j].getScore() > population[i].getScore()){
					MSA temp = population[i];
					population[i] = population[j];
					population[j] = temp;
				}
			}
		}
		return population;
	}
	
	private static MSA[] updatePopulation(MSA[] population, MSA[] new_organisms){
		for(int i = 0; i < new_organisms.length; i++){
			MSA new_organism = new_organisms[i];
			if(population[population.length-1].getScore() >= new_organism.getScore())
				continue;
			for(int j = population.length-2; j >= 0; j--){
				if(population[j].getScore() >= new_organism.getScore()){
					population[j+1] = new_organism;
				}
			}
		}
		return population;
	}

	
}



