import java.util.*;

// Test uruchamiany przez GitHub Actions po kazdym push: wynik musi sie zgadzac z Java z zajec 4.
public class Kontrola {
    public static void main(String[] args) throws Exception {
        List<Raport.Wiersz> w = Raport.salda(args.length > 0 ? args[0] : "z5.db");
        double razem = Raport.razem(w);
        long hhi = Math.round(Raport.hhi(w));
        boolean ok = Math.abs(razem - 498850.50) < 0.005 && hhi == 2646 && w.size() == 7;
        System.out.printf("naleznosci %.2f (oczekiwane 498850.50), HHI %d (2646), klientow %d (7): %s%n",
                razem, hhi, w.size(), ok ? "OK" : "BLAD");
        if (!ok) System.exit(1);                     // kod 1 = czerwony krzyzyk w GitHub Actions
    }
}
