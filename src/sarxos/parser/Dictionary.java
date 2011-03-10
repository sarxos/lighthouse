package sarxos.parser;

import java.util.Hashtable;

import sarxos.parser.FileReader;
import sarxos.parser.Splitter;

/** Klasa s�ownik - ograniczona do String�w tablica hashowana. */
public class Dictionary extends Hashtable {

	/** Konstruktor �aduj�cy has�a i ich odpowiedniki z pliku ASCII. */
	public Dictionary(String fileName) {
		this.readFromFile(fileName);
	}
	
	/** Dodawanie has�a wraz z odpowiednikiem do s�ownika. */
	public void addEntry(String key, String value) {			// Nowe s�owo i jego odpowednik do s�ownika.
		this.put(new String(key), new String(value));	// Wstawia s�owo do tablicy hashowanej.
	}
	
	/** Pobieranie odpowiednika has�a ze s�ownika (na podstawie klucza). */
	public String getEntry(String key) {		// Pobiera wyraz ze s�ownika na podstawie klucza.
		if(!this.isEmpty()) {			// Je�li tablica nie jest pusta
			Object r = this.get(key);	// To pobiera z hashtable odpowiedni klucz
			if(r == null) {									// je�li jest on pusty
				return new String("[err key " + key + "]");	// to zwraca String z b��dem
			} else {										// lub jesli jest pe�ny - tzn. klucz wystepuje w 
				return (String)r;							// hashtable to rzutuje go na String i zwraca.
			}
		} else {								// Je�li tablica jest pusta to zwraca		
			return new String("[dic empty]");	// warto�� tak�.
		}
	} 
	
	/** Czytanie s�ownika z pliku ASCII. */
	public void readFromFile(String fileName) {		// Jako argument przyjmuje lokalizacj� pliku w /res/.
		FileReader R = new FileReader(fileName);	// Tworzymy nowego czytacza kt�ry odczyta nasz plik.
		Splitter P = new Splitter(R.getString());	// Tworzymy nowego Splittera (podzia�owca) i dajemu mu Stringa dostarczonego przez czytacza.
		String[] linie = P.split("\n");				// Splitujemy tego Stringa poprzez znak podzia�u wiersza '\n', 
		String p1 = null;
		String p2 = null;
		String[] u = {"\n", "\r", "\t", "\b"};
		for(int i = 0; i < linie.length; i++) {		// i ponadto ka�dy element tablicy String�w wynikowych te�
			P.setString(linie[i]);					// splitujemy, jednak tym razem przez znak r�wno�ci '='.
			p1 = P.split("=")[0];
			p2 = P.split("=")[1];
			for(int j = 0; j < u.length; j++) {				// Przeszukujemy Stringi w poszukiwaniu
				if(p1.indexOf(u[j]) != -1) {				// zabronionych znak�w z tablicy 'u'.
					p1 = p1.substring(0, p1.indexOf(u[j]));	// Gdy kt�ry� znajdzie, to po prostu obcina Stringa 
				}											// do miejsca gdzie ten znak zabroniony wyst�pi�
				if(p2.indexOf(u[j]) != -1) {				// Tak samo robi i z kluczem i z warto�ci� dla
					p2 = p2.substring(0, p2.indexOf(u[j]));	// danego klucza (klucz = p1, warto�� = p2).
				}
			}
			this.addEntry(p1, p2);	// To co po lewej znaku '=' to klucz, a po prawej to warto��.
		}							// I w ten spos�b mamy tablic� hashowan� wype�nion� wyrazami.
	}	
}