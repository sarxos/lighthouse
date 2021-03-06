package sarxos.parser;

import java.util.Hashtable;

import sarxos.parser.FileReader;
import sarxos.parser.Splitter;

/** Klasa s這wnik - ograniczona do String闚 tablica hashowana. */
public class Dictionary extends Hashtable {

	/** Konstruktor 豉duj鉍y has豉 i ich odpowiedniki z pliku ASCII. */
	public Dictionary(String fileName) {
		this.readFromFile(fileName);
	}
	
	/** Dodawanie has豉 wraz z odpowiednikiem do s這wnika. */
	public void addEntry(String key, String value) {			// Nowe s這wo i jego odpowednik do s這wnika.
		this.put(new String(key), new String(value));	// Wstawia s這wo do tablicy hashowanej.
	}
	
	/** Pobieranie odpowiednika has豉 ze s這wnika (na podstawie klucza). */
	public String getEntry(String key) {		// Pobiera wyraz ze s這wnika na podstawie klucza.
		if(!this.isEmpty()) {			// Je�li tablica nie jest pusta
			Object r = this.get(key);	// To pobiera z hashtable odpowiedni klucz
			if(r == null) {									// je�li jest on pusty
				return new String("[err key " + key + "]");	// to zwraca String z b喚dem
			} else {										// lub jesli jest pe軟y - tzn. klucz wystepuje w 
				return (String)r;							// hashtable to rzutuje go na String i zwraca.
			}
		} else {								// Je�li tablica jest pusta to zwraca		
			return new String("[dic empty]");	// warto�� tak�.
		}
	} 
	
	/** Czytanie s這wnika z pliku ASCII. */
	public void readFromFile(String fileName) {		// Jako argument przyjmuje lokalizacj� pliku w /res/.
		FileReader R = new FileReader(fileName);	// Tworzymy nowego czytacza kt鏎y odczyta nasz plik.
		Splitter P = new Splitter(R.getString());	// Tworzymy nowego Splittera (podzia這wca) i dajemu mu Stringa dostarczonego przez czytacza.
		String[] linie = P.split("\n");				// Splitujemy tego Stringa poprzez znak podzia逝 wiersza '\n', 
		String p1 = null;
		String p2 = null;
		String[] u = {"\n", "\r", "\t", "\b"};
		for(int i = 0; i < linie.length; i++) {		// i ponadto ka盥y element tablicy String闚 wynikowych te�
			P.setString(linie[i]);					// splitujemy, jednak tym razem przez znak r闚no�ci '='.
			p1 = P.split("=")[0];
			p2 = P.split("=")[1];
			for(int j = 0; j < u.length; j++) {				// Przeszukujemy Stringi w poszukiwaniu
				if(p1.indexOf(u[j]) != -1) {				// zabronionych znak闚 z tablicy 'u'.
					p1 = p1.substring(0, p1.indexOf(u[j]));	// Gdy kt鏎y� znajdzie, to po prostu obcina Stringa 
				}											// do miejsca gdzie ten znak zabroniony wyst雷i�
				if(p2.indexOf(u[j]) != -1) {				// Tak samo robi i z kluczem i z warto�ci� dla
					p2 = p2.substring(0, p2.indexOf(u[j]));	// danego klucza (klucz = p1, warto�� = p2).
				}
			}
			this.addEntry(p1, p2);	// To co po lewej znaku '=' to klucz, a po prawej to warto��.
		}							// I w ten spos鏏 mamy tablic� hashowan� wype軟ion� wyrazami.
	}	
}