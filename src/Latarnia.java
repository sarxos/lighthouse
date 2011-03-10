import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

import sarxos.sxrms.Store;
import sarxos.parser.Dictionary;
import sarxos.parser.FileReader;
import sarxos.lights.LightSampleCollector;
import sarxos.lights.LightSampleManager;
import sarxos.lights.LightSampleInfo;
import sarxos.lights.ParagonItem;
import sarxos.graphics.Frame;
import sarxos.graphics.Animation;
import sarxos.graphics.ImageLoader;
import sarxos.graphics.StringPainter;

/** Klasa bazowa aplikacji Latarnia (C) 2005 by SarXos. */
public class Latarnia extends MIDlet implements CommandListener {

	// To trzeba ³adowaæ póxniej z RMS - teraz s¹ to ustawienia domyœlne - czyli DEFAULT
	 
	public final String APP_TITLE 			= this.getAppProperty("MIDDlet-name");			// Nazwa aplikacji.
	public final String RMS_CONFIG			= this.getAppProperty("SarXos-ConfigName");		// Nazwa konfiguracyjnego pakietu RMS.
	public final String IMAGE_LIGHTHOUSE 	= new String("/images/tlo.png");				// Rysunek do menu g³ównego.
	public final String IMAGE_ICON 			= new String("/images/ikonka.png");								// Ikonka wyœwietlana przy danej pozycji paragonu.
	public final int SAMPLING_SPEED 		= Integer.parseInt(this.getAppProperty("SarXos-SampSpeed"));	// Prêdkoœæ zbierania próbek (co 200ms).
	public final String PARAGONS_FILE 		= new String("/paragons/paragons.txt");							// Plik z paragonami.
	public String DefaultLang 				= this.getAppProperty("SarXos-DefLanguage");	// Jêzyk domyœlny (ustawiamy w MIDlet properties KToolbar).

	// Wyœwietlacz (obiekt-uchwyt do ekranu).
	public Display display = Display.getDisplay(this);	// Pobieramy uchwyt ekranu.
	// Ekran g³ówny (obiekt wyœwietlany).
	public Frame ekranGlowny = null;		// Ekran g³ówny - z niego przeprowadzaæ
	// Wracacz.
	public Displayable ostatni = null;		// Obiekt w którym bêdziemy przechowywaæ ostatnio widoczny ekran.
	public Displayable przedostatni = null;	// Obiekt w którym bêdziemy przechowywaæ przedostatnio widoczny ekran.
	public FileReader freader = new FileReader();
	// S³ownik.
	public Dictionary dic = new Dictionary(	// Tworzymy s³ownik wyrazów (otwieramy po 
		"/lang/" + DefaultLang + ".txt"		// prostu z pliku domyœlnego okreœlonegoprzez jêzyk domyœlny  
	);										// zapisany w RMS i rozszerzenie txt.

	// Kolekcjoner próbek œwietlnych.
	public LightSampleCollector sampc = null;
	// Analizator próbek œwietlnych.
	public LightSampleManager manager = null;
	// Wyœwietlacz pomocy z plików.
	public StringPainter helper = null;

	// Ramki animacji.												
	public Frame[] ramki = {					// Tworzymy ramki animacji podstawowej.	
		new Frame("/images/powitanie_1.png"),	// SarXos Design ...
		new Frame("/images/powitanie_2.png"),	// Latarnia, the Lighthosuse ...
		new Frame("/images/blank.png")			// (pustoœæ) ... (ostatnia ramka i tak znika)
	};

	// Animacja wejœciowa.
	public Animation ani = new Animation(display, ramki) {	// Utworzone ramki animacji przekazujemy 
															// do konstruktora i pokrywamy metodê abort() animacji.
		public void abort() {					// Pokrywamy abstrakcyjn¹ metodê abort() - ten kod zostanie
			ani = null;							// wykonany gdy wyst¹pi warunek zatrzymania animacji.
			ramki = null;						// Usuwamy animacjê i jej ramki - niech nie zaœmiecaj¹ pamiêci.
			display.setCurrent(ekranGlowny);	// a nastêpnie go wyœwietlamy.
		}
	};

	// Tablica poleceñ g³ównego ekranu.
	public Command[] cmds = new Command[5];

	// Jedno polecenie osobne - WSTECZ.
	public Command back = null;

	/** Tworzenie poleceñ podstawowych. */
	public void createCommands() {
	
		// Tworzymy polecenia g³ównego ekranu.
		this.cmds[0] = new Command(dic.getEntry("exit"), Command.EXIT, 0);		// Polecenie 'Wyjœcie'.
		this.cmds[1] = new Command(dic.getEntry("meas"), Command.SCREEN, 1);	// Polecenie 'Pomiar'.
		this.cmds[2] = new Command(dic.getEntry("lang"), Command.SCREEN, 1);	// Polecenie 'Jêzyk'.
		this.cmds[3] = new Command(dic.getEntry("help"), Command.SCREEN, 1);	// Polecenie 'Pomoc'.
		this.cmds[4] = new Command(dic.getEntry("data"), Command.SCREEN, 1);	// Polecenie 'Wzorce'.
		// Tworzymy obiekt polecenia wstecz.
		this.back = new Command(dic.getEntry("back"), Command.BACK, 0);
	}

	/** CommandAction - pokrycie metody abstrakcyjnej z CommandListener'a - pods³uch zdarzeñ. */
	public void commandAction(Command c, Displayable d) {
		
		int cmdNum = -1;						// Poczatkowy numer polecenia = -1 (nie ma takiego, czyli
												// ¿adne nie zostanie wybrane przez przypadek).
		int i = 0;								// Zmienna 'i' na pocz¹tku zero.
		int k = 0;								// Zmienna 'k' na pocz¹tku zero.

		for(k = 0; k < this.cmds.length; k++) {	// Porównujemy po kolei ka¿d¹ komendê z
			if(c == this.cmds[k]) cmdNum = i;	// tabl;icy komend, z t¹ która zosta³a przekazana 
			i++;								// jako argument zdarzenia i ustawiamy numer.
		}

		System.gc();			// Sprzatamy œmieci.	

		if(cmdNum >= 0) {		// Jesli polecenie zostalo wybrane (jego numer > 0).
			switch(cmdNum) {	// Mamy dostêpne pozycje dla odpowiednich numerów poleceñ.
				case 0:						
					// Wybrano KONIEC
					this.exitApp();		// To wychodzi z aplikacji.
					break;				// break.
				case 1:						
					// Wybrano POMIAR
					this.ostatni = this.display.getCurrent();	// Pobiera ostatni ekran
					this.display.setCurrent((Canvas)this.sampc);
					break;
				case 2:										
					// Wybrano JÊZYK
					this.ostatni = this.display.getCurrent();	// Pobiera ostatni ekran
					this.menuLanguage();						// i w³¹cza menu wyboru jezyka.
					break;										// break.
				case 3:
					// Wybrano POMOC.
					this.ostatni = this.display.getCurrent();	// Pobiera ostatni ekran
					this.display.setCurrent(this.helper);
					break;
				case 4:
					this.ostatni = this.display.getCurrent();	// Pobiera ostatni ekran
					this.showAllParagons();
					break;
				case 5:
					// Wybrano WRÓÆ.
					this.display.setCurrent(this.ostatni);
					break;
			}
		}
	}

	/** Tworzymy g³ówny ekran aplikacji. */
	public Frame formEkranGlowny() {

		Frame ekran = new Frame(this.IMAGE_LIGHTHOUSE);	// Tworzymy nowy obiekt wyœwietlany - formê o nazwie
		for(int i = 0; i < this.cmds.length; i++) {		// Przechodzimy tablicê poleceñ i ka¿de z nich
			ekran.addCommand(this.cmds[i]);				// dodajemy do utworzonej formy 'ekran'.
		}
		ekran.setCommandListener(this);		// Ustawiamy jako obiekt nas³uchuj¹cy zdarzenia poleceñ
		return ekran;	
	}

	/** Tworzymy menu wyboru jêzyka. */
	public void menuLanguage() {
		
		List ekran = new List("Menu", Choice.IMPLICIT);			// Tworzymy nmowy obiekt 'lista wyboru' (radio)
		
		Image znaczek = ImageLoader.load("/images/choice.png");	// Tworzymy obrazek (haczyk oznaczaj¹cy w³aczony jêzyk)
		Image krzyzyk = ImageLoader.load("/images/brak.png");	// Tworzymy obrazek (krzy¿yk oznaczaj¹cy wy³aczony jêzyk)
			
		if(this.DefaultLang.indexOf("pl") != -1) {	// Sprawdzamy czy w domyœlnie ustawionym aktualnie jêzyku wystêpuje 'pl'
			ekran.append("Polski", znaczek);		// i jesli tak to dodajemy jêzyk polski zer znaczkiem 'ok'
		} else {									// lecz jeœli nie
			ekran.append("Polski", krzyzyk);		// to dodajemy jêzyk polski ale ze znaczniem 'krzy¿yk'.
		}
		if(this.DefaultLang.indexOf("en") != -1) {	// Dodajemy jêzyk angielski.
			ekran.append("English", znaczek);	
		} else {
			ekran.append("English", krzyzyk);
		}
		if(this.DefaultLang.indexOf("de") != -1) {	// Dodajemy jêzyk niemiecki.
			ekran.append("Deutsche", znaczek);	
		} else {
			ekran.append("Deutsche", krzyzyk);
		}

		// Nas³uchiwacz zdarzeñ dla menu Jêzyka - UCHO ajnc.
		CommandListener ucho = new CommandListener() {				// Obs³uga zdarzeñ dla wytworzonej przez nas listy.
			public void commandAction(Command c, Displayable d) {	// Pokrywamy abstrakcjê.
				
				if(c == List.SELECT_COMMAND) {			// Jeœli naciœnieæie klawisza by³o wybraniem pozycji listy
					Choice wybor = (Choice)d;			// to rzutujemy pobrany obiekt aktualnie wyœwietlony na obiekt
					int w = wybor.getSelectedIndex();	// klasy 'Choice' i poprzez jego metody pobieramy indeks zaznaczonego
														// na liœcie elementu.
					switch(w) {
						case 0:
							DefaultLang = "pl";			// Wybrano jêzyk polski
							break;
						case 1:
							DefaultLang = "en";			// Wybrano jêzyk angielski.
							break;
						case 2:
							DefaultLang = "de";			// Wybrano jêzyk niemiecki.
							break;
					}
					
					Store config = new Store(RMS_CONFIG);	// Otwietramy pakiet z konfiguracj¹ MIDletu.
					config.setRecord(1, DefaultLang);		// Uaktualniamy pole z jêzykiem
					config.close();							// a nastêpnie zamykamy pakiet.
					config = null;							// Usuwamy referencjê.
					
					dic = new Dictionary(					// Tworzymy nowy s³ownik z odpowiednim zestawem s³ów.
						"/lang/" + DefaultLang + ".txt"		// S³owa
					);
					
					// Odnawianie jêzyka.
					sampc.title = dic.getEntry("collecting");	// Tytu³ widoczny w canvasie,
					sampc.back = dic.getEntry("back");			// nazwa przycisku 'zbieraj'
					sampc.analysis = dic.getEntry("analysis");	// nazwa przycisku 'analizuj'
					sampc.press = dic.getEntry("press");		// info o nacisnieciu klawisza
					
					helper = new StringPainter("/help/" + DefaultLang + ".txt", dic) {	// Tworzymy nowego helpera.
						public void back() {										// Analogicznie jak w starym nadpisujemy abstrakcjê.
							display.setCurrent(ostatni);
						}	
					};

					createCommands();					// Tworzymy polecenia ze zmienionym jêzykiem.
					ekranGlowny = formEkranGlowny();	// Tworzymy zawartoœæ ekranu g³ównego (ze zmienionym jêzykiem),
					display.setCurrent(ekranGlowny);	// a nastêpnie go wyœwietlamy.
				}
				if(c == back) {						// Jeœli wybranym poleceniem by³o 'Wstecz' to
					display.setCurrent(ostatni);	// wyœwietlamy obiekt który poprzednio goœci³ na
				}									// naszym LCD wyœwietlaczu komórki.
			}
		};
		ekran.addCommand(back);				// Dodaje polecenie 'Wróæ' do ekranu.
		ekran.setCommandListener(ucho);		// Ustawia obiekt 'ucho' jako nas³uch zdarzeñ ekranu.
		display.setCurrent(ekran);			// Ustawia aktualny ekran jako 'ekran'.
	}
	
	/** £adujemy zapisan¹ w RMS konfiguracjê. */
	public void setConstConfiguration() {
		Store st = new Store(this.RMS_CONFIG);	// Otwieramy pakiet RMS o nazwie LatarniaConfig
		if(!st.isRecord(1)) {					// Sprawdzamy czy jest re4kord o identyfikatorze 1
			st.addRecord(this.DefaultLang);		// a je¿li nie ma to zapisujemy go.
		} else {										// Lecz jeœli jest ten rekord ustawiony to
			this.DefaultLang = st.getRecordString(1);	// czytamy z niego domyslny jêzyk (zapisany).
		}
		st.close();								// Zamykamy RecordStore.
		dic = new Dictionary(					// Uaktualniamy s³ownik
			"/lang/" + DefaultLang + ".txt"		// ³aduj¹c nowy plik ze s³owami.
		);
	}

	/** Wyœwietlamy rezultaty analizy próbek otrzymanych ze zbieracza próbek (sampc). */
	public void showResult() {

		// Nas³uchiwacz zdarzeñ dla menu wyboru lokalizacji - UCHO cwaj.
		CommandListener ucho = new CommandListener() {				// Obs³uga zdarzeñ dla wytworzonej przez nas listy.
			public void commandAction(Command c, Displayable d) {	// Pokrywamy abstrakcjê.

				if(c == List.SELECT_COMMAND) {			// Jeœli naciœnieæie klawisza by³o wybraniem pozycji listy
					Choice wybor = (Choice)d;			// to rzutujemy pobrany obiekt aktualnie wyœwietlony na obiekt
					int w = wybor.getSelectedIndex();	// klasy 'Choice' i poprzez jego metody pobieramy indeks zaznaczonego
														// na liœcie elementu.

					ParagonItem pi = manager.paragons[manager.podium[w]];	// Pobieramy wybran¹ pzrzez u¿ytkownika lokalizacjê.
					LightSampleInfo inf = new LightSampleInfo(pi, dic) {	// Tworzymy obiekt ywœwietlacza pomoacy.
						public void back() {								// Nadpisujemy abstrakcjê back() wiêc gdy user kliknie
							display.setCurrent(przedostatni);				// w jakiœ klawisz to wyœwietli siê przedostatni ekran.
						}
					};
					przedostatni = display.getCurrent();	// Przedostatni zostaje zapisany jako aktualny.
					display.setCurrent(inf);				// Wyœwietlamy nastêpny ekran - info o lokalizacji.
				}
				if(c == back) {						// Jeœli wybranym poleceniem by³o 'Wstecz' to
					display.setCurrent(ostatni);	// wyœwietlamy obiekt który poprzednio goœci³ na
				}									// naszym LCD wyœwietlaczu komórki.
			}
		};

		List ekran = new List(dic.getEntry("lighthouse"), Choice.IMPLICIT);	// Tworzymy nmowy obiekt 'lista wyboru'
		Image ok = ImageLoader.load("/images/choice.png");					// £adujemy obrazek z pliku - haczyk OK.
		Image no = ImageLoader.load("/images/brak.png");					// £adujemy obrazek z pliku - krzyzyk NO.

		for(int i = 0; i < manager.size; i++) {
			// Przelatujemy wszystkie paragony.
			int k = manager.podium[i];				// Index pobrany z podium.
			String s = manager.paragons[k].name;	// Pobieramy nazwê latarni
			int match = manager.paragons[k].match;	// Pobieramy zgodnoœæ wzglêdem zarejestrowanej próbki
			if(match / 10 > 89) {					// Jeœli prawdopodobieñstwo okreslenia próbki 
				ekran.append(s, ok);				// (zgodnoœæ) jest wiêksza ni¿ 90% to OK
			} else {					// Jeœli natrafiliœmy na mniejsz¹ zgodnoœæ ni¿
				ekran.append(s, no);	// 90% to dodajemy t¹ pozycjê i
				break;					// przerywamy bo ni¿ej nie ma i tak co szukaæ.	
			}
		}

		ekran.addCommand(back);				// Dodaje polecenie 'Wróæ' do ekranu.
		ekran.setCommandListener(ucho);		// Ustawia obiekt 'ucho' jako nas³uch zdarzeñ ekranu.
		
		if(manager.toShortError) {
			// Jesli wyst¹pi b³¹d to:
			
			String floc = "/message/sh_" + DefaultLang + ".txt";
			final Displayable ek = ekran;
			StringPainter strp = new StringPainter(floc, dic) {
				public void back() {
					display.setCurrent(ek);
				}
			};
			strp.align = Graphics.HCENTER;
			strp.valign = Graphics.TOP;
			display.setCurrent(strp);
			AlertType.ERROR.playSound(display);
		
		} else if(manager.maxProbe < 900) {
			
			String floc = "/message/er_" + DefaultLang + ".txt";
			final Displayable ek = ekran;
			StringPainter strp = new StringPainter(floc, dic) {
				public void back() {
					display.setCurrent(ek);
				}
			};
			strp.align = Graphics.HCENTER;
			strp.valign = Graphics.TOP;
			display.setCurrent(strp);
			AlertType.ERROR.playSound(display);
		
		} else {
			display.setCurrent(ekran);			// Ustawia aktualny ekran jako 'ekran'.
		}
	}

	/** Wyœwietlamy wszystkie dostêpne paragony (wzorce œwiate³). */
	public void showAllParagons() {

		List ekran = new List(dic.getEntry("lighthouse"), Choice.IMPLICIT);	// Tworzymy nowy obiekt 'lista wyboru'
		Image latarnia = ImageLoader.load(this.IMAGE_ICON);					// £adujemy obrazek z pliku - obrazek do latarni.
		
		for(int i = 0; i < manager.size; i++) {		// Przelatujemy ca³¹ tablicê paragonów
			String s = manager.paragons[i].name;	// i pobieramy nazwê ka¿dego (nazwa miejscowoœci)
			ekran.append(s, latarnia);				// a nastêpnie wrzucamy j¹ na utworzon¹ wczeœniej listê
		}

		// Nas³uchiwacz zdarzeñ dla menu wyboru paragonu - UCHO dray.
		CommandListener ucho = new CommandListener() {				// Obs³uga zdarzeñ dla wytworzonej przez nas listy.
			public void commandAction(Command c, Displayable d) {	// Pokrywamy abstrakcjê.

				if(c == List.SELECT_COMMAND) {			// Jeœli naciœnieæie klawisza by³o wybraniem pozycji listy
					Choice wybor = (Choice)d;			// to rzutujemy pobrany obiekt aktualnie wyœwietlony na obiekt
					int w = wybor.getSelectedIndex();	// klasy 'Choice' i poprzez jego metody pobieramy indeks zaznaczonego
														// na liœcie elementu.

					ParagonItem pi = manager.paragons[w];					// Pobieramy wybran¹ pzrzez u¿ytkownika lokalizacjê.
					LightSampleInfo inf = new LightSampleInfo(pi, dic) {	// Tworzymy obiekt ywœwietlacza pomoacy.
						public void back() {								// Nadpisujemy abstrakcjê back() wiêc gdy user kliknie
							display.setCurrent(przedostatni);				// w jakiœ klawisz to wyœwietli siê przedostatni ekran.
						}
					};
					przedostatni = display.getCurrent();	// Przedostatni zostaje zapisany jako aktualny.
					display.setCurrent((Canvas)inf);				// Wyœwietlamy nastêpny ekran - info o lokalizacji.
				}
				if(c == back) {						// Jeœli wybranym poleceniem by³o 'Wstecz' to
					display.setCurrent(ostatni);	// wyœwietlamy obiekt który poprzednio goœci³ na
				}									// naszym LCD wyœwietlaczu komórki.
			}
		};
		ekran.addCommand(back);				// Dodaje polecenie 'Wróæ' do ekranu.
		ekran.setCommandListener(ucho);		// Ustawia obiekt 'ucho' jako nas³uch zdarzeñ ekranu.
		display.setCurrent(ekran);			// Ustawia aktualny ekran jako 'ekran'.
	}
	
	/** Start aplikacji. */
	public void startApp() throws MIDletStateChangeException {		

		this.ani.setSpeed(1500);		// Ustawiamy prêdkoœæ animacji.
		this.ani.setLoopBack(1);		// Ustawiamy iloœæ powtórzeñ animacji
		this.ani.start();				// i na koñcu uruchamiamy animacjê.
		
		// Gdy dzia³a w¹tek animacyjny mo¿emy zaj¹æ siê ³adowaniem potrzebnych klas
		// oraz zasobów umieszczonych w plikach.
		
		// -------- £ADOWANE W CZASIE TRWANIE W¥TKU ANIMACYJNEGO -------- 
		
		this.setConstConfiguration();	// Ustawiamy sta³e programowe z zapisanej konfiguracji.
		this.createCommands();			// Tworzymy tablicê poleceñ dla ekranu g³ównego. Bêdzie ona
										// uzywana jako podstawowe menu nawigacyjne. Po ka¿dorazowej 
												// zmianie ustawieñ trzeba wywo³aæ od pocz¹tku tworzenie komend.
		this.ekranGlowny = formEkranGlowny();	// Tworzymy zawartoœæ ekranu g³ównego,

		// ---- MANAGER PRÓBEK ----
		this.manager = new LightSampleManager(	// Tworzymy nowego menad¿era próbek œwietlnych
			this.dic,
			this.display
		) {
			public void abort() { }
			public void found() {	// Pokrywamy abstrakcyjn¹ metode wywo³ywan¹ gdy wszystkie paragony zosta³y skorelowane.
				this.sort();		// Sortuje wyniki, tak aby wybraæ pierwsze 3 najwy¿sze.
				showResult();		// Rezultat dopasowywania wywalamy na ekran (w funkcji).
			}
		};
		this.manager.loadParagonsFromFile(this.PARAGONS_FILE);	// i ³adujemy do niego wzorce z pliku z paragonami.

		// ---- KOLEKCJONER PRÓBEK ----
		this.sampc = new LightSampleCollector(	// Tworzymy obiekt kolektora próbek (zbieracz)
			this.dic,							// Przekazujemy s³ownik.
			this.SAMPLING_SPEED					// prêdkoœæ zbierania próbek (w ms)
		) {
			public void abort() {								// Pokrywamy abstrakcjê abort()
				display.setCurrent(manager);
				final String ss = new String(this.getSample());	// przez co gdy pomiar zostaje zatrzymany
				manager.setSample(ss);
				manager.findMatches();						// to manager próbek dopasowuje uzyskan¹ próbkê do wzorca findMatches()
				this.reset();						// resetujemy zbieracza (niszczy stare próbki)
			}
			public void loopback() {
				display.setCurrent(ostatni);
			}
		};

		// ---- POMOC DLA USERA ----
		this.helper = new StringPainter("/help/" + DefaultLang + ".txt", dic) {
			public void back() {
				display.setCurrent(ostatni);
			}
		};
	}
	
	/** Pauza aplikacji. */
	public void pauseApp() {
	}

	/** Wyjœcie z aplikacji. */
	public void exitApp() {
		try {						// Proba zniszczenia aplikacji.
			this.destroyApp(true);	// (generujac wyjatek StateChEx - ponizej w catch)
			this.notifyDestroyed();	// zg³asza zniszczenie.
		}
		catch(MIDletStateChangeException e) { 	// Przechwytywanie wyj¹tku. 
		}										// Nie rób nic.
	}
	
	/** Zatrzymanie i zniszczenie aplikacji. */
	public void destroyApp(boolean uncondinental) throws MIDletStateChangeException {
		if(display != null) display = null;	// Usuwamy referencje do ekranu.
		if(ostatni != null) ostatni = null;	// Usuwamy referencje do obszaru wyswietlanego.
		if(przedostatni != null) przedostatni = null;
		if(manager != null) manager = null;
		
		System.gc();	// Sprzatamy smieci.
	}	
}