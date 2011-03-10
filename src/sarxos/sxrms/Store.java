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
	public RecordStore rms = null;		// Otwarty pakiet RMS na którym dzia³amy.
	public int size = 0;				// Iloœæ rekordów w pakiecie.
	
	/** Konstruktor publiczny obiektu Store (magazyn rekordów). */
	public Store(String rmsName) {	// Konstruktor wymaga wprowadzenia nazwy magazynu rekordów.
		this.rmsName = rmsName;		// Ustawiamy nazwê pakietu.
		this.open();				// Otwieramy pakiet.
		try {										// Przy pobieraniu generowany jest wyj¹tek
			this.size = this.rms.getNumRecords();	// ¿e pakiet nie otwarty. Przechwycimy go.
		}											// I pobieramy iloœæ rekordów.
		catch(RecordStoreNotOpenException e) {
			System.out.println(">> B³¹d! Pakiet RMS nie otwarty.");
		}
	}
	
	/** Otwieranie pakietu rms. */
	public RecordStore open() {
		// Umieszczamy w try poniewa¿ otwarcie RMS generuje wyj¹tek, który
		// musi byæ przechwycony.
		try {
			this.rms = RecordStore.openRecordStore(	// Otwieramy magazyn rekordów
				this.rmsName, 						// podaj¹c jako argument jego nazwê oraz
				true								// zmienn¹ logiczn¹ okreslaj¹ca, czy w przypadku
			);										// gdy nie istnmieje taki magazyn to ma zostaæ utworzony.
		}
		// Przechwytywanie b³êdu.
		catch(RecordStoreException e) {
			System.out.println(
				">> B³¹d! Wyj¹tek przy otwieraniu pakietu [" + 
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
			System.out.println(">> B³¹d! B³¹d przy zamykaniu pakietu RMS.");
		}
	}

	/** Usuwanie ca³ego pakietu RMS. */
	public void delete() {
		this.close();
		try {
			RecordStore.deleteRecordStore(this.rmsName);
		}
		catch(RecordStoreNotFoundException e) {
			System.out.println(
				">> B³¹d! Pakiet RMS " + this.rmsName +
				" który ma byæ usuniety nie istnieje."
			);
		}
		catch(RecordStoreException e) {
			System.out.println(
				">> B³¹d! Wyj¹tek pakietu RMS przy jego usuwaniu " +
				"[nazwa: " + this.rmsName + "]."
			);
		}
	}

	/** Usuwanie ca³ego pakietu RMS bez jego otwieranie - metoda statyczna. */
	public static void delete(String rmsName) {
		try {
			RecordStore.deleteRecordStore(rmsName);
		}
		catch(RecordStoreNotFoundException e) {
			System.out.println(
				">> B³¹d! Pakiet RMS " + rmsName +
				" który ma byæ usuniety nie istnieje."
			);
		}
		catch(RecordStoreException e) {
			System.out.println(
				">> B³¹d! Wyj¹tek pakietu RMS przy jego usuwaniu " +
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
			System.out.println(">> B³¹d! Z³y numer identyfikacji rekordu: " + id);
		}
		catch(RecordStoreException e) {
			System.out.println(">> B³¹d! B³¹d ogólny pakietu RMS.");
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
			System.out.println("Nieprawid³owe ID.");
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
		int idnum = -1;												// Pocz¹tkowa wartoœæ numeru identyfikacyjnego -1.
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 	// Tworzymy wyjœciowy strumieñ tablicy bajtów bajtów.
		DataOutputStream os = new DataOutputStream(baos);			// Tworzymy strumieñ wyjœciowy do tej tablicy.
		try {
			os.writeUTF(wartosc);				// Zapisujemy przez strumieñ do tablicy bajtów weartoœæ. 
			os.close();							// Zamykamy strumieñ wyjœciowy.
		}
		catch(IOException e) {
			System.err.println(e);
		}
		byte[] dane = baos.toByteArray();	// Konwertujemy obiekt strumieñ wyjœciowy bajtów na tablicê.
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
	
	/** Ustawianie nowej wartoœci rekordu w pakiecie RMS. */
	public void setRecord(int id, String wartosc) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 	// Tworzymy wyjœciowy strumieñ tablicy bajtów bajtów.
		DataOutputStream os = new DataOutputStream(baos);			// Tworzymy strumieñ wyjœciowy do tej tablicy.
		try {
			os.writeUTF(wartosc);				// Zapisujemy przez strumieñ do tablicy bajtów weartoœæ. 
			os.close();							// Zamykamy strumieñ wyjœciowy.
		}
		catch(IOException e) {
			System.err.println(e);
		}
		byte[] dane = baos.toByteArray();	// Konwertujemy obiekt strumieñ wyjœciowy bajtów na tablicê.
		int p = 0;							// Pocz¹tkowy indeks danych.
		int k = dane.length;				// Koñcowy indeks danych.
		try {
			this.rms.setRecord(id, dane, p, k);	// Ustawiamy dane w pakiecie.
		}
		catch(Exception e) {					// Przechwytujemy wyj¹tek.
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
				">> B³¹d! Pakiet " + this.rmsName + 
				" nie zosta³ wczeœniej otwarty."
			);
			this.size++;
		}
		catch(InvalidRecordIDException e) {
			System.out.println(
				">> B³¹d! Z³y numer identyfikacyjny rekordu: " +
				id + "."
			);
			this.size++;
		}
		catch(RecordStoreException e) {
			System.out.println(">> B³¹d! Wyj¹tek pakietu RMS.");
			this.size++;
		}
	}
	
	/** Usuwanie wszystkich rekordów z pakietu RMS. */
	public void delAllRecords() {
		
		RecordEnumeration en = null;							// Tworzymy nowy obiekt wyliczeniowy rekordów.
		try {													// W enumerateRecords() jest generowany wyj¹tek który przechwycimy.
			en = this.rms.enumerateRecords(null, null, false);	// Tworzymy wyliczenie - enumerator rekordów.
		}
		catch(RecordStoreNotOpenException e) {
			System.out.println(
				">> B³¹d! Przed enumeracj¹ pakiet RMS " + 
				this.rmsName + " nie zosta³ otwarty."
			);
		}
		
		int id = -1;
		while(en.hasNextElement()) {	// Dopóki w enumeratorze jest nastêpny nie strawersowany element
			try {						// to wtedy
				id = en.nextRecordId();	// pobiera jego identyfikator
			}
			// Jesli jest b³¹d to go chwytamy i wyœwietlamy komunikat do konsoli.
			catch(InvalidRecordIDException e) {
				System.out.println(
					">> B³¹d! W enumeracji pakietu RMS " + this.rmsName +
					" pobrano z³y identyfikator rekordu: " + id + "."
				);
			}
			this.delRecord(id);		// Na podstawie pobranego identyfikatora usuwa rekord
		}
	}
}