package sarxos.graphics;

import javax.microedition.lcdui.Image;
import java.io.IOException;

/** Klasa ³aduj¹ca obrazek w formacie png z pliku. */
public class ImageLoader {

	/** Metoda ³adowania obrazka z pliku. */
	public static Image load(String imgPath) {
		
		Image rysunek = null;				// Tworzymy sobie obiekt tymczasowy
											// w którym bêdziemy przechowywaæ
		try {								// za³adowany obrazek.
			if(rysunek == null) {						// Jeœli obiekt rysunek jest pusty
				rysunek = Image.createImage(imgPath);	// pobieramy do niego rysunek z pliku
			}											// okreœlonego przez imgPath.
		}
		catch(IOException err) {							// Gdy wyst¹pi b³¹d w komunikacji 
			System.out.println(								// wejœcia/wyjœcia (nie ma pliku) to
				"Nie mozna za³adowaæ obrazka z pliku" +     // drukujemy komunikat (w komórce pewnie
				imgPath										// bêdzie niewidoczny, ale w konsoli jak
			);												// najbardziej siê poka¿e.
		}
		if(rysunek == null) {			// Sprawdzamy czy rysunek pusty - jeœli 
			System.out.println(								// tak, to znaczy ¿e nie za³adowa³ poprawnie
				"Nie mozna za³adowaæ obrazka z pliku" + 	// wiêc drukujemy na standardowe wyjœcie 
				imgPath										// komunikat o b³êdzie.
			);
		}				
		return rysunek;
	}
}