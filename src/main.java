//import java.awt.List;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.text.Document;
import static java.nio.charset.StandardCharsets.*;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class demo {
	
//	public static Map<String, List<String>> map = new HashMap<String, List<String>>();
	public static Map<String, LinkedList> POST_LIST = new HashMap<String, LinkedList>();
	public static ArrayList OR = new ArrayList();
	public static ArrayList AND = new ArrayList();
	//public static ArrayList merge_list=new ArrayList();
	public static int min;
	public static void getPostings(String[] terms,FileOutputStream out) throws IOException
	{
		for(int i=0;i<terms.length;i++)
		{	//System.out.println("GetPostings");
			out.write("GetPostings\n".getBytes());
			//System.out.println(terms[i]);
			out.write(terms[i].getBytes("UTF-8"));
			out.write("\n".getBytes());
			//System.out.print("Postings list:");
			out.write("Postings list:".getBytes());
			for(int j=0;j<POST_LIST.get(terms[i]).size();j++)
			{
				//System.out.print(" "+POST_LIST.get(terms[i]).get(j));
				out.write((" "+POST_LIST.get(terms[i]).get(j)).getBytes());
			}
			//System.out.println("");
			out.write("\n".getBytes());
		}
		
	}
	
	//Term at a Time
	public static void TAAT(Map<String, LinkedList> INV_LIST, String[] terms,FileOutputStream out) throws IOException
	{
		//Store terms in TERMS for printing purposes
		String TERMS="";
		for(Object e:terms)
		{
			TERMS+=e.toString();
			TERMS+=" ";
		}
		OR.clear();
		AND.clear();
		int comparisons_OR=0,comparisons_AND=0;
		
		//If input is single term print as it is
		if(terms.length==1)
		{
			comparisons_OR=0;
			comparisons_AND=0;
			for(int i=0;i<INV_LIST.get(terms[0]).size();i++)
			{
				OR.add(INV_LIST.get(terms[0]).get(i));
				AND.add(INV_LIST.get(terms[0]).get(i));
			}
		}

		
		
		else
		{
			//Store first term's inverted list in both OR and AND list
			for(int i=0;i<INV_LIST.get(terms[0]).size();i++)
				{OR.add((Integer)INV_LIST.get(terms[0]).get(i));
				 AND.add((Integer)INV_LIST.get(terms[0]).get(i));
				}			
			int i=1,j=0,k=0;
			//Travere all the terms from second
			while(i<terms.length)
			{
				int count=OR.size();
				ArrayList list1=new ArrayList();
				k=0;j=0;
				
				//Traverse OR list and Inverted List of the term(i) parallely 
				while(j<OR.size() && k<INV_LIST.get(terms[i]).size())
				{
					Collections.sort(OR);
					Collections.sort(AND);
						//if term(i).docID(k) < OR(j) then add to OR list and increment k
						if((Integer)INV_LIST.get(terms[i]).get(k)<(Integer)OR.get(j))
							{OR.add((Integer)INV_LIST.get(terms[i]).get(k));k++;comparisons_OR++;comparisons_AND++;}
						//if term(i).docID(k) > OR(j) then just increment OR pointer j
						else if((Integer)INV_LIST.get(terms[i]).get(k)>(Integer)OR.get(j))
							{j++;comparisons_OR++;comparisons_AND++;}//here
						//if term(i).docID(k) = OR(j) then increment both pointers k and j and add the docID to a list to compare with AND
						else
							{list1.add(OR.get(j));j++;k++;comparisons_OR++;comparisons_AND++;}//here
				}
				//Add remaining docIDs from Inverted List of the term to the OR list
				while(k<INV_LIST.get(terms[i]).size())
					{OR.add((Integer)INV_LIST.get(terms[i]).get(k));k++;}
				int m=0,n=0;
				int cnt=AND.size();
				
				//Check the list list1 with AND list and remove the items from AND if not present
				while(m<AND.size() && n<list1.size())
					{
					Collections.sort(OR);
					Collections.sort(AND);
					//remove AND(m) if AND(m)<list1(n). This means it has skipped the docID
						if((Integer)AND.get(m)<(Integer)list1.get(n))
							{AND.remove(m);comparisons_AND++;}
						//if AND(m)>list1(n) increment n
						else if((Integer)AND.get(m)>(Integer)list1.get(n))
							{n++;comparisons_AND++;}
						//This means that both the pointers point to the same docID so increment both pointers.
						else
						{m++;n++;comparisons_AND++;}	//here
					}
				//Remove extra AND docIDs
				while(m<AND.size())
					AND.remove(m);
				list1.clear();
				i++;
			}
			
		}
			//System.out.println(comparisons_OR+"   "+comparisons_AND);
			
		
		
			Collections.sort(OR);
			Collections.sort(AND);
			
			
			//System.out.println("TaatAnd");
			out.write("TaatAnd\n".getBytes());
			//System.out.println(TERMS);
			out.write(TERMS.getBytes("UTF-8"));
			//System.out.print("Results:");
			out.write("\nResults:".getBytes());
			for (Object o:AND)
			{
				//System.out.print(" "+o);
				out.write((" "+o).getBytes());
			}
			if(AND.size()==0){out.write(" empty".getBytes());}//System.out.print(" empty");out.write(" empty".getBytes());}
			//System.out.println("\nNumber of documents in results: "+AND.size());
			out.write(("\nNumber of documents in results: "+AND.size()+"\n").getBytes());
			//System.out.println("Number of comparisons: "+comparisons_AND);
			out.write(("Number of comparisons: "+comparisons_AND+"\n").getBytes());
			
			
			
			//System.out.println("TaatOr");
			out.write("TaatOr\n".getBytes());
			//System.out.println(TERMS);
			out.write(TERMS.getBytes("UTF-8"));
			//System.out.print("Results:");
			out.write("\nResults:".getBytes());
			for (Object o:OR)
			{
				//System.out.print(" "+o);
				out.write((" "+o).getBytes());
			}
			if(OR.size()==0){//System.out.print(" empty");
			out.write(" empty".getBytes());}
			//System.out.println("\nNumber of documents in results: "+OR.size());
			out.write(("\nNumber of documents in results: "+OR.size()+"\n").getBytes());
			//System.out.println("Number of comparisons: "+comparisons_OR);
			out.write(("Number of comparisons: "+comparisons_OR+"\n").getBytes());
			
			
		
		
		
	}
	
	
	//Document at a Time
	public static void DAAT(Map<String, LinkedList> INV_LIST, String[] terms,FileOutputStream out) throws IOException
	{
		//Store terms in TERMS for printing purposes
		String TERMS="";
		for(Object e:terms)
		{
			TERMS+=e.toString();
			TERMS+=" ";
		}
		OR.clear();
		AND.clear();
		//index holds pointers for 'n' different terms
		int[] index=new int[20];
		int count=0,ind=0,comparisons_OR=0,comparisons_AND=0,doc=0,ind1=0;
		
		//If input is single term print as it is
		if(terms.length==1)
		{
			comparisons_OR=0;
			comparisons_AND=0;
			for(int i=0;i<INV_LIST.get(terms[0]).size();i++)
			{
				OR.add(INV_LIST.get(terms[0]).get(i));
				AND.add(INV_LIST.get(terms[0]).get(i));
			}
		}
		
		
		
		else
		{
			//DAAT OR
			//Get the maximum docID and the term index
			for(int i=0;i<terms.length;i++)
			{
					comparisons_OR++;
					if((Integer)INV_LIST.get(terms[i]).get(INV_LIST.get(terms[i]).size()-1)>(Integer)count)
					{
						count=(Integer)INV_LIST.get(terms[i]).get(INV_LIST.get(terms[i]).size()-1);
						ind1=i;
						}
			}
			
			
			Boolean flag2=true,inside=true;
			//Loop till inside is set to true
			while(inside)
			{
				//if(flag2)
				{
					Boolean flag=true;
					//Set min to the docID being pointed by ind1
					min=(Integer)INV_LIST.get(terms[ind1]).get(index[ind1]);
					//All terms are traversed to check one docID
					for(int j=0;j<terms.length;j++)
					{
						//If end of any term has occured set flag2 to false
						if(index[j]==(Integer)INV_LIST.get(terms[j]).size()-1)
							{	
								flag2=false;
							}
						//If pointer is less than term.size and term is not ind1, because it is stored in min
						if(index[j]<(Integer)INV_LIST.get(terms[j]).size() && j!=ind1)
						{
							//Increase OR comparison count
							comparisons_OR++;
							if((Integer)INV_LIST.get(terms[j]).get(index[j])<min)
							{
								//If term.DocID < min assign min to DocId and make necessary changes. flag is used to denote only OR
 								min=(Integer)INV_LIST.get(terms[j]).get(index[j]);
								ind=j;
								doc=min;
								flag=false;
								
							}
							else if((Integer)INV_LIST.get(terms[j]).get(index[j])>min)
							{	
								//If term.DocId > min just ignore and move setting flag to false
								flag=false;
								
							}
						}
					}
					
					
					if(flag)
					{
						
						//If flag is true then all pointers point to same DocID so increment all pointers
							OR.add(min);
							//if(flag2)
								//AND.add(min);
							for(int k=0;k<terms.length;k++)
								index[k]++;	
					}
					else
					{
						
						//Add the minimum to OR and increment the pointers of all terms whose value is min
						OR.add(min);
						for(int z=0;z<terms.length;z++)
						{
							//System.out.println((Integer)INV_LIST.get(terms[z]).get(index[z]));
							if(index[z]<(Integer)INV_LIST.get(terms[z]).size()){
							if((boolean)INV_LIST.get(terms[z]).get(index[z]).equals(min))
								{
								//Increment Pointer
								index[z]++;
								comparisons_OR++;
								}
							}
						}
					}
				}
				
				//When the term with max doc has reached its end that means there is no more terms remaining, So set the inside flag to false
				if(index[ind1]==(Integer)INV_LIST.get(terms[ind1]).size())
					inside=false;
			}
			
			
			//DAAT AND
				index=new int[20];//Term Pointers
				Boolean in=true;
				count=999;
				int indi=-1;
				
				//Get the term with the least number of documents, index=indi
				for(int i=0;i<terms.length;i++)
				{
					if(INV_LIST.get(terms[i]).size()<count)
						{count=INV_LIST.get(terms[i]).size();indi=i;}
				}
				int list_cnt=0;
				AND.clear();
				//Add all items from indi to AND
				while(list_cnt<INV_LIST.get(terms[indi]).size())
				{
					AND.add(INV_LIST.get(terms[indi]).get(list_cnt));
					list_cnt++;
				}
				int[] AND_IND=new int[1];//AND list pointer
				
				//Loop till in is true
				while(in)
				{
					int i=0;
					Boolean flag_and=true;
					//Loop all terms since daat
					while(i<terms.length )
					{
						//Skip indi term since its already in AND
						if(i!=indi)
						{	
								//If one of the terms reached end then stop
								if(index[i]==INV_LIST.get(terms[i]).size()-1)in=false;
								//If AND size is not null and AND pointer hasn't reached its end
								if(AND.size()!=0 && AND_IND[0]<AND.size())
								{
									comparisons_AND++;
									//If the term(i).doc(p) > AND(p_and) then remove the term from AND as it seems to not contain the docID
									if((Integer)INV_LIST.get(terms[i]).get(index[i])>(Integer)AND.get(AND_IND[0]))
										{
											AND.remove(AND_IND[0]);
											flag_and=false;
										}
									//If the term(i).doc(p) < AND(p_and) then increment p and set flag_and to false. flag_and is true when all the pointers point to the same docID.
									else if((Integer)INV_LIST.get(terms[i]).get(index[i])<(Integer)AND.get(AND_IND[0]))
									{
										flag_and=false;
										index[i]++;
									}
								}
								//Else break loop indicating end of AND Pointer.
								else
									in=false;
						}
						i++;
					
					}
					
					//If all the pointers point to the same docID then increment all term pointers including AND Pointer
					if(flag_and)
					{
						for(int x=0;x<terms.length;x++)
							index[x]++;
						AND_IND[0]++;
					}
						//in=false;
				}
				//If there is any remaining docs in AND then remove it
				while(AND_IND[0]<AND.size())AND.remove(AND_IND[0]);
		}


		Collections.sort(OR);
		Collections.sort(AND);
		
		
		//System.out.println("DaatAnd");
		out.write("DaatAnd\n".getBytes());
		//System.out.println(TERMS);
		out.write(TERMS.getBytes("UTF-8"));
		//System.out.print("Results:");
		out.write("\nResults:".getBytes());
		for (Object o:AND)
		{
			//System.out.print(" "+o);
			out.write((" "+o).getBytes());
		}
		if(AND.size()==0){//System.out.print(" empty");
		out.write(" empty".getBytes());}
		//System.out.println("\nNumber of documents in results: "+AND.size());
		out.write(("\nNumber of documents in results: "+AND.size()+"\n").getBytes());
		//System.out.println("Number of comparisons: "+comparisons_AND);
		out.write(("Number of comparisons: "+comparisons_AND+"\n").getBytes());
		
		
		
		//System.out.println("DaatOr");
		out.write("DaatOr\n".getBytes());
		//System.out.println(TERMS);
		out.write(TERMS.getBytes("UTF-8"));
		//System.out.print("Results:");
		out.write("\nResults:".getBytes());
		for (Object o:OR)
		{
			//System.out.print(" "+o);
			out.write((" "+o).getBytes());
		}
		if(OR.size()==0){//System.out.print(" empty");
		out.write(" empty".getBytes());}
		//System.out.println("\nNumber of documents in results: "+OR.size());
		out.write(("\nNumber of documents in results: "+OR.size()+"\n").getBytes());
		//System.out.println("Number of comparisons: "+comparisons_OR);
		out.write(("Number of comparisons: "+comparisons_OR+"\n").getBytes());
			

///////EXTRA APPROACH///////////
///// 0 Comparisons ////////////
/*  Maintain a hash map of <key,Value>=<term,docID> (Visited Matrix).. Increment count every time we encounter docID
 * The way we traverse the DocIDs differ for TaaT and Daat The following approach is for Daat*/

		
//		int[][] term_vect=new int[terms.length][50000];
//		int[] sum=new int[50000];
//		ArrayList OR = new ArrayList();
//		ArrayList AND = new ArrayList();
//		System.out.println("");
//		for(int i=0;i<45314;i++)
//		{
//			for(int j=0;j<terms.length;j++)
//			{
//				for(int k=0;k<INV_LIST.get(terms[j]).size();k++)
//				{
////						int n=(int)INV_LIST.get(terms[j]).get(k);
////						term_vect[j][n]=1; //If new occurrence of term assign count to 1
//					if((int)INV_LIST.get(terms[j]).get(k)==i)
//						term_vect[j][i]=1; //Assign one if the term is present in the Term.. Basically a Visited Matrix. 
//				}
//				sum[i]+=term_vect[j][i]; //Sum up the number of times the term has encountered
//			}
//			if(sum[i]>0)
//				OR.add(i);//If sum > 0 term is present in any one of the Docs so add the docID to or
//			if(sum[i]==terms.length)
//				AND.add(i); //If sum is same as term length then the term has occurred in all the docs
//		}
		
// The approach can be used for Taat as well to obtain 0 Comparison		
		
	
	}
	

	public static Map<String, LinkedList> index_field(IndexReader reader, String lang,Map<String, LinkedList> map) throws IOException
	{
		//Get indexed terms from reader<IndexReader> under the field lang
		Terms term=MultiFields.getTerms(reader, lang);
		TermsEnum termenum=term.iterator();
		BytesRef termx;
		LinkedList l1=new LinkedList();
		    while ( (termx = termenum.next()) != null ) {
		    	PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader,lang, termx);//, PostingsEnum.NONE);
		    	int i;
		        while ((i = postingsEnum.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
		        	l1.add(i);
		        }
		        map.put(termx.utf8ToString(),(LinkedList) l1.clone());
		        l1.clear();       
		    }
		    	
		return map;		
	}
	public static void main(String[] args) throws IOException
	{

		//Reading Index
		Path path1=Paths.get(args[0]);
		Directory index = FSDirectory.open(path1);
		IndexReader reader = DirectoryReader.open(index);
		
		//Obtaining Inverted List by calling index_field method
					POST_LIST=index_field(reader,"text_nl",POST_LIST);
					POST_LIST=index_field(reader,"text_fr",POST_LIST);
					POST_LIST=index_field(reader,"text_de",POST_LIST);
					POST_LIST=index_field(reader,"text_ja",POST_LIST);
					POST_LIST=index_field(reader,"text_ru",POST_LIST);
					POST_LIST=index_field(reader,"text_pt",POST_LIST);
					POST_LIST=index_field(reader,"text_es",POST_LIST);
					POST_LIST=index_field(reader,"text_it",POST_LIST);
					POST_LIST=index_field(reader,"text_da",POST_LIST);
					POST_LIST=index_field(reader,"text_no",POST_LIST);
					POST_LIST=index_field(reader,"text_sv",POST_LIST);
					
					
					FileOutputStream out = new FileOutputStream(args[1]);
					BufferedReader in = new BufferedReader ( new InputStreamReader(new FileInputStream(args[2]),"UTF8"));
					
					//To handle BOM character
					in.mark(4);
					if('\ufeff'==in.read())
					{
						//System.out.println("BOM");
					}
					else
					{
						in.reset();
					}
					String line=in.readLine();
					while(line!=null)
					{
							ArrayList terms_in=new ArrayList();
							String[] ind=line.split(" ");
							for(int i=0;i<ind.length;i++)
							{
								terms_in.add(ind[i]);
							}
							String terms_string[] = new String[terms_in.size()];              
							for(int j =0;j<terms_in.size();j++){
								terms_string[j] = (String) terms_in.get(j);	
							}
							
							//Print Postings List
							getPostings(terms_string,out);
						
							TAAT(POST_LIST,terms_string,out);
							DAAT(POST_LIST,terms_string,out);
						
							line=in.readLine();
				
					}
		
	}
}


