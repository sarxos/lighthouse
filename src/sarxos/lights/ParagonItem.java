package sarxos.lights;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/** Klasa reprezentuj�ca pojedynczy obiekt �wietlny - latarni� morsk� lub punkt
 * nawigacyjny. Paragon jest pobierany z pliku jako ci�g: sAcB, gdzie s oznacza 
 * �wiat�o, a c cie�. A ilo�c sekund �wiat�a, a B cienia. Czyli na przyk�ad dla 
 * paragonu: 111110000011111000000000000000, mamy skr�cony paragon: s5c5s5c15.
 */
public class ParagonItem {
		
	public String idnum			= null;
	public String name 			= null;
	public String location 		= null;
	public String paragon 		= null;
	public String light 		= null;
	public String description 	= null;
		
	public int meters 	= 0;
	public int miles 	= 0;
	public int match 	= 0;
	
	/** Konstruktor. */
	public ParagonItem() {
	}

	/** Ustawia nowy paragon (z formatu skr�conego). */
	public void setParagon(String paragon) {
		
		byte[] ba = paragon.getBytes();								// Tworzymy ze Stringu paragon tablic� typu byte
		ByteArrayInputStream bais = new ByteArrayInputStream(ba);	// Tworzymy strumie� z tablicy bajt�w
		DataInputStream dis = new DataInputStream(bais);			// Tworzymy strumie� danych z tablicy bajt�w.
		
		byte a = 0;		// Zmienna typu byte, kt�r� czytamy ze strumienia.
		int b = 0;		// Cyfra odczytana jako byte (znak ASCII) zamieniona na typ int.
		char c = 's';	// Pocz�tkowa flaga okre�laj�ca czy jest �wiat�o czy cie�.
		int d = 0;				// Liczba zer lub jedynek w serii.
		boolean s = true;			// Okre�la czy by�o �wiat�o czy cie�.
		String w = new String(""); 	// Wyj�ciowy String (paragon).
		byte[] str = new byte[1]; 	// Tymczaowa tablica typu byte s�u��ca do konwersji char na String
		
		do {						// W p�tli
			try {					// pr�bujemy
				a = dis.readByte();	// czyta� ze strumienia bajt po bajcie
			} 
			catch(IOException e) {	// je�li wystepuje b��d to
				if(s) {										// je�li by�o �wiat�o
					for(int h = 0; h < d; h++) w += "1";	// tworzymy d jedynek
				} else {									// a je�li by� cie�
					for(int h = 0; h < d; h++) w += "0";	// to tworzymy d zer.
				}											// a nast�pnie
				break;			// przerywamy wykonywanie p�tli do/while
			}
			c = (char)a;		// Konwertujemy bajt na znak.
			switch(c) {			// i sprawdzamy jaka by�a jego warto��.
				case 's':									// Jesli 's' to znaczyu ze
					for(int h = 0; h < d; h++) w += "0";	// swiatlo - robimy zaleg�e zera.
					s = true;								// ustawiamy flag� �wiat�a na true
					d = 0;		// nast�pnie zerujemy ilo�� jedynek/zer
					break;	    // i przerywamy wybieranie switch.
				case 'c': 
					for(int h = 0; h < d; h++) w += "1";
					s = false;
					d = 0;
					break;
				default:									// Jesli nie by�o ani 's' ani 'c' to
					str[0] = a;								// zapisujemy do tablicy bajt�w bajt a
					b = Integer.parseInt(new String(str));	// a nast�pnie konwertujemy go przez statyczn� metod� 
					d = (d * 10) + b;						// Integer'a do int i dodajemy do poprzedniej warto�ci * 10.
					break;
			}
		} while(true);
	 	// Zapisujemy nowy pragon (przekszta�cony) w polu klasy.			
		this.paragon = w;
	}
}
