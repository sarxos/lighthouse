package sarxos.graphics;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

import java.io.IOException;

import sarxos.graphics.ImageLoader;

/** Klasa reprezentuj¹ca jedn¹ klatkê animacji (lub po rprostu ramkê z obrazkiem). */
public class Frame extends Canvas {
		
	private final int TOP = Graphics.TOP;			// Po³o¿enie szczytowe
	private final int CENTER = Graphics.HCENTER;	// Po³o¿enie centralne horyzontalnie
	private final int MIDDLE = Graphics.VCENTER;	// Po³o¿enie centralne wertykalnie

	private String imgFilePath = null;				// Œcie¿ka do pliku wstawianego do ramki
	private Image rysunek = null;					// Rysunek kryj¹cy siê pod t¹ scie¿k¹.

	/** Publiczny konstruktor (koniecznym argumentem jest œcie¿kia do pliku grafiki). */
	public Frame(String imgFilePath) {					// Publiczny konstruktor - przekazujemy jako
		this.imgFilePath = imgFilePath;					// argument String ze œcie¿k¹ do pliku png, który
		this.rysunek = ImageLoader.load(imgFilePath);	// ma zostaæ wyœwietlony.
	}										

	/** Pokryta abstrakcyjna metoda paint() s³u¿¹ca do malowania klasy Graphics. */
	protected void paint(Graphics g) {

		int width = getWidth();		// Pobiera szerokoœæ ekranu.
		int height = getHeight();	// Pobiera wysokoœæ ekranu.
		
		g.setColor(0xffffff);				// Ustawiamy aktywny kolor rysowania na bia³y.
		g.fillRect(0, 0, width, height);	// Rysujemy wype³niony kolorem (bia³ym) prostok¹t o szerokoœci i wysokoœci ekranu.
		
		g.drawImage(			// narysuj go (œrodek, œrodek).
			this.rysunek, 		// referencja do pliku.
			width/2, height/2,  // w jakim punkcie narysowaæ.
			MIDDLE | CENTER		// jakie pozycjonowanie.
		);
	}
}
