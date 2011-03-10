package sarxos.lights;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import sarxos.lights.ParagonItem;
import sarxos.lights.LightSample;
import sarxos.lights.LightSampleToShortException;

import sarxos.parser.Splitter;
import sarxos.parser.FileReader;
import sarxos.parser.Dictionary;

import java.util.Timer;
import java.util.TimerTask;

/** Klasa nawigacji i analizy próbek charakterystyk œwietlnych. */
public abstract class LightSampleManager extends Canvas {

	public LightSample ls = null;		// Próbka œwietlna = null.
	public ParagonItem[] paragons = null;
	public boolean toShortError = false;
	public int[] podium = null;
	public int probe = 0;				// Dopasowanie = 0.
	public int maxProbe = 0;			// Maksymalone dopasowanie = 0;
	public int maxProbePosition = 0;	// Pozycja max dopasowania = 0;
	public int size = 0;

	private int x = 0;
	private int xp = 0;
	private int y = 0;
	private int yp = 0;
	private int szer = 0;
	private int scrollWidth = 80;

	private FileReader fr = new FileReader();
	private Dictionary dic = null;
	private TimerTask tt = null;
	private Timer tim = null;
	private Display display = null;
	private String ss = null;
	private String s = null;

	/** Konstruktor g³ówny. */
	public LightSampleManager(Dictionary dic, Display display) {
		this.dic = dic;
		this.display = display;
	}
	
	/** Odczytuje wzorce (paragony) z pliku tekstowego. */
	public void loadParagonsFromFile(String filePath) {
		
		String plik = fr.getStringFromFile(filePath);			// Pobieranie Stringu z pliku txt
		String[] podzielone = Splitter.split(plik, "\n");		// Splitujemy go za pomoc¹ delimitera nowej lini \n zapisuj¹c do tablicy
		String[][] wzorce = new String[podzielone.length][7];	// Tworzymy macierz wzorców.
		
		for(int i = 0; i < podzielone.length; i++) {			// Przelatujemy przez ka¿dy wiersz macierzy
			wzorce[i] = Splitter.split(podzielone[i], ";");		// i zapisujemy w nim jako kolumny splitowane 
		}														// wartoœci z ka¿dej linii pliku txt
		
		this.paragons = new ParagonItem[podzielone.length];		// Tworzymy now¹ tablicê paragonów.
		this.size = podzielone.length;							// Ustalamy wielkoœæ managera (ilosæ paragonów).
		
		for(int i = 0; i < this.size; i++) {					// Zapisujemy wartoœæ do ka¿dego z paragonów
			this.paragons[i] = new ParagonItem();				// (tworzymy paragony na podstawie wartoœci pobranych
			this.paragons[i].name = wzorce[i][0];				// z pliku paragonów - na samym pocz¹tku)
			this.paragons[i].location = wzorce[i][1];
			this.paragons[i].setParagon(wzorce[i][2]);
			this.paragons[i].light = wzorce[i][3];
			this.paragons[i].description = wzorce[i][6];
			this.paragons[i].meters = Integer.parseInt(wzorce[i][4]);
			this.paragons[i].miles = Integer.parseInt(wzorce[i][5]);
		}
		
		this.szer = (80 * 1000) / this.size;
		this.x = -this.szer;
		this.podium = new int[this.size];	// Tablica przechowuj¹ca wyniki porównania paragonów
											// z badan¹ próbk¹.
	}

	protected void paint(Graphics g) {
		//synchronized(this) {
			int width = getWidth();
			int height = getHeight();
			xp = (width / 2) - (scrollWidth / 2);
			yp = 30;
			y = 10;
			//try {
				g.setColor(0xffffff);
				g.fillRect(0, 0, width, height);
				g.setColor(0x000000);
				
				g.drawRect(xp, yp, scrollWidth, y);
				g.fillRect(xp, yp, (int)(x / 1000), y);
				
				String an = dic.getEntry("analysis");
				if(s == null) s = new String("");
				g.drawString(an, width/2, 50, Graphics.HCENTER | Graphics.TOP);
				g.drawString(s, width/2, 60, Graphics.HCENTER | Graphics.TOP);
			//}
			//catch(Exception e) {
			//	System.out.println("Znalazlem b³¹d. Exception.");
			//}
		//}
	}

	/** Ustawianie wartoœci próbki. */
	public void setSample(String sample) {
		this.ss = sample;
	}
	
	/** Odnajdywanie po³o¿enia na podstawie dostarczonej próbki. */
	public void findMatches() {
		this.toShortError = false;
		this.tim = new Timer();		
		this.tt = new TimerTask() {
			public int i = 0;
			public void run() {
		//		synchronized(this) {
					probe = 0;
					ls = null;
					ls = new LightSample(paragons[i].paragon, ss);
					try {
						probe = ls.match();
					}
					catch(LightSampleToShortException e) { 	
						toShortError = true;
					}	
					if(probe > maxProbe) {
						maxProbe = probe;
						maxProbePosition = i;
					}
					paragons[i].match = probe;
					podium[i] = i;
					i++;
					if(i < size) {
						x += szer;
						s = paragons[i].name;
						try {
							repaint();
						} 
						catch(NullPointerException e) { 
							System.err.println(e);
						}
					} else {
						tim.cancel();
						tim = null;
						tt = null;
						setDefault();
						found();
					}
		//		} // Koniec synchronizacji.
			}
		};
		this.tim.schedule(this.tt, 0, 10);
	}
	
	/** Sortowanie (bombelkowe) wyników porównywania próbki z paragonami. */
	public void sort() {
		int a = 0;
		int b = 0;
		int t = 0;	
		for(int i = 0; i < this.paragons.length; i++) {
			for(int k = 0; k < this.paragons.length - 1; k++) {
				a = this.paragons[this.podium[k]].match;
				b = this.paragons[this.podium[k + 1]].match;
				if(a < b) {
					t = this.podium[k + 1];
					this.podium[k + 1] = this.podium[k];
					this.podium[k] = t;
				}
			}
		}
	}

	/** Ustawianie domyslnych wartoœci zmiennych. */
	public void setDefault() {
		this.szer = (80 * 1000) / this.size;
		this.x = -this.szer;
		this.xp = 0;
		this.yp = 30;
		this.y = 10;
		this.ss = null;
		if(tt != null) {
			this.tt = null;
		}
		if(tim != null) {
			this.tim.cancel();
			this.tim = null;
		}
	}
	
	/** Koniec dopasowywania. */
	public void matchEnd() {
		this.setDefault();
		this.found();
	}
	
	/** Wciœniety powrót. */
	public void goBack() {
		this.setDefault();
		this.abort();
	}

	/** Gdy wyszukiwanie zostaje przerwane. */
	public abstract void abort();
	
	/** Gdy wyszukiwanie zosta³o zakoñczone. */
	public abstract void found();

//	/** Drukuje wzorce (paragony) do konsoli. */
//	public void printParagons() {
//		for(int i = 0; i < this.size; i++) {
//			System.out.println((i + 1) + "/" + this.size + "\n" +
//				"    " + (this.paragons[i].name) + "\n" +
//				"    " + (this.paragons[i].location) + "\n" +
//				"    " + (this.paragons[i].paragon) + "\n" +
//				"    " + (this.paragons[i].light) + "\n" +
//				"    " + (this.paragons[i].meters) + "\n" +
//				"    " + (this.paragons[i].miles) + "\n" +
//				"    " + (this.paragons[i].description)
//			);
//		}
//	}
	
//	/** Drukuje dopasowania do konsoli. */	
//	public void printMatches(int k) {
//		for(int i = 0; i < k; i++) {
//			if(podium[i] != - 1) {
//				System.out.println(
//					"----------\n" + paragons[podium[i]].name + "\n" +
//					"                " + paragons[podium[i]].match + "%\n" +
//					"----------"
//				);
//			}
//		}
//	}


}