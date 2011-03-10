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

	// To trzeba �adowa� p�xniej z RMS - teraz s� to ustawienia domy�lne - czyli DEFAULT
	 
	public final String APP_TITLE 			= this.getAppProperty("MIDDlet-name");			// Nazwa aplikacji.
	public final String RMS_CONFIG			= this.getAppProperty("SarXos-ConfigName");		// Nazwa konfiguracyjnego pakietu RMS.
	public final String IMAGE_LIGHTHOUSE 	= new String("/images/tlo.png");				// Rysunek do menu g��wnego.
	public final String IMAGE_ICON 			= new String("/images/ikonka.png");								// Ikonka wy�wietlana przy danej pozycji paragonu.
	public final int SAMPLING_SPEED 		= Integer.parseInt(this.getAppProperty("SarXos-SampSpeed"));	// Pr�dko�� zbierania pr�bek (co 200ms).
	public final String PARAGONS_FILE 		= new String("/paragons/paragons.txt");							// Plik z paragonami.
	public String DefaultLang 				= this.getAppProperty("SarXos-DefLanguage");	// J�zyk domy�lny (ustawiamy w MIDlet properties KToolbar).

	// Wy�wietlacz (obiekt-uchwyt do ekranu).
	public Display display = Display.getDisplay(this);	// Pobieramy uchwyt ekranu.
	// Ekran g��wny (obiekt wy�wietlany).
	public Frame ekranGlowny = null;		// Ekran g��wny - z niego przeprowadza�
	// Wracacz.
	public Displayable ostatni = null;		// Obiekt w kt�rym b�dziemy przechowywa� ostatnio widoczny ekran.
	public Displayable przedostatni = null;	// Obiekt w kt�rym b�dziemy przechowywa� przedostatnio widoczny ekran.
	public FileReader freader = new FileReader();
	// S�ownik.
	public Dictionary dic = new Dictionary(	// Tworzymy s�ownik wyraz�w (otwieramy po 
		"/lang/" + DefaultLang + ".txt"		// prostu z pliku domy�lnego okre�lonegoprzez j�zyk domy�lny  
	);										// zapisany w RMS i rozszerzenie txt.

	// Kolekcjoner pr�bek �wietlnych.
	public LightSampleCollector sampc = null;
	// Analizator pr�bek �wietlnych.
	public LightSampleManager manager = null;
	// Wy�wietlacz pomocy z plik�w.
	public StringPainter helper = null;

	// Ramki animacji.												
	public Frame[] ramki = {					// Tworzymy ramki animacji podstawowej.	
		new Frame("/images/powitanie_1.png"),	// SarXos Design ...
		new Frame("/images/powitanie_2.png"),	// Latarnia, the Lighthosuse ...
		new Frame("/images/blank.png")			// (pusto��) ... (ostatnia ramka i tak znika)
	};

	// Animacja wej�ciowa.
	public Animation ani = new Animation(display, ramki) {	// Utworzone ramki animacji przekazujemy 
															// do konstruktora i pokrywamy metod� abort() animacji.
		public void abort() {					// Pokrywamy abstrakcyjn� metod� abort() - ten kod zostanie
			ani = null;							// wykonany gdy wyst�pi warunek zatrzymania animacji.
			ramki = null;						// Usuwamy animacj� i jej ramki - niech nie za�miecaj� pami�ci.
			display.setCurrent(ekranGlowny);	// a nast�pnie go wy�wietlamy.
		}
	};

	// Tablica polece� g��wnego ekranu.
	public Command[] cmds = new Command[5];

	// Jedno polecenie osobne - WSTECZ.
	public Command back = null;

	/** Tworzenie polece� podstawowych. */
	public void createCommands() {
	
		// Tworzymy polecenia g��wnego ekranu.
		this.cmds[0] = new Command(dic.getEntry("exit"), Command.EXIT, 0);		// Polecenie 'Wyj�cie'.
		this.cmds[1] = new Command(dic.getEntry("meas"), Command.SCREEN, 1);	// Polecenie 'Pomiar'.
		this.cmds[2] = new Command(dic.getEntry("lang"), Command.SCREEN, 1);	// Polecenie 'J�zyk'.
		this.cmds[3] = new Command(dic.getEntry("help"), Command.SCREEN, 1);	// Polecenie 'Pomoc'.
		this.cmds[4] = new Command(dic.getEntry("data"), Command.SCREEN, 1);	// Polecenie 'Wzorce'.
		// Tworzymy obiekt polecenia wstecz.
		this.back = new Command(dic.getEntry("back"), Command.BACK, 0);
	}

	/** CommandAction - pokrycie metody abstrakcyjnej z CommandListener'a - pods�uch zdarze�. */
	public void commandAction(Command c, Displayable d) {
		
		int cmdNum = -1;						// Poczatkowy numer polecenia = -1 (nie ma takiego, czyli
												// �adne nie zostanie wybrane przez przypadek).
		int i = 0;								// Zmienna 'i' na pocz�tku zero.
		int k = 0;								// Zmienna 'k' na pocz�tku zero.

		for(k = 0; k < this.cmds.length; k++) {	// Por�wnujemy po kolei ka�d� komend� z
			if(c == this.cmds[k]) cmdNum = i;	// tabl;icy komend, z t� kt�ra zosta�a przekazana 
			i++;								// jako argument zdarzenia i ustawiamy numer.
		}

		System.gc();			// Sprzatamy �mieci.	

		if(cmdNum >= 0) {		// Jesli polecenie zostalo wybrane (jego numer > 0).
			switch(cmdNum) {	// Mamy dost�pne pozycje dla odpowiednich numer�w polece�.
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
					// Wybrano J�ZYK
					this.ostatni = this.display.getCurrent();	// Pobiera ostatni ekran
					this.menuLanguage();						// i w��cza menu wyboru jezyka.
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
					// Wybrano WR��.
					this.display.setCurrent(this.ostatni);
					break;
			}
		}
	}

	/** Tworzymy g��wny ekran aplikacji. */
	public Frame formEkranGlowny() {

		Frame ekran = new Frame(this.IMAGE_LIGHTHOUSE);	// Tworzymy nowy obiekt wy�wietlany - form� o nazwie
		for(int i = 0; i < this.cmds.length; i++) {		// Przechodzimy tablic� polece� i ka�de z nich
			ekran.addCommand(this.cmds[i]);				// dodajemy do utworzonej formy 'ekran'.
		}
		ekran.setCommandListener(this);		// Ustawiamy jako obiekt nas�uchuj�cy zdarzenia polece�
		return ekran;	
	}

	/** Tworzymy menu wyboru j�zyka. */
	public void menuLanguage() {
		
		List ekran = new List("Menu", Choice.IMPLICIT);			// Tworzymy nmowy obiekt 'lista wyboru' (radio)
		
		Image znaczek = ImageLoader.load("/images/choice.png");	// Tworzymy obrazek (haczyk oznaczaj�cy w�aczony j�zyk)
		Image krzyzyk = ImageLoader.load("/images/brak.png");	// Tworzymy obrazek (krzy�yk oznaczaj�cy wy�aczony j�zyk)
			
		if(this.DefaultLang.indexOf("pl") != -1) {	// Sprawdzamy czy w domy�lnie ustawionym aktualnie j�zyku wyst�puje 'pl'
			ekran.append("Polski", znaczek);		// i jesli tak to dodajemy j�zyk polski zer znaczkiem 'ok'
		} else {									// lecz je�li nie
			ekran.append("Polski", krzyzyk);		// to dodajemy j�zyk polski ale ze znaczniem 'krzy�yk'.
		}
		if(this.DefaultLang.indexOf("en") != -1) {	// Dodajemy j�zyk angielski.
			ekran.append("English", znaczek);	
		} else {
			ekran.append("English", krzyzyk);
		}
		if(this.DefaultLang.indexOf("de") != -1) {	// Dodajemy j�zyk niemiecki.
			ekran.append("Deutsche", znaczek);	
		} else {
			ekran.append("Deutsche", krzyzyk);
		}

		// Nas�uchiwacz zdarze� dla menu J�zyka - UCHO ajnc.
		CommandListener ucho = new CommandListener() {				// Obs�uga zdarze� dla wytworzonej przez nas listy.
			public void commandAction(Command c, Displayable d) {	// Pokrywamy abstrakcj�.
				
				if(c == List.SELECT_COMMAND) {			// Je�li naci�nie�ie klawisza by�o wybraniem pozycji listy
					Choice wybor = (Choice)d;			// to rzutujemy pobrany obiekt aktualnie wy�wietlony na obiekt
					int w = wybor.getSelectedIndex();	// klasy 'Choice' i poprzez jego metody pobieramy indeks zaznaczonego
														// na li�cie elementu.
					switch(w) {
						case 0:
							DefaultLang = "pl";			// Wybrano j�zyk polski
							break;
						case 1:
							DefaultLang = "en";			// Wybrano j�zyk angielski.
							break;
						case 2:
							DefaultLang = "de";			// Wybrano j�zyk niemiecki.
							break;
					}
					
					Store config = new Store(RMS_CONFIG);	// Otwietramy pakiet z konfiguracj� MIDletu.
					config.setRecord(1, DefaultLang);		// Uaktualniamy pole z j�zykiem
					config.close();							// a nast�pnie zamykamy pakiet.
					config = null;							// Usuwamy referencj�.
					
					dic = new Dictionary(					// Tworzymy nowy s�ownik z odpowiednim zestawem s��w.
						"/lang/" + DefaultLang + ".txt"		// S�owa
					);
					
					// Odnawianie j�zyka.
					sampc.title = dic.getEntry("collecting");	// Tytu� widoczny w canvasie,
					sampc.back = dic.getEntry("back");			// nazwa przycisku 'zbieraj'
					sampc.analysis = dic.getEntry("analysis");	// nazwa przycisku 'analizuj'
					sampc.press = dic.getEntry("press");		// info o nacisnieciu klawisza
					
					helper = new StringPainter("/help/" + DefaultLang + ".txt", dic) {	// Tworzymy nowego helpera.
						public void back() {										// Analogicznie jak w starym nadpisujemy abstrakcj�.
							display.setCurrent(ostatni);
						}	
					};

					createCommands();					// Tworzymy polecenia ze zmienionym j�zykiem.
					ekranGlowny = formEkranGlowny();	// Tworzymy zawarto�� ekranu g��wnego (ze zmienionym j�zykiem),
					display.setCurrent(ekranGlowny);	// a nast�pnie go wy�wietlamy.
				}
				if(c == back) {						// Je�li wybranym poleceniem by�o 'Wstecz' to
					display.setCurrent(ostatni);	// wy�wietlamy obiekt kt�ry poprzednio go�ci� na
				}									// naszym LCD wy�wietlaczu kom�rki.
			}
		};
		ekran.addCommand(back);				// Dodaje polecenie 'Wr��' do ekranu.
		ekran.setCommandListener(ucho);		// Ustawia obiekt 'ucho' jako nas�uch zdarze� ekranu.
		display.setCurrent(ekran);			// Ustawia aktualny ekran jako 'ekran'.
	}
	
	/** �adujemy zapisan� w RMS konfiguracj�. */
	public void setConstConfiguration() {
		Store st = new Store(this.RMS_CONFIG);	// Otwieramy pakiet RMS o nazwie LatarniaConfig
		if(!st.isRecord(1)) {					// Sprawdzamy czy jest re4kord o identyfikatorze 1
			st.addRecord(this.DefaultLang);		// a je�li nie ma to zapisujemy go.
		} else {										// Lecz je�li jest ten rekord ustawiony to
			this.DefaultLang = st.getRecordString(1);	// czytamy z niego domyslny j�zyk (zapisany).
		}
		st.close();								// Zamykamy RecordStore.
		dic = new Dictionary(					// Uaktualniamy s�ownik
			"/lang/" + DefaultLang + ".txt"		// �aduj�c nowy plik ze s�owami.
		);
	}

	/** Wy�wietlamy rezultaty analizy pr�bek otrzymanych ze zbieracza pr�bek (sampc). */
	public void showResult() {

		// Nas�uchiwacz zdarze� dla menu wyboru lokalizacji - UCHO cwaj.
		CommandListener ucho = new CommandListener() {				// Obs�uga zdarze� dla wytworzonej przez nas listy.
			public void commandAction(Command c, Displayable d) {	// Pokrywamy abstrakcj�.

				if(c == List.SELECT_COMMAND) {			// Je�li naci�nie�ie klawisza by�o wybraniem pozycji listy
					Choice wybor = (Choice)d;			// to rzutujemy pobrany obiekt aktualnie wy�wietlony na obiekt
					int w = wybor.getSelectedIndex();	// klasy 'Choice' i poprzez jego metody pobieramy indeks zaznaczonego
														// na li�cie elementu.

					ParagonItem pi = manager.paragons[manager.podium[w]];	// Pobieramy wybran� pzrzez u�ytkownika lokalizacj�.
					LightSampleInfo inf = new LightSampleInfo(pi, dic) {	// Tworzymy obiekt yw�wietlacza pomoacy.
						public void back() {								// Nadpisujemy abstrakcj� back() wi�c gdy user kliknie
							display.setCurrent(przedostatni);				// w jaki� klawisz to wy�wietli si� przedostatni ekran.
						}
					};
					przedostatni = display.getCurrent();	// Przedostatni zostaje zapisany jako aktualny.
					display.setCurrent(inf);				// Wy�wietlamy nast�pny ekran - info o lokalizacji.
				}
				if(c == back) {						// Je�li wybranym poleceniem by�o 'Wstecz' to
					display.setCurrent(ostatni);	// wy�wietlamy obiekt kt�ry poprzednio go�ci� na
				}									// naszym LCD wy�wietlaczu kom�rki.
			}
		};

		List ekran = new List(dic.getEntry("lighthouse"), Choice.IMPLICIT);	// Tworzymy nmowy obiekt 'lista wyboru'
		Image ok = ImageLoader.load("/images/choice.png");					// �adujemy obrazek z pliku - haczyk OK.
		Image no = ImageLoader.load("/images/brak.png");					// �adujemy obrazek z pliku - krzyzyk NO.

		for(int i = 0; i < manager.size; i++) {
			// Przelatujemy wszystkie paragony.
			int k = manager.podium[i];				// Index pobrany z podium.
			String s = manager.paragons[k].name;	// Pobieramy nazw� latarni
			int match = manager.paragons[k].match;	// Pobieramy zgodno�� wzgl�dem zarejestrowanej pr�bki
			if(match / 10 > 89) {					// Je�li prawdopodobie�stwo okreslenia pr�bki 
				ekran.append(s, ok);				// (zgodno��) jest wi�ksza ni� 90% to OK
			} else {					// Je�li natrafili�my na mniejsz� zgodno�� ni�
				ekran.append(s, no);	// 90% to dodajemy t� pozycj� i
				break;					// przerywamy bo ni�ej nie ma i tak co szuka�.	
			}
		}

		ekran.addCommand(back);				// Dodaje polecenie 'Wr��' do ekranu.
		ekran.setCommandListener(ucho);		// Ustawia obiekt 'ucho' jako nas�uch zdarze� ekranu.
		
		if(manager.toShortError) {
			// Jesli wyst�pi b��d to:
			
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

	/** Wy�wietlamy wszystkie dost�pne paragony (wzorce �wiate�). */
	public void showAllParagons() {

		List ekran = new List(dic.getEntry("lighthouse"), Choice.IMPLICIT);	// Tworzymy nowy obiekt 'lista wyboru'
		Image latarnia = ImageLoader.load(this.IMAGE_ICON);					// �adujemy obrazek z pliku - obrazek do latarni.
		
		for(int i = 0; i < manager.size; i++) {		// Przelatujemy ca�� tablic� paragon�w
			String s = manager.paragons[i].name;	// i pobieramy nazw� ka�dego (nazwa miejscowo�ci)
			ekran.append(s, latarnia);				// a nast�pnie wrzucamy j� na utworzon� wcze�niej list�
		}

		// Nas�uchiwacz zdarze� dla menu wyboru paragonu - UCHO dray.
		CommandListener ucho = new CommandListener() {				// Obs�uga zdarze� dla wytworzonej przez nas listy.
			public void commandAction(Command c, Displayable d) {	// Pokrywamy abstrakcj�.

				if(c == List.SELECT_COMMAND) {			// Je�li naci�nie�ie klawisza by�o wybraniem pozycji listy
					Choice wybor = (Choice)d;			// to rzutujemy pobrany obiekt aktualnie wy�wietlony na obiekt
					int w = wybor.getSelectedIndex();	// klasy 'Choice' i poprzez jego metody pobieramy indeks zaznaczonego
														// na li�cie elementu.

					ParagonItem pi = manager.paragons[w];					// Pobieramy wybran� pzrzez u�ytkownika lokalizacj�.
					LightSampleInfo inf = new LightSampleInfo(pi, dic) {	// Tworzymy obiekt yw�wietlacza pomoacy.
						public void back() {								// Nadpisujemy abstrakcj� back() wi�c gdy user kliknie
							display.setCurrent(przedostatni);				// w jaki� klawisz to wy�wietli si� przedostatni ekran.
						}
					};
					przedostatni = display.getCurrent();	// Przedostatni zostaje zapisany jako aktualny.
					display.setCurrent((Canvas)inf);				// Wy�wietlamy nast�pny ekran - info o lokalizacji.
				}
				if(c == back) {						// Je�li wybranym poleceniem by�o 'Wstecz' to
					display.setCurrent(ostatni);	// wy�wietlamy obiekt kt�ry poprzednio go�ci� na
				}									// naszym LCD wy�wietlaczu kom�rki.
			}
		};
		ekran.addCommand(back);				// Dodaje polecenie 'Wr��' do ekranu.
		ekran.setCommandListener(ucho);		// Ustawia obiekt 'ucho' jako nas�uch zdarze� ekranu.
		display.setCurrent(ekran);			// Ustawia aktualny ekran jako 'ekran'.
	}
	
	/** Start aplikacji. */
	public void startApp() throws MIDletStateChangeException {		

		this.ani.setSpeed(1500);		// Ustawiamy pr�dko�� animacji.
		this.ani.setLoopBack(1);		// Ustawiamy ilo�� powt�rze� animacji
		this.ani.start();				// i na ko�cu uruchamiamy animacj�.
		
		// Gdy dzia�a w�tek animacyjny mo�emy zaj�� si� �adowaniem potrzebnych klas
		// oraz zasob�w umieszczonych w plikach.
		
		// -------- �ADOWANE W CZASIE TRWANIE W�TKU ANIMACYJNEGO -------- 
		
		this.setConstConfiguration();	// Ustawiamy sta�e programowe z zapisanej konfiguracji.
		this.createCommands();			// Tworzymy tablic� polece� dla ekranu g��wnego. B�dzie ona
										// uzywana jako podstawowe menu nawigacyjne. Po ka�dorazowej 
												// zmianie ustawie� trzeba wywo�a� od pocz�tku tworzenie komend.
		this.ekranGlowny = formEkranGlowny();	// Tworzymy zawarto�� ekranu g��wnego,

		// ---- MANAGER PR�BEK ----
		this.manager = new LightSampleManager(	// Tworzymy nowego menad�era pr�bek �wietlnych
			this.dic,
			this.display
		) {
			public void abort() { }
			public void found() {	// Pokrywamy abstrakcyjn� metode wywo�ywan� gdy wszystkie paragony zosta�y skorelowane.
				this.sort();		// Sortuje wyniki, tak aby wybra� pierwsze 3 najwy�sze.
				showResult();		// Rezultat dopasowywania wywalamy na ekran (w funkcji).
			}
		};
		this.manager.loadParagonsFromFile(this.PARAGONS_FILE);	// i �adujemy do niego wzorce z pliku z paragonami.

		// ---- KOLEKCJONER PR�BEK ----
		this.sampc = new LightSampleCollector(	// Tworzymy obiekt kolektora pr�bek (zbieracz)
			this.dic,							// Przekazujemy s�ownik.
			this.SAMPLING_SPEED					// pr�dko�� zbierania pr�bek (w ms)
		) {
			public void abort() {								// Pokrywamy abstrakcj� abort()
				display.setCurrent(manager);
				final String ss = new String(this.getSample());	// przez co gdy pomiar zostaje zatrzymany
				manager.setSample(ss);
				manager.findMatches();						// to manager pr�bek dopasowuje uzyskan� pr�bk� do wzorca findMatches()
				this.reset();						// resetujemy zbieracza (niszczy stare pr�bki)
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

	/** Wyj�cie z aplikacji. */
	public void exitApp() {
		try {						// Proba zniszczenia aplikacji.
			this.destroyApp(true);	// (generujac wyjatek StateChEx - ponizej w catch)
			this.notifyDestroyed();	// zg�asza zniszczenie.
		}
		catch(MIDletStateChangeException e) { 	// Przechwytywanie wyj�tku. 
		}										// Nie r�b nic.
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