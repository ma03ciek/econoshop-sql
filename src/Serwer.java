import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

// Strona WWW z raportem. Bez frameworka: serwer HTTP wbudowany w JDK.
// Lokalnie: java -cp "lib/*:out" Serwer  ->  http://localhost:8080
public class Serwer {
    static final String BAZA = "z5.db";
    static final Locale PL = Locale.forLanguageTag("pl-PL");

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));   // Render ustawia PORT
        HttpServer srv = HttpServer.create(new InetSocketAddress(___, port), 0);       // TODO 4: adres, na ktorym Render nas uslyszy
        srv.createContext("/", Serwer::raport);
        srv.createContext("/klient", Serwer::klient);
        srv.start();
        System.out.println("Serwer dziala na porcie " + port);
    }

    static void raport(HttpExchange ex) throws IOException {
        try {
            List<Raport.Wiersz> w = Raport.salda(BAZA);
            StringBuilder t = new StringBuilder("<h1>Należności EconoShop B2B</h1><table><tr><th>Klient</th><th>Saldo [PLN]</th></tr>");
            for (Raport.Wiersz x : w)
                t.append(String.format(PL, "<tr><td><a href='/klient?nazwa=%s'>%s</a></td><td class=n>%,.2f</td></tr>",
                        x.klient(), x.klient(), x.saldo()));
            t.append(String.format(PL, "<tr><th>Razem</th><th class=n>%,.2f</th></tr></table><p>HHI należności: <b>%.0f</b></p>",
                    Raport.razem(w), Raport.hhi(w)));
            wyslij(ex, 200, t.toString());
        } catch (SQLException e) { wyslij(ex, 500, "Błąd bazy: " + esc(e.getMessage())); }
    }

    static void klient(HttpExchange ex) throws IOException {
        String nazwa = parametr(ex, "nazwa");
        String sql = "SELECT nr, data, typ, kwota, status FROM transakcje WHERE klient = ? ORDER BY nr";
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + BAZA);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ___);                                         // TODO 5: wartosc parametru z adresu strony
            StringBuilder t = new StringBuilder("<h1>Klient " + esc(nazwa) + "</h1><table><tr><th>nr</th><th>data</th><th>typ</th><th>kwota</th><th>status</th></tr>");
            int n = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    n++;
                    t.append(String.format(PL, "<tr><td>%d</td><td>%s</td><td>%s</td><td class=n>%,.2f</td><td>%s</td></tr>",
                            rs.getInt("nr"), rs.getString("data"), rs.getString("typ"), rs.getDouble("kwota"), rs.getString("status")));
                }
            }
            t.append("</table><p>Transakcji: " + n + "</p><p><a href='/'>powrót</a></p>");
            wyslij(ex, 200, t.toString());
        } catch (SQLException e) { wyslij(ex, 500, "Błąd bazy: " + esc(e.getMessage())); }
    }

    static String parametr(HttpExchange ex, String nazwa) {
        String q = ex.getRequestURI().getRawQuery();
        if (q == null) return "";
        for (String para : q.split("&")) {
            String[] kv = para.split("=", 2);
            if (kv[0].equals(nazwa) && kv.length == 2) return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
        }
        return "";
    }

    static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    static void wyslij(HttpExchange ex, int kod, String body) throws IOException {
        String html = "<!doctype html><meta charset=utf-8><title>Należności</title>"
                + "<style>body{font-family:sans-serif;max-width:640px;margin:2em auto}td,th{padding:4px 10px;border-bottom:1px solid #ddd}.n{text-align:right}</style>"
                + body;
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(kod, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}
