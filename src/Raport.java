import java.sql.*;
import java.util.*;

// Raport naleznosci: SQL liczy salda, Java liczy razem i HHI. Ta sama klasa zasila konsole i strone WWW.
public class Raport {
    public record Wiersz(String klient, double saldo) {}

    static final String SQL = """
        SELECT klient,
               ROUND(SUM(CASE WHEN typ = 'F' THEN kwota ELSE -kwota END), 2) AS saldo
        FROM transakcje
        WHERE status = ?
        GROUP BY klient
        ORDER BY saldo DESC""";

    public static List<Wiersz> salda(String plikBazy) throws SQLException {
        List<Wiersz> wynik = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + plikBazy);
             PreparedStatement ps = con.prepareStatement(SQL)) {
            // TODO 3: zamien ___ w zapytaniu na znak zapytania i ustaw tu jego wartosc (ps.setString)
            ps.setString(1, "przyjeta");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) wynik.add(new Wiersz(rs.getString("klient"), rs.getDouble("saldo")));
            }
        }
        return wynik;
    }

    public static double razem(List<Wiersz> w) {
        long gr = 0;
        for (Wiersz x : w) gr += Math.round(x.saldo() * 100);
        return gr / 100.0;
    }

    public static double hhi(List<Wiersz> w) {
        double suma = razem(w), h = 0;
        if (suma <= 0) return 0;
        for (Wiersz x : w) { double u = 100 * x.saldo() / suma; h += u * u; }
        return h;
    }

    public static void main(String[] args) throws SQLException {
        List<Wiersz> w = salda(args.length > 0 ? args[0] : "z5.db");
        for (Wiersz x : w) System.out.printf("%-6s %12.2f%n", x.klient(), x.saldo());
        System.out.printf("Naleznosci razem: %.2f PLN%nHHI: %.0f%n", razem(w), hhi(w));
    }
}
