package sarxos.lights;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import java.util.Timer;
import java.util.TimerTask;

import sarxos.graphics.ImageLoader;
import sarxos.parser.Dictionary;

/** Klasa zbieracza pr�bek. */
public abstract class LightSampleCollector extends Canvas implements CommandListener {

	private int x = 2;	// pozycja lini licz�c od xp
	private int xp = 0;	// Poczatkowa pozycja linii
	private int y = 23;	// Poczatkowa pozycja Y.
		
	private String str = new String("");		// Widmo wyj�ciowe.
	private String act = new String("1");	// Aktualna pr�bka.
	public int speed = 200;					// Pr�dko�c od�wierzania.
	private boolean enable = false;			// Czy zliczanie jest w��czone.
		
	private Timer t = new Timer();				// Zegar licz�cy.
	private TimerTask tt = null;

	public String title = null;		// Tytu� wy�wietlany na ekranie.
	public String back = null;		// Napis przy przycisku do zbierania.
	public String analysis = null;	// Napis przy przycisku analizy.
	public String press = null;		// Info aby ludek wcisn�� klawisz.

	private Command analiza = null;
	private Command wstecz = null;
	
	private Font myFont = Font.getFont(	// Tworzymy w�asna czcionk�:
		Font.FACE_PROPORTIONAL, 		// proporcjonalna,
		Font.STYLE_PLAIN, 				// zwyk�y styl (normal lub plain)
		Font.SIZE_SMALL					// ma�a
	);


	/** Konstruktor domy�lny. Wymaga trzech parametr�w. */
	public LightSampleCollector(Dictionary dic, int speed) {
		this.title = dic.getEntry("collecting");
		this.back = dic.getEntry("back");
		this.analysis = dic.getEntry("analysis");
		this.press = dic.getEntry("press");
		this.speed = speed;
		
		this.analiza = new Command(this.analysis, Command.OK, 1);
		this.addCommand(this.analiza);
		this.wstecz = new Command(this.back, Command.BACK, 1);
		this.addCommand(this.wstecz);
		
		this.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == this.analiza) {
			this.stop();
		}
		if(c == this.wstecz) {
			this.reset();
			this.loopback();
		}
	}

	/** Pokryta abstrakcyjbna metoda paint() z klasy Canvas. */
	protected void paint(Graphics g) {
		
		int width = getWidth();		// Pobiera szeroko�� ekranu.
		int height = getHeight();	// Pobiera wysoko�� ekranu.
		
		this.xp = (width / 2) - 41;	// Poczatkowa pozycja linii.
		
		g.setColor(0xffffff);					// Ustawiamy kolor na czarny.
		g.fillRect(0, 0, width, height);		// Wype�niamy ekran czerni�.
		
		g.setColor(0x000000);					// ustaw kolor bia�y,
		g.setFont(this.myFont);					// ustaw domy�ln� czcionk� i

		// Rysujemy t�o - czyli bardzo �adny rysuneczek.
		g.drawImage(
			ImageLoader.load("/images/tlo.png"), 
			width/2, height/2, 
			Graphics.HCENTER | Graphics.VCENTER
		);
			
		g.drawString(this.title, width/2, 0, Graphics.HCENTER | Graphics.TOP);			// Napis g��wny
		g.drawString(this.press, width/2, 10, Graphics.HCENTER | Graphics.TOP);			// Napis g��wny
			
		synchronized(this) {						// Ten blok musi by� zsynchronizowany aby nie kolidowa� 
			if(xp + x + 1 >= (width / 2) + 40) {	// z w�tkiem timera kt�ry wywo�uje przemalowywanie.
				x = 0;
				y += 7;
			}
			
			int a = 0;
			int b = 0;
			if(act.indexOf("1") != -1) {
				a = 0;	// Offset
				b = 3;	// Wysoko�� piku
			} else {
				a = 3;	// Offset
				b = 0;	// Wysoko�� piku.
			}
			if(this.enable) {
				g.setColor(0xaa0000);
				g.drawRect(xp + x, y + a, 1, b);
				g.setColor(0x000000);
				x++;
			}
		} 
		// Koniec bloku zsynchronizowanego
		// Gdy ko�cz� si� te instrukcje - ko�czy si� malowanie. Od tego momentu metoda
		// repaint() mo�e by� wywo�ana ponownie.
	}

	/** Metoda wywo�ywana gdy klawisz naci�ni�ty. */
	protected void keyPressed(int keyCode) {
		this.act = "1";
		if(!this.enable) this.start();
	}
		
	/** Metoda wywo�ywana gdy klawisz puszczony. */
	protected void keyReleased(int keyCode) {
		this.act = "0";
	}
	
	/** Wl�czanie zbierania pr�bek. */
	public void start() {
		this.tt = new TimerTask() {	// Zadanie zegara.
			public void run() {						// Pokrywamy abstrakcyjn� klas� run()
				synchronized(this) {				// w�tek ten musi zosta� zsynchronizowany, aby
					str += act;									// operacja przemalowania nie kolidowa�a
					repaint(xp + x + 1, y - 3, xp + x + 2, y + 3);	// z operacj� przeliczania pozycji w klatkach.
				}												// Odmalowywanie jest wywo�ywane na obszarze.
			}
		};
		
		this.t.scheduleAtFixedRate(tt, 0, this.speed);
		this.enable = true;
	}

	/** Zatrzymywanie zbierania pr�bek. */
	public void stop() {
		this.t.cancel();
		this.abort();
	}

	/** Resetowanie zbieracza pr�bek. */
	public void reset() {
		this.t.cancel();
		this.t = null;
		this.t = new Timer();
		this.enable = false;
		this.str = new String("");
		this.act = new String("1");
		this.x = 2;
		this.xp = 0;
		this.y = 23;
	}

	/** Zwracanie pr�bki przechwyconej z urz�dzenia. */
	public String getSample() {
		return this.str;
	}

	/** Abstrakcyjna metoda wywo�ywana gdy wyst�puje przerwanie pomiaru. */
	public abstract void abort();
	
	/** Abstrakcyjna metoda wywo�ywana gdy klikniemy wstecz. */
	public abstract void loopback();
}
