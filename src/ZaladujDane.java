import java.nio.file.*;
import java.sql.*;
import java.util.*;

// Buduje baze SQLite z plikow CSV. Decyzje (przyjeta / odmowa / nieznany) podejmuje KontoKlienta z zajec 4.
// Uzycie: java -cp "lib/*:out" ZaladujDane z5.db dane/klienci.csv dane/transakcje_2026-10.csv [kolejne CSV]
public class ZaladujDane {
    public static void main(String[] args) throws Exception {
        String plikBazy = args[0];
        Files.deleteIfExists(Path.of(plikBazy));                     // zawsze budujemy od zera

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + plikBazy)) {
            try (Statement st = con.createStatement()) {
                st.execute("CREATE TABLE klienci (klient TEXT PRIMARY KEY, limit_pln REAL NOT NULL CHECK (limit_pln >= 0))");
                st.execute("""
                    CREATE TABLE transakcje (
                      nr INTEGER PRIMARY KEY, data TEXT NOT NULL, klient TEXT NOT NULL,
                      typ TEXT NOT NULL CHECK (typ IN ('F', 'W')), kwota REAL NOT NULL,
                      status TEXT NOT NULL CHECK (status IN ('przyjeta', 'odmowa', 'nieznany')))""");
            }
            con.setAutoCommit(false);                                    // wszystko albo nic

            // 1. klienci: CSV -> obiekty KontoKlienta i wiersze tabeli klienci
            Map<String, KontoKlienta> konta = new LinkedHashMap<>();
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO klienci (klient, limit_pln) VALUES (?, ?)")) {
                List<String> linie = Files.readAllLines(Path.of(args[1]));
                for (String linia : linie.subList(1, linie.size())) {           // bez wiersza naglowka
                    String[] p = linia.split(",");
                    konta.put(p[0], new KontoKlienta(p[0], Double.parseDouble(p[1])));
                    ps.setString(1, p[0]);
                    ps.setDouble(2, Double.parseDouble(p[1]));
                    ps.executeUpdate();
                }
            }

            // 2. transakcje: kazda przechodzi przez KontoKlienta, decyzja trafia do kolumny status
            int[] licz = new int[3];
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transakcje (nr, data, klient, typ, kwota, status) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (int i = 2; i < args.length; i++) {
                    List<String> linie = Files.readAllLines(Path.of(args[i]));
                    for (String linia : linie.subList(1, linie.size())) {
                        String[] p = linia.split(",");
                        String klient = p[2], typ = p[3];
                        double kwota = Double.parseDouble(p[4]);
                        KontoKlienta k = konta.get(klient);
                        String status;
                        if (k == null) status = "nieznany";
                        else if (typ.equals("F")) status = ___;   // TODO 1: decyzja KontoKlienta dla faktury -> "przyjeta" albo "odmowa"
                        else status = ___;                          // TODO 1: to samo dla wplaty
                        licz[status.equals("przyjeta") ? 0 : status.equals("odmowa") ? 1 : 2]++;

                        ps.setInt(1, Integer.parseInt(p[0]));
                        ps.setString(2, p[1]);
                        ps.setString(3, klient);
                        ps.setString(4, typ);
                        ps.setDouble(5, kwota);
                        ps.setString(6, ___);                     // TODO 2: szosty znak zapytania w INSERT
                        ps.executeUpdate();
                    }
                }
            }
            con.commit();
            System.out.printf("Baza %s: klienci %d, przyjeta %d, odmowa %d, nieznany %d%n",
                    plikBazy, konta.size(), licz[0], licz[1], licz[2]);
        }
    }
}
