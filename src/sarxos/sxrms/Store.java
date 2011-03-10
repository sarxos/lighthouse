package sarxos.sxrms;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import java.io.IOException;

public class Store {
	
	public String rmsName = null;		// Nazwa pakietu RMS.
	public RecordStore rms = null;		// Otwarty pakiet RMS na kt�rym dzia�amy.
	public int size = 0;				// Ilo�� rekord�w w pakiecie.
	
	/** Konstruktor publiczny obiektu Store (magazyn rekord�w). */
	public Store(String rmsName) {	// Konstruktor wymaga wprowadzenia nazwy magazynu rekord�w.
		this.rmsName = rmsName;		// Ustawiamy nazw� pakietu.
		this.open();				// Otwieramy pakiet.
		try {										// Przy pobieraniu generowany jest wyj�tek
			this.size = this.rms.getNumRecords();	// �e pakiet nie otwarty. Przechwycimy go.
		}											// I pobieramy ilo�� rekord�w.
		catch(RecordStoreNotOpenException e) {
			System.out.println(">> B��d! Pakiet RMS nie otwarty.");
		}
	}
	
	/** Otwieranie pakietu rms. */
	public RecordStore open() {
		// Umieszczamy w try poniewa� otwarcie RMS generuje wyj�tek, kt�ry
		// musi by� przechwycony.
		try {
			this.rms = RecordStore.openRecordStore(	// Otwieramy magazyn rekord�w
				this.rmsName, 						// podaj�c jako argument jego nazw� oraz
				true								// zmienn� logiczn� okreslaj�ca, czy w przypadku
			);										// gdy nie istnmieje taki magazyn to ma zosta� utworzony.
		}
		// Przechwytywanie b��du.
		catch(RecordStoreException e) {
			System.out.println(
				">> B��d! Wyj�tek przy otwieraniu pakietu [" + 
				this.rmsName + "]."
			);
		}
		return this.rms;
	}
	
	/** Zamykanie pakietu rms. */
	public void close() {
		try {
			this.rms.closeRecordStore();
		}
		catch(RecordStoreException e) {
			System.out.println(">> B��d! B��d przy zamykaniu pakietu RMS.");
		}
	}

	/** Usuwanie ca�ego pakietu RMS. */
	public void delete() {
		this.close();
		try {
			RecordStore.deleteRecordStore(this.rmsName);
		}
		catch(RecordStoreNotFoundException e) {
			System.out.println(
				">> B��d! Pakiet RMS " + this.rmsName +
				" kt�ry ma by� usuniety nie istnieje."
			);
		}
		catch(RecordStoreException e) {
			System.out.println(
				">> B��d! Wyj�tek pakietu RMS przy jego usuwaniu " +
				"[nazwa: " + this.rmsName + "]."
			);
		}
	}

	/** Usuwanie ca�ego pakietu RMS bez jego otwieranie - metoda statyczna. */
	public static void delete(String rmsName) {
		try {
			RecordStore.deleteRecordStore(rmsName);
		}
		catch(RecordStoreNotFoundException e) {
			System.out.println(
				">> B��d! Pakiet RMS " + rmsName +
				" kt�ry ma by� usuniety nie istnieje."
			);
		}
		catch(RecordStoreException e) {
			System.out.println(
				">> B��d! Wyj�tek pakietu RMS przy jego usuwaniu " +
				"[nazwa: " + rmsName + "]."
			);
		}
	}

	/** Pobieranie znakowego rekordu z pakietu RMS. */	
	public String getRecordString(int id) {
		byte[] dane = null;
		try {
			if(id <= 0) {
				throw new InvalidRecordIDException();
			}
			dane = this.rms.getRecord(id);
		}
		catch(InvalidRecordIDException e) {
			System.out.println(">> B��d! Z�y numer identyfikacji rekordu: " + id);
		}
		catch(RecordStoreException e) {
			System.out.println(">> B��d! B��d og�lny pakietu RMS.");
		}
		ByteArrayInputStream bais =  new ByteArrayInputStream(dane);;
		DataInputStream is = new DataInputStream(bais);
		String s = null;
		try {
			s = is.readUTF();
			is.close();
		}
		catch(IOException e) { 
			System.err.println(e);
		}
		return s;
	}

	/** Pobieranie liczbowego rekordu z pakietu RMS. */
	public int getRecordInt(int id) {
		byte[] dane = null;
		try {
			if(id <= 0) {
				throw new InvalidRecordIDException();
			}
			dane = this.rms.getRecord(id);
		}
		catch(InvalidRecordIDException e) {
			System.err.println(e);
			System.out.println("Nieprawid�owe ID.");
		}
		catch(RecordStoreException e) {
			System.err.println(e);
		}
		ByteArrayInputStream bais =  new ByteArrayInputStream(dane);;
		DataInputStream is = new DataInputStream(bais);
		int s = 0;
		try {
			s = is.readInt();
			is.close();
		}
		catch(IOException e) { 
			System.err.println(e);
		}
		return s;
	}
	
	/** Dodawanie rekordu do pakietu RMS. */
	public int addRecord(String wartosc) {
		int idnum = -1;												// Pocz�tkowa warto�� numeru identyfikacyjnego -1.
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 	// Tworzymy wyj�ciowy strumie� tablicy bajt�w bajt�w.
		DataOutputStream os = new DataOutputStream(baos);			// Tworzymy strumie� wyj�ciowy do tej tablicy.
		try {
			os.writeUTF(wartosc);				// Zapisujemy przez strumie� do tablicy bajt�w wearto��. 
			os.close();							// Zamykamy strumie� wyj�ciowy.
		}
		catch(IOException e) {
			System.err.println(e);
		}
		byte[] dane = baos.toByteArray();	// Konwertujemy obiekt strumie� wyj�ciowy bajt�w na tablic�.
		int p = 0;
		int k = dane.length;
		try {
			idnum = this.rms.addRecord(dane, p, k);
			this.size++;
		}
		catch(Exception e) {
			System.err.println(e);
			this.size--;
		}
		return idnum;
	}
	
	/** Ustawianie nowej warto�ci rekordu w pakiecie RMS. */
	public void setRecord(int id, String wartosc) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 	// Tworzymy wyj�ciowy strumie� tablicy bajt�w bajt�w.
		DataOutputStream os = new DataOutputStream(baos);			// Tworzymy strumie� wyj�ciowy do tej tablicy.
		try {
			os.writeUTF(wartosc);				// Zapisujemy przez strumie� do tablicy bajt�w wearto��. 
			os.close();							// Zamykamy strumie� wyj�ciowy.
		}
		catch(IOException e) {
			System.err.println(e);
		}
		byte[] dane = baos.toByteArray();	// Konwertujemy obiekt strumie� wyj�ciowy bajt�w na tablic�.
		int p = 0;							// Pocz�tkowy indeks danych.
		int k = dane.length;				// Ko�cowy indeks danych.
		try {
			this.rms.setRecord(id, dane, p, k);	// Ustawiamy dane w pakiecie.
		}
		catch(Exception e) {					// Przechwytujemy wyj�tek.
			System.err.println(e);
		}
	}
	
	/** Sprawdzanie czy jest dany rekord w pakiecie. */
	public boolean isRecord(int id) {
		boolean b = true;
		try {
			byte[] y = this.rms.getRecord(id);
		}
		catch(RecordStoreNotOpenException e) { }
		catch(InvalidRecordIDException e) {
			b = false;
		}
		catch(RecordStoreException e) { }
		return b;
	}
	
	/** Usuwanie rekordu z pakietu RMS. */
	public void delRecord(int id) {
		try {
			this.rms.deleteRecord(id);
			this.size--;
		}
		catch(RecordStoreNotOpenException e) {
			System.out.println(
				">> B��d! Pakiet " + this.rmsName + 
				" nie zosta� wcze�niej otwarty."
			);
			this.size++;
		}
		catch(InvalidRecordIDException e) {
			System.out.println(
				">> B��d! Z�y numer identyfikacyjny rekordu: " +
				id + "."
			);
			this.size++;
		}
		catch(RecordStoreException e) {
			System.out.println(">> B��d! Wyj�tek pakietu RMS.");
			this.size++;
		}
	}
	
	/** Usuwanie wszystkich rekord�w z pakietu RMS. */
	public void delAllRecords() {
		
		RecordEnumeration en = null;							// Tworzymy nowy obiekt wyliczeniowy rekord�w.
		try {													// W enumerateRecords() jest generowany wyj�tek kt�ry przechwycimy.
			en = this.rms.enumerateRecords(null, null, false);	// Tworzymy wyliczenie - enumerator rekord�w.
		}
		catch(RecordStoreNotOpenException e) {
			System.out.println(
				">> B��d! Przed enumeracj� pakiet RMS " + 
				this.rmsName + " nie zosta� otwarty."
			);
		}
		
		int id = -1;
		while(en.hasNextElement()) {	// Dop�ki w enumeratorze jest nast�pny nie strawersowany element
			try {						// to wtedy
				id = en.nextRecordId();	// pobiera jego identyfikator
			}
			// Jesli jest b��d to go chwytamy i wy�wietlamy komunikat do konsoli.
			catch(InvalidRecordIDException e) {
				System.out.println(
					">> B��d! W enumeracji pakietu RMS " + this.rmsName +
					" pobrano z�y identyfikator rekordu: " + id + "."
				);
			}
			this.delRecord(id);		// Na podstawie pobranego identyfikatora usuwa rekord
		}
	}
}