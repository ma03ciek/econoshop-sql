public class KontoKlienta {
    private final String klient;
    private final long limitGr;         // limit kupiecki w groszach
    private long saldoGr;               // naleznosc w groszach; zawsze 0 <= saldo <= limit
    private int odmowy;

    public KontoKlienta(String klient, double limit) {
        if (klient == null || klient.isBlank()) {
            throw new IllegalArgumentException("pusta nazwa klienta");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit < 0: " + limit);
        }
        this.klient = klient;
        this.limitGr = Math.round(limit * 100);
        this.saldoGr = 0;
    }

    public boolean wystawFakture(double kwota) {
        long gr = Math.round(kwota * 100);
        if (gr <= 0 || saldoGr + gr > limitGr) {
            odmowy++;
            return false;
        }
        saldoGr += gr;
        return true;
    }

    public boolean zaplac(double kwota) {
        long gr = Math.round(kwota * 100);
        if (gr <= 0 || gr > saldoGr) {
            odmowy++;
            return false;
        }
        saldoGr -= gr;
        return true;
    }

    public double saldo()          { return saldoGr / 100.0; }
    public double dostepnyLimit()  { return (limitGr - saldoGr) / 100.0; }
    public boolean rozliczone()    { return saldoGr == 0; }
    public int odmowy()            { return odmowy; }
    public String klient()         { return klient; }
}
