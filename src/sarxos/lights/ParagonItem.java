package sarxos.lights;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/** Klasa reprezentuj¹ca pojedynczy obiekt œwietlny - latarniê morsk¹ lub punkt
 * nawigacyjny. Paragon jest pobierany z pliku jako ci¹g: sAcB, gdzie s oznacza 
 * œwiat³o, a c cieñ. A iloœc sekund œwiat³a, a B cienia. Czyli na przyk³ad dla 
 * paragonu: 111110000011111000000000000000, mamy skrócony paragon: s5c5s5c15.
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

	/** Ustawia nowy paragon (z formatu skróconego). */
	public void setParagon(String paragon) {
		
		byte[] ba = paragon.getBytes();								// Tworzymy ze Stringu paragon tablicê typu byte
		ByteArrayInputStream bais = new ByteArrayInputStream(ba);	// Tworzymy strumieñ z tablicy bajtów
		DataInputStream dis = new DataInputStream(bais);			// Tworzymy strumieñ danych z tablicy bajtów.
		
		byte a = 0;		// Zmienna typu byte, któr¹ czytamy ze strumienia.
		int b = 0;		// Cyfra odczytana jako byte (znak ASCII) zamieniona na typ int.
		char c = 's';	// Pocz¹tkowa flaga okreœlaj¹ca czy jest œwiat³o czy cieñ.
		int d = 0;				// Liczba zer lub jedynek w serii.
		boolean s = true;			// Okreœla czy by³o œwiat³o czy cieñ.
		String w = new String(""); 	// Wyjœciowy String (paragon).
		byte[] str = new byte[1]; 	// Tymczaowa tablica typu byte s³u¿¹ca do konwersji char na String
		
		do {						// W pêtli
			try {					// próbujemy
				a = dis.readByte();	// czytaæ ze strumienia bajt po bajcie
			} 
			catch(IOException e) {	// jeœli wystepuje b³¹d to
				if(s) {										// jeœli by³o œwiat³o
					for(int h = 0; h < d; h++) w += "1";	// tworzymy d jedynek
				} else {									// a jeœli by³ cieñ
					for(int h = 0; h < d; h++) w += "0";	// to tworzymy d zer.
				}											// a nastêpnie
				break;			// przerywamy wykonywanie pêtli do/while
			}
			c = (char)a;		// Konwertujemy bajt na znak.
			switch(c) {			// i sprawdzamy jaka by³a jego wartoœæ.
				case 's':									// Jesli 's' to znaczyu ze
					for(int h = 0; h < d; h++) w += "0";	// swiatlo - robimy zaleg³e zera.
					s = true;								// ustawiamy flagê œwiat³a na true
					d = 0;		// nastêpnie zerujemy iloœæ jedynek/zer
					break;	    // i przerywamy wybieranie switch.
				case 'c': 
					for(int h = 0; h < d; h++) w += "1";
					s = false;
					d = 0;
					break;
				default:									// Jesli nie by³o ani 's' ani 'c' to
					str[0] = a;								// zapisujemy do tablicy bajtów bajt a
					b = Integer.parseInt(new String(str));	// a nastêpnie konwertujemy go przez statyczn¹ metodê 
					d = (d * 10) + b;						// Integer'a do int i dodajemy do poprzedniej wartoœci * 10.
					break;
			}
		} while(true);
	 	// Zapisujemy nowy pragon (przekszta³cony) w polu klasy.			
		this.paragon = w;
	}
}
