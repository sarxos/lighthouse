package sarxos.graphics;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import sarxos.parser.FileReader;
import sarxos.parser.Splitter;
import sarxos.parser.Dictionary;

public abstract class StringPainter extends Canvas implements CommandListener {

	private Font myFont = Font.getFont(	// Tworzymy w³asna czcionkê:
		Font.FACE_PROPORTIONAL, 		// proporcjonalna,
		Font.STYLE_PLAIN, 				// zwyk³y styl (normal lub plain)
		Font.SIZE_SMALL					// ma³a
	);

	public int align = Graphics.LEFT;
	public int valign = Graphics.TOP;

	private FileReader fr = new FileReader();
	private String str[] = null;
	
	private Command wstecz = null;
	
	/** G³ówny konstruktor. */
	public StringPainter(String filePath, Dictionary dic) {
		
		this.str = Splitter.split(this.fr.getStringFromFile(filePath), "\n");	// Dzielimy na linie.

		String[] u = {"\n", "\r", "\t", "\b"};				// Zabronione znaki.
		for(int i = 0; i < this.str.length; i++) {			// Indeksuje Stringi
			for(int j = 0; j < u.length; j++) {				// Przeszukujemy Stringi w poszukiwaniu
				if(this.str[i].indexOf(u[j]) != -1) {									// zabronionych znaków z tablicy 'u'.
					this.str[i] = this.str[i].substring(0, this.str[i].indexOf(u[j]));	// Gdy któryœ znajdzie, to po prostu obcina Stringa 
				}																		// do miejsca gdzie ten znak zabroniony wyst¹pi³
			}
		}
		
		this.wstecz = new Command(dic.getEntry("back"), Command.BACK, 1);
		this.addCommand(this.wstecz);
		this.setCommandListener(this);
	}
	
	public void commandAction(Command c, Displayable d) {
		if(c == this.wstecz) {
			this.back();
		}
	}
	
	protected void paint(Graphics g) {

		int width = getWidth();		// Pobiera szerokoœæ ekranu.
		int height = getHeight();	// Pobiera wysokoœæ ekranu.

		g.setColor(0xffffff);
		g.fillRect(0, 0, width, height);
		g.setFont(this.myFont);
		g.setColor(0x000000);
		
		int y = 2;
		int x = 2;
		switch(align) {
			case Graphics.HCENTER:
				x = width/2;
				break;
			case Graphics.RIGHT:
				x = width;
				break;
		}
		for(int i = 0; i < this.str.length; i++) {
			g.drawString(this.str[i], x, y, align | valign);
			y += 10;
		}
	}
	
	/** Gdy naciœniêcie klawisza. */
	protected void keyPressed(int keyCode) {
		this.back();
	}
	
	/** Gdy puszczenie klawisza. */
	protected void keyReleased(int keyCode) {
		this.back();
	}
	
	/** Abstrakcyjna metoda wywo³ywana na naciœniêcie klawisza. */
	public abstract void back();
}