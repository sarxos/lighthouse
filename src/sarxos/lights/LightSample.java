package sarxos.lights;

/** Klasa pr�bki i dopasowywania jej charakterystyki �wietlnej. */ 
public class LightSample {
		
	private byte[] paragon	= null;	// Wzorzec �wietlny.
	private byte[] sample 	= null;	// Pr�bki zbierane z nas�uchu zdarze�.
	private byte[] tmp 		= null;	// Tablica tymczasowa.
		
	private int pLen 	= -1;		// D�ugo�� wzorca.
	private int sLen 	= -1;		// D�ugo�� pr�bek.
	private int minPos 	= -1;		// Minimalna pozycja najwi�kszego dopasowania.
		
	public int percent 	= 0;		// Procent dopasowania.
	public int promile 	= 0;		// Promil dopasowania.
	public int minSum 	= 0;
		
	/** Konstruktor. */
	public LightSample() {
	}
	
	/** Konstruktor obiektu pr�bki �wietlnej. */
	public LightSample(String paragon, String sample) {

		this.setParagon(paragon);	// Ustanawiamy nowy paragon (wzorzec doskona�y)
		this.setSample(sample);		// Ustanawiamy now� pr�bk� sygna�u.
	}
		
	/** Ustawia nowy wzorzec charakterystyki �wietlnej. */
	public void setParagon(String paragon) {
			
		if(this.paragon != null) { 			// Wspomaganie GC - je�li nie jest null to 
		    this.paragon = null;			// ustawiamy na null a dopiero pozniej
		}									// zapisujemy nowa wartosc.
			
		this.pLen = paragon.length();		// Pobieramy d�ugo�� Stringu paragonu
		this.paragon = new byte[this.pLen];	// i tworzymy tak� d�ug� tablic� typu byte.
			
		for(int i = 0; i < this.pLen; i++) {						// Przelatujemy ca�y String paragonu i tablic� byte paragonu
			if(paragon.substring(i, i + 1).indexOf("1") != -1) {	// sprawdzaj�c czy na danym miejscu wyst�puje 1 czy 0 i
				this.paragon[i] = 1;								// na tej podstawie ustwaiamy 1 w tablicy
			} else {												// lub 
				this.paragon[i] = 0;								// zero 0
			}
		}
	}

	/** Ustawia now� pr�bk� charakterystyki �wietlnej. */
	public void setSample(String sample) {
			
		if(this.sample != null) { 			// Wspomaganie GC - je�li nie jest null to 
		    this.sample = null;				// ustawiamy na null a dopiero pozniej
		}									// zapisujemy nowa wartosc.
			
		this.sLen = sample.length();		// Pobieramy d�ugo�� Stringu pr�bki
		this.sample = new byte[this.sLen];	// i tworzymy tak� d�ug� tablic� typu byte.
			
		for(int i = 0; i < this.sLen; i++) {
			if(sample.substring(i, i + 1).indexOf("1") != -1) {
				this.sample[i] = 1;
			} else {
				this.sample[i] = 0;
			}
		}
	}
		
	/** Znajduje miejsce najwi�kszego dopasowania pr�bki do wzorca (synchronizacja pr�bki i wzorca). */
	private void synchronize() {
		
		int i = -1;				// Index tablic paragonu.
		int minPos = -1;		// Domyslnie nie ma pozycji max dopasowania.
		this.minSum = this.pLen;	// Zak�adamy �e suma b��d�w == d�ugo�ci pr�bki (duuu�o).
		int sum = 0;			// Tymczasowa suma b��d�w.
		
		for(i = 0; i < this.pLen; i++) {			// Przelatujemy poprzez tablic� paragonu
			sum = 0;								// i za ka�dym razem ustawiamy tymczasow� sum� b��d�w = 0
			for(int k = 0; k < this.pLen; k++) {				// Przelatujemy przez tablic� paragonu
				sum += Math.abs(sample[i + k] - paragon[k]);	// i dodajemy kolejny znaleziony b��d.
			}
			if(sum < minSum) {	// Je�li suma b��d�w jest mniejsza ni� minimalna suma b��d�w
				minSum = sum;	// znaleziona wcze�niej to wtedy ustanawiamy now� minimaln�
				minPos = i;		// sum� i zapisujemy jej pozycj� w pr�bce.
			}
			if(sum == 0) {		// Je�li suma b��d�w wynosi zero, to znaczy �e 
				minPos = i;		// znale�li�my doskona�e dopasowanie i nie ma co
				break;			// dalej szuka�, wi�c zapisujemy pozycj� w pr�bce
			}					// i przerywamy przez break.
		}
		this.minPos = minPos;	// Ustawiamy pole minimalnej pozycji szukania (maxymalbnego dopasowania)
	}
		
	/** Zwraca procent dopasowania pr�bki do wielokrotno�ci wzorca (procent tysi�czny). */
	public int match() throws LightSampleToShortException {
			
		if(this.sLen < 2 * this.pLen) {
			throw new LightSampleToShortException(
				"Badana pr�bka jest za kr�tka wzgl�dem wzorca:\n" +
				"d�ugo�� wzorca: " + this.pLen + "\n" +
				"d�ugo�� pr�bki: " + this.sLen
			);
		}
			
		if(this.minPos < 0) {				// Jesli minimalna pozycja dopasowania jest < 0
			this.synchronize();				// to przeprowad� szukanie minimalnej pozycji 
		}									// dopasowania.
			
		if(this.minSum < this.pLen / 4) {
		
			int bad = 0;			// Ilo�� znalezionych rozbie�no�ci mi�dzy wzorcem, a pr�bkami.
			int i = this.minPos;	// Minimalny index tablicy pr�bek (pozycja max dopasowania).
			
			do {										// Powtarzaj.
				for(int k = 0; k < this.pLen; k++) {	//Przebiegnij ca�� tablic� wzorca (paragon)
					if(i == this.sLen) break;			// je�li przez przypadek i >= d�ugo�� pr�bek to przerwij.
					if(sample[i] != paragon[k]) {		// Je�li pr�bka o zadanym indeksie jest r�na od wielokrotno�ci
						bad++;							// wzorca w tym punkcie to zwi�ksz ilo�� rozbie�no�ci o 1.
					}  									// ..
					i++;								// Zwi�kszamy index tablicy pr�bek (id� r�wnolegle)
				}
			} while(i < this.sLen);						// Dop�ki index jest mniejszy od wielko�ci
			
			int percentOfBad = (bad * 1000) / (this.sLen);	// Procent b��d�w.
			int percent = 1000 - percentOfBad;				// Procent dopasowania (x z 1000).
			this.percent = percent / 10;		            // Procent dopasowania (x z 100).
			this.promile = percent - (this.percent * 10);	// Ilo�� promili dopasowania (Procent.x z 100)
			return (this.percent *10) + this.promile;		// Zwraca procent (% z 1000)
															// tablicy pr�bek.
		} else {
			
			return 0;
		}
	}
}
