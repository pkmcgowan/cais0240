import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HaikuDetector
{
	private ArrayList<Sentence> sentences; 
	private static TreeMap<String, Integer> compoundWordSyllables;
	
	public HaikuDetector()
	{
		
		Scanner keyboard = new Scanner(System.in);
		String filename = "";
		this.sentences = new ArrayList<Sentence>();
		this.sentences.clear(); 	//clear sentence list as a precaution
		System.out.print("Enter file name to process (include extension):  ");
		filename = keyboard.nextLine();
		
		this.readFile(filename);
		this.lookForHaikus();
		keyboard.close();
	}

	public static void main(String[] args)	{
		
		//Load dictionary of compound words
		String line = "";
		try
		{
			compoundWordSyllables = new TreeMap<String, Integer>();
			BufferedReader inputStream = new BufferedReader(new FileReader("compoundwords.csv"));
			while((line = inputStream.readLine()) != null)
			{
				String[] words = line.split(",");
				String key = words[0];
				int syllables = 0;
				
				for(int i=1; i<words.length; i++)
				{
					
					if(words[i].length() > 0)
					{
						syllables += countSyllables(words[i]);
					}
				}
				
				compoundWordSyllables.put(key, syllables);
			}
			inputStream.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new HaikuDetector();
	}

	/*
	 * method read a text file and converts it into a collection of sentences
	 * source: http://docs.oracle.com/javase/tutorial/essential/io/charstreams.html
	 */
	private void readFile(String filename)
	{
		FileReader inputStream = null;
		String sentence = "";
		char character = 0;
		int input = 0;
		int count = 0;
		
		
		try
		{
			inputStream = new FileReader(filename);
//			System.out.print("Reading");
			while((input = inputStream.read()) != -1)
			{
				character = (char) input;
//				System.out.print(character);
//				System.out.print(".");
				
				if(count == 0 && (Character.isWhitespace(character) ||
						character == '.' || character == '?' || character == '\t' ||
						character == '!' || character == '\n'))
				{
					continue;	//skip leading whitespace and terminating punctuation
				}
				else
				{
					sentence += character;	//append character to sentence
					if(character == '.' || character == '?' || character == '!' || character == '\n')
					{
						count = 0;
						Sentence obj = new Sentence(sentence);
						
						this.sentences.add(obj);
//						System.out.println(obj.getText()+ " " + obj.getSyllableCount());
						sentence = "";
					}
					else
					{
						count++;	//increment counter
					}
				}
			}//end while
			
			System.out.println();
			inputStream.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println(e);
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}
	
	private void lookForHaikus()
	{
		final int NO_HAIKU = 0;
		final int FIRST_LINE_OK = 1;
		final int SECOND_LINE_OK = 2;
		
		String firstLine = "";
		String secondLine = "";
		String haiku = "";
		
		int status = NO_HAIKU;
		int syllables = 0;
		
		Iterator<Sentence> iterator = this.sentences.listIterator();
		while(iterator.hasNext())
		{
			Sentence sentence = iterator.next();
			syllables = sentence.getSyllableCount();
			switch(status)
			{
				case NO_HAIKU:
					if(syllables == 5)
					{
						firstLine = sentence.getText();
						status = FIRST_LINE_OK;
					}
					break;
				case FIRST_LINE_OK:
					if(syllables == 7)
					{
						secondLine = sentence.getText();
						status = SECOND_LINE_OK;
					}
					else
					{
						status = NO_HAIKU;
						firstLine = "";
						secondLine = "";
					}
					break;
				case SECOND_LINE_OK:
					if(syllables == 5)
					{
						haiku = firstLine + "\n" + secondLine + "\n" + sentence.getText();
						System.out.println("Haiku verse detected:\n\n"+haiku+"\n");
						status = NO_HAIKU;
						firstLine = "";
						secondLine = "";
						haiku = "";
					}
					else
					{
						status = NO_HAIKU;
						firstLine = "";
						secondLine = "";
					}
					break;
			}//end switch
		}//end while
	}//end lookForHaikus()
	
	/*
	 * method counts the number of syllables in a word
	 * (translated from python)
	 * source: http://eayd.in/?p=232
	 */
	public static int countSyllables(String word)
	{
		String lcWord = word.toLowerCase();	//convert parameter to lower case
		int sylCount = 0;
		int sylAdd = 0;	//additional syllables to add
		int sylDel = 0; //syllables to delete
		int dtCount = 0;	//dipthong/triple count
		int tCount = 0; //vowel triplet count
		int vcCount = 0; //vowel followed by letter count		
		
		int wLength = lcWord.length();
		
		//word exceptions 
		String[] exceptionAdd = {"serious", "crucial"};
		String[] exceptionDel = {"fortunately", "unfortunately"};
	/*
 	python source code:
 	co_one = ['cool','coach','coat','coal','count','coin','coarse','coup','coif','cook','coign','coiffe','coof','court']
	co_two = ['coapt','coed','coinci']

	pre_one = ['preach']
	
	negative = ["doesn't", "isn't", "shouldn't", "couldn't","wouldn't"]
	le_except = ['whole','mobile','pole','male','female','hale','pale','tale','sale','aisle','whale','while']
	 */
		String[] pre_one= {"preach"};
		String[] co_one = {"cool","coach","coat","coal","count","coin","coarse",
				"coup","coif","cook","coign","coiffe","coof","court"};
		String[] co_two = {"coapt","coed","coinci"};
		String[] negatives = {"doesn't", "isn't", "shouldn't", "couldn't","wouldn't"};
		String[] le_except = {"whole","mobile","pole","male","female","hale","pale","tale","sale","aisle","whale","while"};
		
		//step 1a: check compound word list and return syllable count
		try {
			if(compoundWordSyllables.containsKey(word))
				return compoundWordSyllables.get(word);
			
			//step 1b: return one syllable if word length <= 3
			if(wLength <= 3)
				return 1;
			
			//step 2a: count vowel triplets, dipthongs and vowel-consonant pairs
			tCount = countMatches("[aeiou][aeiou][aeiou]", lcWord);
			dtCount = countMatches("[aeiou][aeiou]", lcWord);
			vcCount = countMatches("[aeiou][^aeiou]", lcWord);
			
			//step 2b: check for silent 'es' & 'ed' and discard if one vowel double/triple or one cowel/consonant pair found
			//exceptions are 'ted', 'tes', 'ses', 'ied', 'ies'
			if(lcWord.endsWith("es") || lcWord.endsWith("ed"))
			{
				if((dtCount == 1) || (vcCount == 1))
				{
					if(!(lcWord.endsWith("ted") || lcWord.endsWith("tes") ||
							lcWord.endsWith("ses") || lcWord.endsWith("ied") || lcWord.endsWith("ies")))
						sylDel++;	//discard if found
				}
			}//end if(lcWord.endsWith("es") || lcWord.endsWith("ed"))
			
			//step 3: discard trailing e unless word is element of le_except array
			if(lcWord.endsWith("e"))
			{
				boolean exception = false;
				for(int i=0; i<le_except.length; i++)
				{
					if(lcWord.equals(le_except[i]))
					{
						exception = true;
						break;
					}//end if
				}// end for
				if(!exception)
					sylDel++;
				
			}//end if(lcWord.endsWith("e"))
			
			//Step 4:Discard vowel doubles and tripples
			sylDel += (dtCount + tCount);
			
			//Step 5: count remaining vowels
			sylCount = countMatches("[aeiou]", lcWord);
			
			//Step 6: add syllable if word starts with 'mc'
			if(lcWord.startsWith("mc"))
				sylAdd++;
			
			//Step 7: add syllable if word ends with 'y' but is not preceded by vowel
			if(lcWord.endsWith("y"))
			{
				char c = lcWord.charAt(wLength - 2);
				if(!isVowel(c))
					sylAdd++;
			}//end if
			
			//Step 8: add syllable if word contains a 'y' surrounded by consonants
			//		 ('y' suffix doesn't count)
			sylAdd += countMatches("[^aeiou]y[^aeiou]", lcWord);
			
			//Step 9a: add syllable if word starts with 'tri' and is followed by a vowel
			if(lcWord.startsWith("tri") && lcWord.length() > 4)
			{
				char c = lcWord.charAt(4);
				if(isVowel(c))
					sylAdd++;
			}//end if
			
			//Step 9b: add syllable if word starts with 'bi' and is followed by a vowel
			if(lcWord.startsWith("bi")  && lcWord.length() > 3)
			{
				char c = lcWord.charAt(3);
				if(isVowel(c))
					sylAdd++;
			}//end if
			
			//Step 10: words that end in 'ian' except 'cian' and 'tian' count as two syllables
			if(!(lcWord.endsWith("cian") || lcWord.endsWith("tian")) && lcWord.endsWith("ian"))
				sylAdd++;
			
			//step 11: check for words beginning with 'co' and followed by vowel
			if(lcWord.startsWith("co"))
			{
				char c = lcWord.charAt(3);
				if(isVowel(c))
				{
					boolean inDoubleDict = false;
					for(int i=0; i<co_two.length; i++ )
					{					
						if(lcWord.startsWith(co_two[i]))
						{
							sylAdd++;	//add one syllable
							inDoubleDict = true;
							break;
						}
					}//end for
					if(!inDoubleDict)	//search single dictionary
					{
						boolean isFound = false;
						for(int i=0; i<co_one.length; i++)
						{
							if(lcWord.startsWith(co_one[i]))
							{
								isFound = true;
								break;
							}
						}//end for
						if(!isFound)
							sylAdd++;	//add one syllable if word is not in single dictionary
					}//end if(!inDoubleDict)
				}//end if(this.isVowel(c))
			}//end if(lcWord.startsWith("co"))
			
			//Step 12: check for words beginning with 'pre' and followed by vowel
			if(lcWord.startsWith("pre") && lcWord.length() > 4)
			{
				char c = lcWord.charAt(4);
				if(isVowel(c))
				{
					boolean found = false;
					//while a for loop for a single element array is trivial, 
					//one cannot dismiss the possibility of expanding the array
					for(int i=0; i<pre_one.length; i++)
					{
						if(lcWord.startsWith(pre_one[i]))
						{
							found = true;
							break;
						}
					}
					if(!found)
						sylAdd++;	//add one syllable if word is not in 'pre' dictionary
				}//end if(this.isVowel(c))
			}//end if(lcWord.startsWith("pre"))
			
			//Step 13: check if word ends with "-n't" and cross check negative dictionary to add syllable
			if(lcWord.endsWith("n't"))
			{
				for(int i=0; i<negatives.length; i++)
				{
					if(lcWord.equals(negatives[i]))
					{
						sylAdd++;	//add one syllable - word found in list
						break;
					}
				}//end for
				
			}//end if(lcWord.endsWith("n't"))
			
			//Step 14a: handle the exceptions - syllable discards
			for(int i=0; i<exceptionDel.length;i++)
			{
				if(lcWord.equals(exceptionDel[i]))
				{
					sylDel++;
					break;
				}
			}//end for
			
			//Step 14b: handle the exceptions - syllable additions
			for(int i=0; i<exceptionAdd.length;i++)
			{
				if(lcWord.equals(exceptionAdd[i]))
				{
					sylDel++;
					break;
				}
			}//end for
		}
		catch(NullPointerException e)
		{
			sylCount = 0;
			sylAdd = 0;	//additional syllables to add
			sylDel = 0; //syllables to delete
		}
		return (sylCount - sylDel + sylAdd);
	}
	
	private static boolean isVowel(char c)
	{
		boolean result = false;
		char[] vowels = {'a','e','i','o','u'};
		
		for(int i = 0; i < vowels.length; i++)
		{
			if(c == vowels[i])
			{
				result = true;
				break;
			}
		}
		return result;
	}
	
	private static int countMatches(String regex, String word)
	{
		int count = 0;
		//generate regex pattern match
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(word);
		
		while(matcher.find())
			count++;
		
		return count;
	}
}
