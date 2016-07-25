package index;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * set path to the stop word set here
 * 
 * @author Laurie
 * 
 */

public class StopLists {

	public static Set<String> getStopSet() throws FileNotFoundException {

		final Set<String> stopSet = new TreeSet<String>();

		final Scanner sc = new Scanner(
				new File(
						
					//	"src//cfg//stop_words_moderate.txt"));	
					//	C:\Users\laurie\Java\classifyGA\classifyGA\classify\cfg
					//	"C:\\Users\\laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\stop_words_moderate.txt"));
						//"C:\\Users\\laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\stop_words_most.txt"));
	"src/cfg/stop_words_most.txt"));
		//"C:\\Users\\Laurie\\Java\\classifyGAN\\classify\\cfg\\stop_words_moderate.txt"));

		while (sc.hasNext()) {
			stopSet.add(sc.next());
		}
		return stopSet;
	}
}
