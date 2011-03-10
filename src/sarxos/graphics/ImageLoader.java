package sarxos.graphics;

import javax.microedition.lcdui.Image;
import java.io.IOException;

/** Klasa �aduj�ca obrazek w formacie png z pliku. */
public class ImageLoader {

	/** Metoda �adowania obrazka z pliku. */
	public static Image load(String imgPath) {
		
		Image rysunek = null;				// Tworzymy sobie obiekt tymczasowy
											// w kt�rym b�dziemy przechowywa�
		try {								// za�adowany obrazek.
			if(rysunek == null) {						// Je�li obiekt rysunek jest pusty
				rysunek = Image.createImage(imgPath);	// pobieramy do niego rysunek z pliku
			}											// okre�lonego przez imgPath.
		}
		catch(IOException err) {							// Gdy wyst�pi b��d w komunikacji 
			System.out.println(								// wej�cia/wyj�cia (nie ma pliku) to
				"Nie mozna za�adowa� obrazka z pliku" +     // drukujemy komunikat (w kom�rce pewnie
				imgPath										// b�dzie niewidoczny, ale w konsoli jak
			);												// najbardziej si� poka�e.
		}
		if(rysunek == null) {			// Sprawdzamy czy rysunek pusty - je�li 
			System.out.println(								// tak, to znaczy �e nie za�adowa� poprawnie
				"Nie mozna za�adowa� obrazka z pliku" + 	// wi�c drukujemy na standardowe wyj�cie 
				imgPath										// komunikat o b��dzie.
			);
		}				
		return rysunek;
	}
}