package sarxos.graphics;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

import java.io.IOException;

import sarxos.graphics.ImageLoader;

/** Klasa reprezentuj�ca jedn� klatk� animacji (lub po rprostu ramk� z obrazkiem). */
public class Frame extends Canvas {
		
	private final int TOP = Graphics.TOP;			// Po�o�enie szczytowe
	private final int CENTER = Graphics.HCENTER;	// Po�o�enie centralne horyzontalnie
	private final int MIDDLE = Graphics.VCENTER;	// Po�o�enie centralne wertykalnie

	private String imgFilePath = null;				// �cie�ka do pliku wstawianego do ramki
	private Image rysunek = null;					// Rysunek kryj�cy si� pod t� scie�k�.

	/** Publiczny konstruktor (koniecznym argumentem jest �cie�kia do pliku grafiki). */
	public Frame(String imgFilePath) {					// Publiczny konstruktor - przekazujemy jako
		this.imgFilePath = imgFilePath;					// argument String ze �cie�k� do pliku png, kt�ry
		this.rysunek = ImageLoader.load(imgFilePath);	// ma zosta� wy�wietlony.
	}										

	/** Pokryta abstrakcyjna metoda paint() s�u��ca do malowania klasy Graphics. */
	protected void paint(Graphics g) {

		int width = getWidth();		// Pobiera szeroko�� ekranu.
		int height = getHeight();	// Pobiera wysoko�� ekranu.
		
		g.setColor(0xffffff);				// Ustawiamy aktywny kolor rysowania na bia�y.
		g.fillRect(0, 0, width, height);	// Rysujemy wype�niony kolorem (bia�ym) prostok�t o szeroko�ci i wysoko�ci ekranu.
		
		g.drawImage(			// narysuj go (�rodek, �rodek).
			this.rysunek, 		// referencja do pliku.
			width/2, height/2,  // w jakim punkcie narysowa�.
			MIDDLE | CENTER		// jakie pozycjonowanie.
		);
	}
}
