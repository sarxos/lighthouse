package sarxos.graphics;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import sarxos.graphics.Frame;

/** Klasa animacyjna - przekazujesz ref. do ekranu, dodajesz klatki (Frame) i start. */
public abstract class Animation extends Vector {
	
	private Display disp = null;				// Referencja do ekranu.
	private TimerTask frameExchanger = null;	// Zmieniacz klatek (obiekt ZadanieTimera).
	private Timer film = null;					// Engine animacyjne - dzia³anie co okreœlony interwa³.

	private int counter = 0;	// Licznik wykonañ animacji.	
	public int length = 0;		// D³ugoœæ animacji (iloœæ klatek).
	public int speed = 100;		// Szybkoœæ zmiany klatek (domyœlnie 100ms).
	public int loop = -1;		// Iloœæ pêtli po których animacja ma siê zatrzymaæ.
	
	private int lastFrame = 0;	// Ostatnio wykonana klatka.
	
	/** Konstruktor domyœlny - przekazujesz tylko obiekt Display (ma³a efektywnoœæ). */
	public Animation(Display d) {	// Bieda w kraju, naród chleba wo³a (ubogi ten konstruktor).
		this.disp = d;				// Tworzymy referencjê do ekranu.
	}

	/** Konstruktor niedomyœlny - przekazujesz obiekt Display i d³ugoœæ animacji (ile klatek). */
	public Animation(Display d, int capacity) {
		this.disp = d;				// Ustawiamy d³ugoœæ animacji.
		setSize(capacity);			// Ustawiamy d³ugoœæ wektora animacji.
	}
	
	/** Konstruktor niedomyœlny - przekazujesz obiekt Display i tablicê klatek. */
	public Animation(Display d, Frame[] f) {
		this.disp = d;						// Tworzymy referencjê do ekranu.
		setSize(f.length);					// Ustawiamy d³ugoœæ wektora animacji (sama siebie klasa swoja) - metoda odziedziczona
		boolean isEmptyElement = false;		// Flaga okreœlaj¹ca nam czy któryœ z elementów okaza³ siê pusty.
		
		for(int i = 0; i < f.length; i++) {		// Przelatujemy ca³¹ tablicê Frame'sów
			if(f[i] != null) {					// i jeœli zadany element nie jest pusty
				setElementAt(f[i], i);			// to kopiujemy go do wektora animacji (metoda odziedziczona)
				this.length++;					// i zwiêkszamy d³ugoœæ animacji.
			} else {					// Lecz jeœli któryœ z elementów oka¿e siê
				isEmptyElement = true;	// byæ pusty to ustawiamy flagê isEmptyElement na
			}							// true i przekazujemy dalej.
		}
		
		if(isEmptyElement) {	// Jeœli jest ustawiona flaga pustego elementu to aby
			trimToSize();		// zaoszczêdziæ pamiêæ trimujemy wektor animacji do  (metoda odziedziczona)
		}						// wielkoœci wystarczaj¹cej do przechowania istniej¹cych klatek.
	}
	
	/** Dodawanie klatki do animacji. */
	public void addFrame(Frame f) {		// Klatka nale¿y do klasy Frame.
		if(f != null) {					// Jeœli klataka nie jest pusta to
			addElement(f);				// dodajemy j¹ do wektora animacji (metoda odziedziczona)
			this.length++;				// i zwiêkszamy d³ugoœæ animacji.
		}
	}

	/** Ustawianie ogranicznika pêtli animacji. Gdy minie tylê pêtli to siê zatrzyma. */
	public void setLoopBack(int loop) {
		this.loop = loop;				// Ustalamy iloœæ pêtli do wykonania.
	}
	
	/** Ustawianie szybkoœci wykonywania animacji (odstêp miêdzy klatakami w ms). */
	public void setSpeed(int speed) {
		this.speed = speed;				// Ustalamy szybkoœæ animacji - odstêpy miêdzy klatkami.
	}
	
	/** Uruchamianie animacji. */
	public void start() {
		frameExchanger = new TimerTask() {		// Nowe zadanie dla timera do wykonania.
			private int i = lastFrame;			// Wewnêtrzny licznik klatki.
			public synchronized void run() {		// Zsynchronizowana abstrakcyjna metoda uruchomieniowa.
				Frame f = (Frame)(elementAt(i++)); 	// Pobiera obiekt z wektora animacji i konwertuje na Displayable.
				disp.setCurrent(f);					// £aduje go na ekran (obiekt z wektora po konwersji).
				lastFrame = i;				// Zapamiêtuje numer ostatnio wykonanej ramki.
				if(i >= length) {			// Jeœli licznik klatek jest
					i = 0;					// eiêkszy równy d³ugoœci wektora animacji, to
					counter++;								// ustawia go na zero i zwiêksza o jeden cykl wykonania animacji.
					if((counter >= loop) && (loop > 0)) {	// Jeœli minê³y pêtle które
						stop();								// mia³y siê wykonaæ, to
					}										// zatrzymujemy film.
				}
			}
		};
		this.film = new Timer();		// Nowy Timer - bedzie cyklicznie wywo³ywaæ exchangera klatek.
		this.film.scheduleAtFixedRate(	// Dodajemy jako zadanie ze sta³ym interwa³em czasowym.
			frameExchanger, 			// Przekazujemy zmieniacza klatek.	
			0,			 				// Opóznienie wykonania.
			this.speed					// Okres dzia³ania - time interval.
		);
	}

	/** Pausowanie animacji. */
	public void pause() {
		if(this.film != null) {		// Jeœli film by³ wykonywany
			this.film.cancel();		// to go zatrzymujemy,
		}
	}

	/** Zatrzymywanie animacji. */	
	public void stop() {
		if(this.film != null) {		// Jeœli film nie jest pusty (tzn zosta³ wykonany)
			this.film.cancel();		// to go zatrzymujemy,
			this.lastFrame = 0;		// ustawiamy ostatni¹ ramkê na zero.
			
			this.abort();			// Wywo³ujemy abstrakcyjn¹ metodê abort. To co bedzie
									// siê teraz dzia³o zale¿y od programisty.
									
			// Wspomaganie GC - oszczêdŸmy pamiêæ i usuñmy nieu¿ywane referencje.
			this.film = null;			// Ustalamy ¿e film = null. 
			this.frameExchanger = null;	// Ustalamy ¿e to te¿ = null.
		}
	}
	
	/** Abstrakcyjna metoda przerwania. */
	public abstract void abort();
}