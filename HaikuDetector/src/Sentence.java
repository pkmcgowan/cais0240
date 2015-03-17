import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class Sentence
{
	private String text;	//last character is punctuation (!, .(period) ,?)
	private int syllableCount;
	
	
	public Sentence(String text)
	{
		this.text = text.substring(0, text.length() - 1);
		this.setSyllableCount();	
	}

	private void setSyllableCount()
	{
		this.syllableCount = 0;
		String sentence = this.text;
		String[] words = sentence.split(" ");	//split the sentence into words
		
		for(int i=0; i < words.length; i++)
		{
			this.syllableCount += HaikuDetector.countSyllables(words[i]);
		}
	}
	
	public String getText()
	{
		return text;
	}

	public int getSyllableCount()
	{
		return syllableCount;
	}
	
}
