import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class EmojiHelper {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static File dictionaryFile;
	private static List<String> dicList = new ArrayList<String>();
	private static List<List<String>> dicListO = new ArrayList<List<String>>();
	private static FileWriter fileWriter = null;
	private static BufferedWriter writer = null;

	private static boolean saveToFile = false;
	private static String saveFile = null;
	private static String dictFile = null;
	private static String sequence;
	private static String letters;
	private static String[] sequenceArr;
	private static String[] hintPatternArr;
	private static List<List<Character>> hintLetters = new ArrayList<List<Character>>();
	private static boolean[] sequenceHasHint;
	private static int numWords;
	private static int solCounter = 0;
	private static boolean gotResults = false;
	private static boolean quietMode = false;

	public static void main(String[] args) {
		loadArgs(args);
		logStart();
		loadDictionary();
		checkHints();
		startSolver();
		logEnd();
		closeFile();
	}

	private static void solver(List<String> currWords, int currWordsPos, List<Character> currLetters) {
		if (currWordsPos == numWords) {
			gotResults = true;
			printSolution(currWords);
			return;
		}

		int numChars = sequenceArr[currWordsPos].length();
		String allLetters = charListToString(currLetters);

		if (sequenceHasHint[currWordsPos]) {
			allLetters += charListToString(hintLetters.get(currWordsPos)).replaceAll("-", "\\\\-");
		}

		for (String word : dicListO.get(numChars - 1)) {
			if (!word.matches("^[" + allLetters + "].{" + (numChars - 1) + "}$")) continue;
			if (!validMatch(word, allLetters)) continue;
			if (sequenceHasHint[currWordsPos]) if (!matchesHint(word, currWordsPos)) continue;

			List<String> newWords = new ArrayList<String>(currWords);
			newWords.add(word);

			List<Character> newLetters = new ArrayList<Character>(stringToCharList(allLetters));
			for (char c : word.toCharArray()) newLetters.remove(newLetters.indexOf(c));

			solver(newWords, (currWordsPos + 1), newLetters);
		}
	}

	private static void startSolver() {
		writeToScreen("Finding possible solutions...\n");

		List<String> wordsList = new ArrayList<String>();
		List<Character> lettersList = stringToCharList(letters);

		solver(wordsList, 0, lettersList);
	}

	private static boolean validMatch(String word, String letters) {
		for (char c : word.toCharArray()) {
			if (countOccurrences(word, c) > countOccurrences(letters, c)) return false;
		}

		return true;
	}

	private static boolean matchesHint(String word, int pos) {
		if (!word.matches(hintPatternArr[pos])) return false;

		return true;
	}

	private static void checkHints() {
		writeToScreen("Checking hints...");

		sequenceHasHint = new boolean[sequenceArr.length];
		hintPatternArr = new String[sequenceArr.length];

		for (int i=0; i<sequenceArr.length; i++) {
			if (sequenceArr[i].matches(".*[a-zA-ZÀ-ÿ0-9\\-]+.*")) {
				String underscore = "_";

				sequenceHasHint[i] = true;
				hintPatternArr[i] = sequenceArr[i].replaceAll("_", ".");

				List<Character> letters = new ArrayList<Character>();
				for (int j=0; j<sequenceArr[i].length(); j++) {
					Character c = sequenceArr[i].charAt(j);
					if (c != underscore.charAt(0)) letters.add(c);
				}

				hintLetters.add(letters);
			} else {
				sequenceHasHint[i] = false;
				hintPatternArr[i] = ".*";
			}
		}
	}

	private static void writeToScreen(String msg) {
		if (quietMode) return;
		System.out.println(msg);
	}

	private static void printSolution(List<String> solution) {
		solCounter++;
		String label = String.format("%9s", String.valueOf(solCounter) +  " |  ");
		String words = stringListToString(solution);
		writeToScreen(label + words);
		writeToFile(label + words);
	}

	private static void loadDictionary() {
		int maxSize = 0;
		FileReader fileReader = null;
		BufferedReader reader = null;

		writeToScreen("Loading dictionary...");
		
		try {
			fileReader = new FileReader(dictionaryFile);
			reader = new BufferedReader(fileReader);
			for (String line; (line = reader.readLine()) != null; ) {
				dicList.add(line);
				if (line.length() > maxSize) maxSize = line.length();
			}
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
			try {
				if (reader != null) reader.close();	
				if (fileReader != null) fileReader.close();
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}

		writeToScreen("Optimizing dictionary...");

		for (int i=0; i<maxSize; i++) dicListO.add(new ArrayList<String>());
		for (String word : dicList)	dicListO.get(word.length() - 1).add(word);

		dicList.clear();
	}

	private static void loadArgs(String[] args) {
		String hifen = "-";
		int s = 0;

		if (args[0].charAt(0) != hifen.charAt(0)) {
			sequence = args[0];
			letters = args[1];
			s = 2;
		}

		for (int i=s; i<args.length; i++) {
			switch(args[i]) {
				case "-p":
				case "-pattern":
					sequence = args[i+1];
					i++;
					break;
				case "-l":
				case "-letters":
					letters = args[i+1];
					i++;
					break;
				case "-s":
				case "-save":
					saveFile = args[i+1];
					i++;
					break;
				case "-d":
				case "-dictionary":
					dictFile = args[i+1];
					i++;
					break;
				case "-q":
				case "-quiet":
					quietMode = true;
					break;
				default:
					System.out.println("Bad argument \"" + args[i] + "\"!");
					System.out.println("Exiting...");
					System.exit(1);
					break;
			}
		}

		sequenceArr = sequence.split(" ");
		numWords = sequenceArr.length;

		if (dictFile == null) dictFile = "dictionary.txt";
		dictionaryFile = new File(dictFile);

		if (saveFile != null) {
			try {
				fileWriter = new FileWriter(new File(saveFile));
				writer = new BufferedWriter(fileWriter);	
			} catch (IOException e) {
				closeFile();
				e.printStackTrace();
			} 
			saveToFile = true;
		}
	}

	private static void writeToFile(String msg) {
		if (!saveToFile) return;
		try {
			writer.write(msg + "\r\n");
		} catch (IOException e) {
			closeFile();
			e.printStackTrace();
		} 
	}

	private static void closeFile() {
		try {
			if (writer != null) writer.close();	
			if (fileWriter != null) fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private static void logStart() {
		String msg = "Started: " + dateFormat.format(new Date());
		String sep = "----------------------------";

		writeToScreen(msg + "\n" + sep);
		writeToFile(msg + "\r\n" + sep + "\r\n");

		if (quietMode) System.out.println("Running...");
	}

	private static void logEnd() {
		if (!gotResults) {
			String resMsg = "No possible solutions were found.";
			writeToScreen(resMsg);
			writeToFile(resMsg);
		}

		String sep = "\n----------------------------";
		String msg = "Ended: " + dateFormat.format(new Date());

		writeToScreen(sep + "\n" + msg);
		writeToFile(sep + "\r\n" + msg + "\r\n");

		if (quietMode) System.out.println("Completed.");
	}

	private static String stringListToString(List<String> list) {
		String str = "";
		for (String s : list) str += s + " ";

		return str.trim();
	}

	private static List<Character> stringToCharList(String str) {
		List<Character> list = new ArrayList<Character>();
		for (char c : str.toCharArray()) list.add(c);

		return list;
	}

	private static String charListToString(List<Character> list) {
		StringBuilder builder = new StringBuilder();
		for (Character c : list) builder.append(c);

		return builder.toString();
	}

	private static int countOccurrences(String haystack, char needle) {
		int count = 0;
		for (int i=0; i<haystack.length(); i++) {
			if (haystack.charAt(i) == needle) count++;
		}

		return count;
	}

}