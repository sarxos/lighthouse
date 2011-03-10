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
	private Timer film = null;					// Engine animacyjne - dzia�anie co okre�lony interwa�.

	private int counter = 0;	// Licznik wykona� animacji.	
	public int length = 0;		// D�ugo�� animacji (ilo�� klatek).
	public int speed = 100;		// Szybko�� zmiany klatek (domy�lnie 100ms).
	public int loop = -1;		// Ilo�� p�tli po kt�rych animacja ma si� zatrzyma�.
	
	private int lastFrame = 0;	// Ostatnio wykonana klatka.
	
	/** Konstruktor domy�lny - przekazujesz tylko obiekt Display (ma�a efektywno��). */
	public Animation(Display d) {	// Bieda w kraju, nar�d chleba wo�a (ubogi ten konstruktor).
		this.disp = d;				// Tworzymy referencj� do ekranu.
	}

	/** Konstruktor niedomy�lny - przekazujesz obiekt Display i d�ugo�� animacji (ile klatek). */
	public Animation(Display d, int capacity) {
		this.disp = d;				// Ustawiamy d�ugo�� animacji.
		setSize(capacity);			// Ustawiamy d�ugo�� wektora animacji.
	}
	
	/** Konstruktor niedomy�lny - przekazujesz obiekt Display i tablic� klatek. */
	public Animation(Display d, Frame[] f) {
		this.disp = d;						// Tworzymy referencj� do ekranu.
		setSize(f.length);					// Ustawiamy d�ugo�� wektora animacji (sama siebie klasa swoja) - metoda odziedziczona
		boolean isEmptyElement = false;		// Flaga okre�laj�ca nam czy kt�ry� z element�w okaza� si� pusty.
		
		for(int i = 0; i < f.length; i++) {		// Przelatujemy ca�� tablic� Frame's�w
			if(f[i] != null) {					// i je�li zadany element nie jest pusty
				setElementAt(f[i], i);			// to kopiujemy go do wektora animacji (metoda odziedziczona)
				this.length++;					// i zwi�kszamy d�ugo�� animacji.
			} else {					// Lecz je�li kt�ry� z element�w oka�e si�
				isEmptyElement = true;	// by� pusty to ustawiamy flag� isEmptyElement na
			}							// true i przekazujemy dalej.
		}
		
		if(isEmptyElement) {	// Je�li jest ustawiona flaga pustego elementu to aby
			trimToSize();		// zaoszcz�dzi� pami�� trimujemy wektor animacji do  (metoda odziedziczona)
		}						// wielko�ci wystarczaj�cej do przechowania istniej�cych klatek.
	}
	
	/** Dodawanie klatki do animacji. */
	public void addFrame(Frame f) {		// Klatka nale�y do klasy Frame.
		if(f != null) {					// Je�li klataka nie jest pusta to
			addElement(f);				// dodajemy j� do wektora animacji (metoda odziedziczona)
			this.length++;				// i zwi�kszamy d�ugo�� animacji.
		}
	}

	/** Ustawianie ogranicznika p�tli animacji. Gdy minie tyl� p�tli to si� zatrzyma. */
	public void setLoopBack(int loop) {
		this.loop = loop;				// Ustalamy ilo�� p�tli do wykonania.
	}
	
	/** Ustawianie szybko�ci wykonywania animacji (odst�p mi�dzy klatakami w ms). */
	public void setSpeed(int speed) {
		this.speed = speed;				// Ustalamy szybko�� animacji - odst�py mi�dzy klatkami.
	}
	
	/** Uruchamianie animacji. */
	public void start() {
		frameExchanger = new TimerTask() {		// Nowe zadanie dla timera do wykonania.
			private int i = lastFrame;			// Wewn�trzny licznik klatki.
			public synchronized void run() {		// Zsynchronizowana abstrakcyjna metoda uruchomieniowa.
				Frame f = (Frame)(elementAt(i++)); 	// Pobiera obiekt z wektora animacji i konwertuje na Displayable.
				disp.setCurrent(f);					// �aduje go na ekran (obiekt z wektora po konwersji).
				lastFrame = i;				// Zapami�tuje numer ostatnio wykonanej ramki.
				if(i >= length) {			// Je�li licznik klatek jest
					i = 0;					// ei�kszy r�wny d�ugo�ci wektora animacji, to
					counter++;								// ustawia go na zero i zwi�ksza o jeden cykl wykonania animacji.
					if((counter >= loop) && (loop > 0)) {	// Je�li min�y p�tle kt�re
						stop();								// mia�y si� wykona�, to
					}										// zatrzymujemy film.
				}
			}
		};
		this.film = new Timer();		// Nowy Timer - bedzie cyklicznie wywo�ywa� exchangera klatek.
		this.film.scheduleAtFixedRate(	// Dodajemy jako zadanie ze sta�ym interwa�em czasowym.
			frameExchanger, 			// Przekazujemy zmieniacza klatek.	
			0,			 				// Op�znienie wykonania.
			this.speed					// Okres dzia�ania - time interval.
		);
	}

	/** Pausowanie animacji. */
	public void pause() {
		if(this.film != null) {		// Je�li film by� wykonywany
			this.film.cancel();		// to go zatrzymujemy,
		}
	}

	/** Zatrzymywanie animacji. */	
	public void stop() {
		if(this.film != null) {		// Je�li film nie jest pusty (tzn zosta� wykonany)
			this.film.cancel();		// to go zatrzymujemy,
			this.lastFrame = 0;		// ustawiamy ostatni� ramk� na zero.
			
			this.abort();			// Wywo�ujemy abstrakcyjn� metod� abort. To co bedzie
									// si� teraz dzia�o zale�y od programisty.
									
			// Wspomaganie GC - oszcz�d�my pami�� i usu�my nieu�ywane referencje.
			this.film = null;			// Ustalamy �e film = null. 
			this.frameExchanger = null;	// Ustalamy �e to te� = null.
		}
	}
	
	/** Abstrakcyjna metoda przerwania. */
	public abstract void abort();
}