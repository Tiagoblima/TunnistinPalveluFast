/*
    HeLIfast, general purpose language identifier for digital text
	See: Jauhiainen, Lindén, Jauhiainen 2016: "HeLI, a Word-Based Backoff Method for Language Identification" In Proceedings of the 3rd Workshop on Language Technology for Closely Related Languages, Varieties and Dialects (VarDial)
    Copyright (C) 2019 Tommi Jauhiainen
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.*;
import java.util.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.lang.Math.*;
import java.io.*;
import java.net.*;
import java.security.*;

class TunnistinPalveluFast {

// global table holding the language models for all the languages

	private static Table<String, String, Double> gramDict;
	private static Table<String, String, Double> wordDict;
	
// global variable containing all the languages known by the identifier
	
	private static List<String> kielilista = new ArrayList<String>();

// These variables should be moved to a configuration file
// They set the relative frequency to read from each model file

	private static double usedmonos = 0.0000005;
	private static double usedbis = 0.0000005;
	private static double usedtris = 0.0000005;
	private static double usedquads = 0.0000005;
	private static double usedcinqs = 0.0000005;
	private static double usedsexts = 1;
	private static double usedwords = 0.0000005;
	
// this is the penalty value for unseen tokens
	
	private static double gramsakko = 7.0;
	
// This is the maximum length of used character n-grams (setting them to 0 gives the same outcome, but the identifier still divides the words)
	
	private static int maksimingram = 6;
	
	private static int port=8082, maxConnections=0;
	
// We read the file languagelist which includes list of the languages to be included in the repertoire of the language identifier.

	
	public static void main(String[] args) {
		File file = new File("languagelist");
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			while ((text = reader.readLine()) != null) {
				kielilista.add(text);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		
		gramDict = HashBasedTable.create();
		wordDict = HashBasedTable.create();
		
		ListIterator gramiterator = kielilista.listIterator();
		while(gramiterator.hasNext()) {
			Object element = gramiterator.next();
			String kieli = (String) element;

			kayLapi(usedmonos, kieli, "mono");
			kayLapi(usedbis, kieli, "bi");
			kayLapi(usedtris, kieli, "tri");
			kayLapi(usedquads, kieli, "quad");
			kayLapi(usedcinqs, kieli, "cinq");
			kayLapi(usedsexts, kieli, "sext");
			kayLapi(usedwords, kieli, "word");
		}
		System.out.println("Ready to accept queries.");
		doServe();
	}
	
	private static void kayLapi(double usedgrams, String kieli, String tyyppi) {
		Table<String, String, Double> tempDict;
		
		tempDict = HashBasedTable.create();
	
		String seuraava = null;
		String pituustiedosto = null;
	
		if (tyyppi.equals("word")) {
			seuraava = "LanguageModels/" + kieli + ".wordcount";
			pituustiedosto = "LanguageModels/" + kieli + ".words.count";
		}
		else {
			seuraava = "LanguageModels/" + kieli + "-" + tyyppi + ".X";
			pituustiedosto = "LanguageModels/" + kieli + "-" + tyyppi + ".count";
		}
	
		double grampituus = 0;
		double langamount = 0;
	
		File file = new File(pituustiedosto);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			while ((text = reader.readLine()) != null) {
				grampituus = Double.parseDouble(text);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}

		file = new File(seuraava);
		reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			String text = null;
			while ((text = reader.readLine()) != null) {
				String gram = text;
				
				if (tyyppi.equals("word")) {
					gram = gram.replaceAll(".*[1-90]", "");
					gram = gram.replaceAll("$", " ");
				}
				else {
					gram = text.replaceAll(".*[1-90]=", "");
				}

				int amount = Integer.parseInt(text.replaceAll("[^1-90]", ""));
				
				if (amount/grampituus > usedgrams) {
					tempDict.put(gram, kieli, (double) amount);
					langamount = langamount + (double) amount;
				}
				else {
					break;
				}				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}				

		for (Cell<String, String, Double> cell: tempDict.cellSet()){
			double probability = -Math.log10(cell.getValue() / langamount);
			if (tyyppi.equals("word")) {
				wordDict.put(cell.getRowKey(), kieli, probability);
			}
			else {
				gramDict.put(cell.getRowKey(), kieli, probability);
			}
		}
	}
	public static String tunnistaTeksti(String teksti) {
		teksti = teksti.toLowerCase();

		teksti = teksti.replaceAll("[^\\p{L}\\p{M}′']", " ");

		teksti = teksti.replaceAll("  *", " ");
		
		teksti = teksti.replaceAll("^ ", "");
		
		int strLength = teksti.length();
		
		if (strLength == 0) {
			return("xxx0.0");
		}

		Boolean viimeinensana = false;
		
		if (teksti.substring(strLength - 1, strLength).equals(" ")) {
			teksti = teksti.replaceAll(" $", "");
			viimeinensana = true;
		}

		String[] sanat = teksti.split(" ");

		List<String> sanalista = new ArrayList<String>();
		
		double sanamaara = 0;
		double uniquesanat = 0;
				
		for (String ss : sanat) {
			if (sanalista.contains(ss)) {
				sanamaara = sanamaara +1 ;
			}
			else {
				sanalista.add(ss);
				uniquesanat = uniquesanat + 1;
				sanamaara = sanamaara +1 ;
			}
		}
		
		double suhde = sanamaara / uniquesanat;
		
		if (suhde > 15) {
			return("xxx0.0");
		}
		
		Map<String, Double> kielipisteet = new HashMap();
		
		ListIterator pisteiterator = kielilista.listIterator();
		while(pisteiterator.hasNext()) {
			Object element = pisteiterator.next();
			String pistekieli = (String) element;
			kielipisteet.put(pistekieli, 0.0);
		}
		
		double monesko = 0;
		
		for (String sana : sanat) {
			Boolean olisana = false;
			
			Map<String, Double> sanapisteet = new HashMap();
			
			monesko = monesko + 1;
			sana = " " + sana;
			if (monesko == sanamaara) {
				if (viimeinensana) {
					sana = sana + " ";
				}
			}
			else {
				sana = sana + " ";
			}
			
			if (usedwords < 1) {
				if (wordDict.containsRow(sana)) {
					olisana = true;
					pisteiterator = kielilista.listIterator();
					while(pisteiterator.hasNext()) {
						Object element = pisteiterator.next();
						String pistekieli = (String) element;
						if (wordDict.contains(sana,pistekieli)) {
							sanapisteet.put(pistekieli, wordDict.get(sana,pistekieli));
						}
						else {
							sanapisteet.put(pistekieli, gramsakko);
						}
					}
				}
			}
			
			if (!olisana) {
				pisteiterator = kielilista.listIterator();
				while(pisteiterator.hasNext()) {
					Object element = pisteiterator.next();
					String pistekieli = (String) element;
					sanapisteet.put(pistekieli, 0.0);
				}
			}
			
			int t = maksimingram;
			while (t > 0) {
				if (olisana) {
					break;
				}
				else {
					int pituus = sana.length();
					int x = 0;
					int grammaara = 0;
					if (pituus > (t-1)) {
						while (x < pituus - t + 1) {
							String gram = sana.substring(x,x+t);
							if (gramDict.containsRow(gram)) {
								grammaara = grammaara + 1;
								olisana = true;
								
								pisteiterator = kielilista.listIterator();
								while(pisteiterator.hasNext()) {
									Object element = pisteiterator.next();
									String pistekieli = (String) element;
									if (gramDict.contains(gram,pistekieli)) {
										sanapisteet.put(pistekieli, (sanapisteet.get(pistekieli)+gramDict.get(gram,pistekieli)));
									}
									else {
										sanapisteet.put(pistekieli, (sanapisteet.get(pistekieli)+gramsakko));
									}
								}
							}
							x = x + 1;
						}
					}
					if (olisana) {
						pisteiterator = kielilista.listIterator();
						while(pisteiterator.hasNext()) {
							Object element = pisteiterator.next();
							String pistekieli = (String) element;
							sanapisteet.put(pistekieli, (sanapisteet.get(pistekieli)/grammaara));
						}
					}
				}
				t = t -1 ;
			}
			pisteiterator = kielilista.listIterator();
			while(pisteiterator.hasNext()) {
				Object element = pisteiterator.next();
				String pistekieli = (String) element;
				kielipisteet.put(pistekieli, (kielipisteet.get(pistekieli) + sanapisteet.get(pistekieli)));
			}
		}
		
		String voittaja = "xxx";
		Double pienin = gramsakko + 1;
		
		pisteiterator = kielilista.listIterator();
		while(pisteiterator.hasNext()) {
			Object element = pisteiterator.next();
			String pistekieli = (String) element;
			kielipisteet.put(pistekieli, (kielipisteet.get(pistekieli)/sanamaara));
			if (kielipisteet.get(element) < pienin) {
				voittaja = pistekieli;
				pienin = kielipisteet.get(element);
			}
		}
		return (voittaja);
	}
	
	private static void doServe() {
		int i=0;

		try{
		  ServerSocket listener = new ServerSocket(port);
		  Socket server;

		  while((i++ < maxConnections) || (maxConnections == 0)){
			doCommsFast connection;

			server = listener.accept();
			doCommsFast conn_c= new doCommsFast(server);
			Thread t = new Thread(conn_c);
			t.start();
		  }
		} catch (IOException ioe) {
		  System.out.println("IOException on socket listen: " + ioe);
		  ioe.printStackTrace();
		}
	}
}

class doCommsFast implements Runnable {
    private Socket server;
    private String line,input;

    doCommsFast(Socket server) {
      this.server=server;
    }

    public void run () {

      input="";
		
	  List<String> uralilaiset = Arrays.asList("enf", "enh", "fit", "fkv", "izh", "kca", "koi", "kom", "kpv", "krl", "liv", "lud", "mdf", "mhr", "mns", "mrj", "mtm", "myv", "nio", "olo", "sel", "sia", "sjd", "sje", "sjk", "sjt", "sju", "sma", "sme", "smj", "smn", "sms", "udm", "vep", "vot", "vro", "xas", "yrk");

      try {
		BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        PrintStream out = new PrintStream(server.getOutputStream());

        line = in.readLine();

		if (line.length() == 300) {
			
			String lang;
			lang = TunnistinPalveluFast.tunnistaTeksti(line.substring(0,100));
			if (uralilaiset.contains(lang.substring(0,3))) {
				out.println(lang);
			}
			else {
				lang = TunnistinPalveluFast.tunnistaTeksti(line.substring(100,200));
				if (uralilaiset.contains(lang.substring(0,3))) {
					out.println(lang);
				}
				else {
					lang = TunnistinPalveluFast.tunnistaTeksti(line.substring(200,300));
					out.println(lang);
				}
			}				
		}
		else {
			out.println(TunnistinPalveluFast.tunnistaTeksti(line));
		}

        server.close();
      } catch (IOException ioe) {
        System.out.println("IOException on socket listen: " + ioe);
        ioe.printStackTrace();
      }
    }
}
