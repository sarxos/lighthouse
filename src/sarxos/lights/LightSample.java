package sarxos.lights;

/** Klasa próbki i dopasowywania jej charakterystyki œwietlnej. */ 
public class LightSample {
		
	private byte[] paragon	= null;	// Wzorzec œwietlny.
	private byte[] sample 	= null;	// Próbki zbierane z nas³uchu zdarzeñ.
	private byte[] tmp 		= null;	// Tablica tymczasowa.
		
	private int pLen 	= -1;		// D³ugoœæ wzorca.
	private int sLen 	= -1;		// D³ugoœæ próbek.
	private int minPos 	= -1;		// Minimalna pozycja najwiêkszego dopasowania.
		
	public int percent 	= 0;		// Procent dopasowania.
	public int promile 	= 0;		// Promil dopasowania.
	public int minSum 	= 0;
		
	/** Konstruktor. */
	public LightSample() {
	}
	
	/** Konstruktor obiektu próbki œwietlnej. */
	public LightSample(String paragon, String sample) {

		this.setParagon(paragon);	// Ustanawiamy nowy paragon (wzorzec doskona³y)
		this.setSample(sample);		// Ustanawiamy now¹ próbkê sygna³u.
	}
		
	/** Ustawia nowy wzorzec charakterystyki œwietlnej. */
	public void setParagon(String paragon) {
			
		if(this.paragon != null) { 			// Wspomaganie GC - jeœli nie jest null to 
		    this.paragon = null;			// ustawiamy na null a dopiero pozniej
		}									// zapisujemy nowa wartosc.
			
		this.pLen = paragon.length();		// Pobieramy d³ugoœæ Stringu paragonu
		this.paragon = new byte[this.pLen];	// i tworzymy tak¹ d³ug¹ tablicê typu byte.
			
		for(int i = 0; i < this.pLen; i++) {						// Przelatujemy ca³y String paragonu i tablicê byte paragonu
			if(paragon.substring(i, i + 1).indexOf("1") != -1) {	// sprawdzaj¹c czy na danym miejscu wystêpuje 1 czy 0 i
				this.paragon[i] = 1;								// na tej podstawie ustwaiamy 1 w tablicy
			} else {												// lub 
				this.paragon[i] = 0;								// zero 0
			}
		}
	}

	/** Ustawia now¹ próbkê charakterystyki œwietlnej. */
	public void setSample(String sample) {
			
		if(this.sample != null) { 			// Wspomaganie GC - jeœli nie jest null to 
		    this.sample = null;				// ustawiamy na null a dopiero pozniej
		}									// zapisujemy nowa wartosc.
			
		this.sLen = sample.length();		// Pobieramy d³ugoœæ Stringu próbki
		this.sample = new byte[this.sLen];	// i tworzymy tak¹ d³ug¹ tablicê typu byte.
			
		for(int i = 0; i < this.sLen; i++) {
			if(sample.substring(i, i + 1).indexOf("1") != -1) {
				this.sample[i] = 1;
			} else {
				this.sample[i] = 0;
			}
		}
	}
		
	/** Znajduje miejsce najwiêkszego dopasowania próbki do wzorca (synchronizacja próbki i wzorca). */
	private void synchronize() {
		
		int i = -1;				// Index tablic paragonu.
		int minPos = -1;		// Domyslnie nie ma pozycji max dopasowania.
		this.minSum = this.pLen;	// Zak³adamy ¿e suma b³êdów == d³ugoœci próbki (duuu¿o).
		int sum = 0;			// Tymczasowa suma b³êdów.
		
		for(i = 0; i < this.pLen; i++) {			// Przelatujemy poprzez tablicê paragonu
			sum = 0;								// i za ka¿dym razem ustawiamy tymczasow¹ sumê b³êdów = 0
			for(int k = 0; k < this.pLen; k++) {				// Przelatujemy przez tablicê paragonu
				sum += Math.abs(sample[i + k] - paragon[k]);	// i dodajemy kolejny znaleziony b³¹d.
			}
			if(sum < minSum) {	// Jeœli suma b³êdów jest mniejsza ni¿ minimalna suma b³êdów
				minSum = sum;	// znaleziona wczeœniej to wtedy ustanawiamy now¹ minimaln¹
				minPos = i;		// sumê i zapisujemy jej pozycjê w próbce.
			}
			if(sum == 0) {		// Jeœli suma b³êdów wynosi zero, to znaczy ¿e 
				minPos = i;		// znaleŸliœmy doskona³e dopasowanie i nie ma co
				break;			// dalej szukaæ, wiêc zapisujemy pozycjê w próbce
			}					// i przerywamy przez break.
		}
		this.minPos = minPos;	// Ustawiamy pole minimalnej pozycji szukania (maxymalbnego dopasowania)
	}
		
	/** Zwraca procent dopasowania próbki do wielokrotnoœci wzorca (procent tysiêczny). */
	public int match() throws LightSampleToShortException {
			
		if(this.sLen < 2 * this.pLen) {
			throw new LightSampleToShortException(
				"Badana próbka jest za krótka wzglêdem wzorca:\n" +
				"d³ugoœæ wzorca: " + this.pLen + "\n" +
				"d³ugoœæ próbki: " + this.sLen
			);
		}
			
		if(this.minPos < 0) {				// Jesli minimalna pozycja dopasowania jest < 0
			this.synchronize();				// to przeprowadŸ szukanie minimalnej pozycji 
		}									// dopasowania.
			
		if(this.minSum < this.pLen / 4) {
		
			int bad = 0;			// Iloœæ znalezionych rozbie¿noœci miêdzy wzorcem, a próbkami.
			int i = this.minPos;	// Minimalny index tablicy próbek (pozycja max dopasowania).
			
			do {										// Powtarzaj.
				for(int k = 0; k < this.pLen; k++) {	//Przebiegnij ca³¹ tablicê wzorca (paragon)
					if(i == this.sLen) break;			// jeœli przez przypadek i >= d³ugoœæ próbek to przerwij.
					if(sample[i] != paragon[k]) {		// Jeœli próbka o zadanym indeksie jest ró¿na od wielokrotnoœci
						bad++;							// wzorca w tym punkcie to zwiêksz iloœæ rozbie¿noœci o 1.
					}  									// ..
					i++;								// Zwiêkszamy index tablicy próbek (id¹ równolegle)
				}
			} while(i < this.sLen);						// Dopóki index jest mniejszy od wielkoœci
			
			int percentOfBad = (bad * 1000) / (this.sLen);	// Procent b³êdów.
			int percent = 1000 - percentOfBad;				// Procent dopasowania (x z 1000).
			this.percent = percent / 10;		            // Procent dopasowania (x z 100).
			this.promile = percent - (this.percent * 10);	// Iloœæ promili dopasowania (Procent.x z 100)
			return (this.percent *10) + this.promile;		// Zwraca procent (% z 1000)
															// tablicy próbek.
		} else {
			
			return 0;
		}
	}
}
