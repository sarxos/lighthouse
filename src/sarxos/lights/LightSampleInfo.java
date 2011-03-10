package sarxos.lights;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

import sarxos.lights.ParagonItem;
import sarxos.parser.Dictionary;

public abstract class LightSampleInfo extends Canvas implements CommandListener {

	private ParagonItem paragon = null;	// Obiekt paragon, którego bedziemy wyœwietlaæ.
	private Font myFont = Font.getFont(	// Tworzymy w³asna czcionkê:
		Font.FACE_PROPORTIONAL, 		// proporcjonalna,
		Font.STYLE_PLAIN, 				// zwyk³y styl (normal lub plain)
		Font.SIZE_SMALL					// ma³a
	);
	private String[] opisy = new String[8];
	private Command wstecz = null;
	private byte odslonaTresci = 0;
	private int lastKey = 0;
	
	public LightSampleInfo(ParagonItem paragon, Dictionary dic) {
		this.paragon = paragon;
		this.opisy[0] = dic.getEntry("name"); 
		this.opisy[1] = dic.getEntry("attitude");
		this.opisy[2] = dic.getEntry("pattern");
		this.opisy[3] = dic.getEntry("scope");
		this.opisy[4] = dic.getEntry("height");
		this.opisy[5] = dic.getEntry("match");
		this.opisy[6] = dic.getEntry("none");
		this.opisy[7] = dic.getEntry("back");
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
		g.setColor(0x000077);
		
		int x1 = 2;
		
		switch(odslonaTresci) {
			case 0:		
				int x2 = 0;
				for(int i = 0; i < opisy.length; i++) {
					int ww = this.myFont.stringWidth(opisy[i]);
					if(x2 < ww) x2 = ww;
				}
				x2 += 9;
				// Nazwy poszczególnych pozycji.		
				g.drawString(opisy[0] + ":", x1, 2, Graphics.LEFT | Graphics.TOP);
				g.drawString(opisy[1] + ":", x1, 12, Graphics.LEFT | Graphics.TOP);
				g.drawString(opisy[2] + ":", x1, 22, Graphics.LEFT | Graphics.TOP);
				g.drawString(opisy[3] + ":", x1, 32, Graphics.LEFT | Graphics.TOP);
				g.drawString(opisy[4] + ":", x1, 42, Graphics.LEFT | Graphics.TOP);
				g.drawString(opisy[5] + ":", x1, 52, Graphics.LEFT | Graphics.TOP);
				// Dane dla ka¿dej pozycji.
				g.setColor(0x005500);
				g.drawString(paragon.name, x2, 2, Graphics.LEFT | Graphics.TOP);
				g.drawString(paragon.location, x2, 12, Graphics.LEFT | Graphics.TOP);
				g.drawString(paragon.light, x2, 22, Graphics.LEFT | Graphics.TOP);
				// Konwersja intów na Stringi
				String match = null;
				if(paragon.match <= 0) {
					match = opisy[6] + " ";
				} else {
					match = new String((paragon.match/10) + "." + (paragon.match - ((paragon.match / 10) * 10)));
				}
				//String meters = new String("" + paragon.meters);
				//String miles = new String("" + paragon.miles);
				g.drawString(paragon.miles + " Mm", x2, 32, Graphics.LEFT | Graphics.TOP);
				g.drawString(paragon.meters + " m", x2, 42, Graphics.LEFT | Graphics.TOP);
				g.drawString(match + "%", x2, 52, Graphics.LEFT | Graphics.TOP);
				// Drukowanie opisu w wielu liniach (jeœli nie mieœci siê w jednej).
				break;
			case 1:
				String tmp1 = new String(paragon.description);
				String tmp2 = null;
				int y2 = 2;
				int i = 0;
				do {					// Powtarzaj ...
					tmp2 = tmp1;		// Zapisuje nowy String
					for(i = tmp1.length() - 1; myFont.stringWidth(tmp2) > width - 4; i--) {	
														// Dopóki d³ugoœæ tego Stringu jest 
						tmp2 = tmp1.substring(0, i);	// wiêksza ni¿ szerokoœæ ekranu to odejmuje 
					}									// po literce.
					g.drawString(tmp2, x1, y2, Graphics.LEFT | Graphics.TOP);	// Rysujemy ten String
					tmp1 = tmp1.substring(i + 1, tmp1.length()).trim();			// Przekopiowujemy dalsza czêœæ Stringu
					y2 += 10;													// Dodajemy 10px do indexu lini.
				} while(myFont.stringWidth(tmp1) > width - 4);
				// Powtarza dopóki d³ugoœæ Stringu wynikowego jest wiêksza ni¿ ekran.
				if(tmp1.length() > 0) {								
					// Jesli w Stringu coœ jeszcze zosta³o to trza to narysowaæ.
					g.drawString(tmp1, x1, y2, Graphics.LEFT | Graphics.TOP);
				}
			case 2:
				break;
		}
	}
	
	/** Gdy naciœniêcie klawisza. */
	protected void keyPressed(int keyCode) {
		this.lastKey = getGameAction(keyCode);
		switch(getGameAction(keyCode)) {
			case Canvas.UP:
				if(this.odslonaTresci > 0) {
					this.odslonaTresci--;
				}
				break;
			case Canvas.DOWN:
				if(this.odslonaTresci < 1) {
					this.odslonaTresci++;
				}
				break;
		}
		this.repaint();
	}
	
	/** Gdy puszczenie klawisza. */
	protected void keyReleased(int keyCode) {
	}
	
	/** Abstrakcyjna metoda wywo³ywana na naciœniêcie klawisza. */
	public abstract void back();
}