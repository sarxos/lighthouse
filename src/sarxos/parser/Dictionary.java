package sarxos.parser;

import java.util.Hashtable;

import sarxos.parser.FileReader;
import sarxos.parser.Splitter;

/** Klasa s³ownik - ograniczona do Stringów tablica hashowana. */
public class Dictionary extends Hashtable {

	/** Konstruktor ³aduj¹cy has³a i ich odpowiedniki z pliku ASCII. */
	public Dictionary(String fileName) {
		this.readFromFile(fileName);
	}
	
	/** Dodawanie has³a wraz z odpowiednikiem do s³ownika. */
	public void addEntry(String key, String value) {			// Nowe s³owo i jego odpowednik do s³ownika.
		this.put(new String(key), new String(value));	// Wstawia s³owo do tablicy hashowanej.
	}
	
	/** Pobieranie odpowiednika has³a ze s³ownika (na podstawie klucza). */
	public String getEntry(String key) {		// Pobiera wyraz ze s³ownika na podstawie klucza.
		if(!this.isEmpty()) {			// Jeœli tablica nie jest pusta
			Object r = this.get(key);	// To pobiera z hashtable odpowiedni klucz
			if(r == null) {									// jeœli jest on pusty
				return new String("[err key " + key + "]");	// to zwraca String z b³êdem
			} else {										// lub jesli jest pe³ny - tzn. klucz wystepuje w 
				return (String)r;							// hashtable to rzutuje go na String i zwraca.
			}
		} else {								// Jeœli tablica jest pusta to zwraca		
			return new String("[dic empty]");	// wartoœæ tak¹.
		}
	} 
	
	/** Czytanie s³ownika z pliku ASCII. */
	public void readFromFile(String fileName) {		// Jako argument przyjmuje lokalizacjê pliku w /res/.
		FileReader R = new FileReader(fileName);	// Tworzymy nowego czytacza który odczyta nasz plik.
		Splitter P = new Splitter(R.getString());	// Tworzymy nowego Splittera (podzia³owca) i dajemu mu Stringa dostarczonego przez czytacza.
		String[] linie = P.split("\n");				// Splitujemy tego Stringa poprzez znak podzia³u wiersza '\n', 
		String p1 = null;
		String p2 = null;
		String[] u = {"\n", "\r", "\t", "\b"};
		for(int i = 0; i < linie.length; i++) {		// i ponadto ka¿dy element tablicy Stringów wynikowych te¿
			P.setString(linie[i]);					// splitujemy, jednak tym razem przez znak równoœci '='.
			p1 = P.split("=")[0];
			p2 = P.split("=")[1];
			for(int j = 0; j < u.length; j++) {				// Przeszukujemy Stringi w poszukiwaniu
				if(p1.indexOf(u[j]) != -1) {				// zabronionych znaków z tablicy 'u'.
					p1 = p1.substring(0, p1.indexOf(u[j]));	// Gdy któryœ znajdzie, to po prostu obcina Stringa 
				}											// do miejsca gdzie ten znak zabroniony wyst¹pi³
				if(p2.indexOf(u[j]) != -1) {				// Tak samo robi i z kluczem i z wartoœci¹ dla
					p2 = p2.substring(0, p2.indexOf(u[j]));	// danego klucza (klucz = p1, wartoœæ = p2).
				}
			}
			this.addEntry(p1, p2);	// To co po lewej znaku '=' to klucz, a po prawej to wartoœæ.
		}							// I w ten sposób mamy tablicê hashowan¹ wype³nion¹ wyrazami.
	}	
}