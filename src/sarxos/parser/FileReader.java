package sarxos.parser;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import java.lang.StringBuffer;

/** Klasa sluzaca do odczytywania plik�w tekstowych. Zamienia znaki w pliku
 * przez bufor tekstowy na Unicode. Za pomoca metody getStringFromFile(String loc)
 * lub getString() zwraca zawarto�� danego pliku.
 */
public class FileReader {
		
	// textTmp przechowuje String odczytany z pliku.
	private String textTmp;
    public boolean odczytano;
		
	/** Domy�lny konstruktor. */
	public FileReader() {
		this.textTmp = new String("");
		this.odczytano = false;
	}

	/** Konstruktor od razu czyta plik. Dost�p do Stringu metod� getString(). */		
	public FileReader(String fLocation) {
		this.textTmp = getStringFromFile(fLocation);
		this.odczytano = true;
	}

	/** Czytanie zawarto�ci pliku. */
	public String getStringFromFile(String fLocation) {
		try {
			InputStream StreamDane = getClass().getResourceAsStream(fLocation);	// Tworzenie strumienia do zasobu (zasoby w katalogu /res)
       		InputStreamReader DaneReader = new InputStreamReader(StreamDane);	// Tworzenie odczytywacza strumienia (po 8 bitow, konwersja na unicode).
           	char[] Bufor = new char[32];										// Tablica tymczasowych znakow - mini buforek.
           	StringBuffer BuforTextowy = new StringBuffer();						// Bufor textowy - laczenie znak�w z tymczasowej tablicy znak�w.
           	int i = 0;															// Zmnienna liczaca znaki
          	while((i = DaneReader.read(Bufor, 0, Bufor.length)) > -1) {										// Dop�ki liczba odczytanych bajt�w jest rozna od zera.
               	BuforTextowy.append(Bufor, 0, i);								// Dodaje do bufora textowego za pomoca met. append()
           	}
   			this.textTmp = BuforTextowy.toString();				// Przeksztalcanie do Stringa o stalej szerokosci.
   			this.odczytano = true;								// Oznaczenie odczytu na true.
   		}
   		// Przechwycenie bledu (jesli powstal)
   		catch(IOException e) {
   			this.textTmp = null;
   			this.odczytano = false;
   		}
   		return this.textTmp;
	}
	
	/** Metoda zwracajaca d�ugo�� odczytanego z pliku Stringu. */
	int getLength() {
		return this.textTmp.length();
	}
	
	/** Zwraca odczytany z pliku String. */
	String getString() {
   		return this.textTmp;
    }
}
