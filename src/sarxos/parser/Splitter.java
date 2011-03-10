package sarxos.parser;

/** Klasa s³u¿¹ca do rozdzielania Stringów na tablicê pod³añcuchów. */
public class Splitter {
	
	private String str = null;		// Pole Stringa.
	
	/** Domyœlny konstruktor. */
	public Splitter() {
	}	
	
	/** Konstruktor z argumentem typu String - od razu zapisuje w sobie text. */
	public Splitter(String str) {	// Konstruktor kopiujacy.
		this.str = str;
	}	
	
	/** Ustalanie wartoœci nowego Stringa. */
	public void setString(String str) {	
		this.str = str;				// Ustala nowy String do splitowania.
	}
	
	/** Metoda splituje zapisany tekst i zwraca tablice Stringów. */
	public String[] split(String delimiter) {	// Jako argument dostarczamy znak wg ktorego nastapi podzial
		
		int index;								// Inicjacja zmiennej indexowej - polozenie w stringu danego ciagu.
		int licznik = 0;						// Licznik - ile razy dany delimiter wystapil.
		
		String tmp = this.str;								// Skopiowanie stringa do zmiennej tymczasowej tmp.
		while((index = tmp.indexOf(delimiter)) != -1) {		// Wyszukiwanie w tmp delimitera (dopoki on wystepuje)
			tmp = tmp.substring(index + 1, tmp.length());	// Skracanie tmp o stringa przed delimiterem i o samego delimitera.
			licznik++;										// Zwiekszenie licznika wystapien.
		}
		if(tmp.length() > 0) licznik++;				// Jesli tmp nie jest == 0 to skopiuj reszte do ostatniego elementu tablicy.
		String[] collection = new String[licznik];	// Nowa kolekcja - tablica stringow dlugosci takiej ile 4razy wystapil delimiter.
		tmp = this.str;								// Powtorne kopiowanie stringa.
		licznik = 0;											// Zerowanie licznika wystapien.
		while((index = tmp.indexOf(delimiter)) != -1) {			// Powtorne trawersowanie stringa
			collection[licznik] = tmp.substring(0, index);		// w poszukiwaniu delimiterow
			tmp = tmp.substring(index + 1, tmp.length());		// i kopiowanie tego co miedzy nimi sie znajduje.
			licznik++;
		}
		if(tmp.length() > 0) collection[licznik] = tmp;	// Dopisuje ostatni element (o ile wystepuje)
		
		return collection;								// i zwraca.
	}
	
	public static String[] split(String str, String delimiter) {	// Jako argument dostarczamy znak wg ktorego nastapi podzial
		
		int index;								// Inicjacja zmiennej indexowej - polozenie w stringu danego ciagu.
		int licznik = 0;						// Licznik - ile razy dany delimiter wystapil.
		
		String tmp = str;									// Skopiowanie stringa do zmiennej tymczasowej tmp.
		while((index = tmp.indexOf(delimiter)) != -1) {		// Wyszukiwanie w tmp delimitera (dopoki on wystepuje)
			tmp = tmp.substring(index + 1, tmp.length());	// Skracanie tmp o stringa przed delimiterem i o samego delimitera.
			licznik++;										// Zwiekszenie licznika wystapien.
		}
		if(tmp.length() > 0) licznik++;				// Jesli tmp nie jest == 0 to skopiuj reszte do ostatniego elementu tablicy.
		String[] collection = new String[licznik];	// Nowa kolekcja - tablica stringow dlugosci takiej ile 4razy wystapil delimiter.
		tmp = str;									// Powtorne kopiowanie stringa.
		licznik = 0;											// Zerowanie licznika wystapien.
		while((index = tmp.indexOf(delimiter)) != -1) {			// Powtorne trawersowanie stringa
			collection[licznik] = tmp.substring(0, index);		// w poszukiwaniu delimiterow
			tmp = tmp.substring(index + 1, tmp.length());		// i kopiowanie tego co miedzy nimi sie znajduje.
			licznik++;
		}
		if(tmp.length() > 0) collection[licznik] = tmp;	// Dopisuje ostatni element (o ile wystepuje)
		
		return collection;								// i zwraca.
	}
	
} 