package sarxos.lights;

/** Klasa b³êdu: d³ugoœæ próbki jest za krótka. */
public class LightSampleToShortException extends Exception {
		
	/** Publiczny konstruktor b³êdu. */
	public LightSampleToShortException(String s) {
		// Wywo³ujemy konstruktor klasy bazowej czyli Exception.
		super(s);
	}
}