# EconoShop B2B: należności w bazie SQLite (Java + JDBC + GitHub + Render)

Programowanie w Javie dla ekonomistów · Zajęcia 5 intro (przed blokiem SQL).

Program w Javie czyta dane z plików CSV, każdą transakcję przepuszcza przez klasę `KontoKlienta` z zajęć 4,
a wynik zapisuje do bazy SQLite. Baza `z5.db` jest dokładnie tą, na której pracujemy na zajęciach 5.
Raport należności liczy SQL; ten sam raport jest stroną WWW na Render.

```
dane/*.csv  ──ZaladujDane──▶  z5.db  ──Raport (SQL przez JDBC)──▶  konsola / Kontrola (GitHub Actions)
              (KontoKlienta)          └──Serwer──▶  https://<twoja-nazwa>.onrender.com
```

## Co jest w repozytorium

| Plik | Rola |
|---|---|
| `src/KontoKlienta.java` | klasa z zajęć 4, bez zmian: decyduje, czy faktura lub wpłata jest przyjęta |
| `src/ZaladujDane.java` | CSV → baza SQLite; kolumna `status` to decyzja `KontoKlienta` |
| `src/Raport.java` | zapytanie SQL z parametrem `?`; należności razem i HHI |
| `src/Kontrola.java` | test: 498 850,50 PLN, HHI 2646, 7 klientów; inaczej kod wyjścia 1 |
| `src/Serwer.java` | strona WWW: `/` raport, `/klient?nazwa=Dom` transakcje klienta |
| `lib/sqlite-jdbc-3.46.1.3.jar` | sterownik JDBC (zawiera całą bazę SQLite; nic nie instalujemy) |
| `Dockerfile` | przepis na obraz dla Render (Render nie ma natywnego środowiska Java) |
| `.github/workflows/kontrola.yml` | GitHub Actions: kompilacja + baza + Kontrola po każdym `push` |

## Krok po kroku

**0. Konta.** GitHub oraz Render (render.com, „Sign in with GitHub”).

**1. Własne repozytorium.** Na stronie repozytorium prowadzącego: **Use this template → Create a new repository**.
Nazwa np. `econoshop-sql`. Prywatne repozytorium: dodaj prowadzącego w *Settings → Collaborators*.

**2. Środowisko.** Jedno z dwóch:
- *GitHub Codespaces*: **Code → Codespaces → Create codespace on main**; w terminalu sprawdź `java -version` (potrzebny JDK 17+);
- *komputer*: `git clone <adres>`, JDK 17+ (np. Temurin 21), IntelliJ IDEA: *File → Project Structure → Libraries → + → lib/sqlite-jdbc-3.46.1.3.jar*.

**3. Kompilacja.** W folderze repozytorium (Windows: w `-cp` zamiast `:` wpisz `;`):

```bash
javac -cp "lib/*" -d out src/*.java
```

Starter się nie kompiluje: pięć miejsc `___` to zadania (TODO 1–5). Kompilator wskaże TODO 1, 2, 4, 5. TODO 3 jest w tekście zapytania SQL, więc kompilator go nie widzi.

**4. TODO 1–2 → baza.**

```bash
java -cp "lib/*:out" ZaladujDane z5.db dane/klienci.csv dane/transakcje_2026-10.csv
```
Wynik oczekiwany: `Baza z5.db: klienci 8, przyjeta 15, odmowa 4, nieznany 1`.

**5. TODO 3 → raport i kontrola.**

```bash
java -cp "lib/*:out" Raport z5.db
java -cp "lib/*:out" Kontrola z5.db
```
Wynik oczekiwany: `... 498850.50 ... HHI 2646 ... : OK`.

**6. Commit i push.**

```bash
git add src
git commit -m "TODO 1-3: baza i raport"
git push
```
Zakładka **Actions**: zielony znaczek = Kontrola przeszła na serwerze GitHub, nie tylko u Ciebie.

**7. TODO 4–5 → serwer lokalnie.**

```bash
java -cp "lib/*:out" Serwer
```
Otwórz `http://localhost:8080` (w Codespaces: zakładka *Ports*, port 8080). Kliknij klienta Dom: 4 transakcje, w tym odmowa 105 000,00.

**8. Render.** *Dashboard → New → Web Service* → wybierz swoje repozytorium → *Language*: **Docker** →
*Instance Type*: **Free** → *Deploy*. W logu budowania szukaj linii `... HHI 2646 ...: OK`.
Adres strony: `https://<nazwa>.onrender.com`. Każdy kolejny `push` wdraża nową wersję automatycznie.

## Dobrze wiedzieć

- Render wymaga, żeby serwer słuchał na adresie `0.0.0.0` i na porcie ze zmiennej `PORT` (domyślnie 10000).
- Instancja Free usypia po 15 minutach bez ruchu; pierwsze wejście po przerwie trwa około minuty.
- Dysk na Render jest ulotny: zmiany w plikach giną przy restarcie i każdym wdrożeniu. Dlatego baza powstaje
  w czasie budowania obrazu (Dockerfile), a strona tylko ją czyta.
- Pliki `out/` i `*.db` są w `.gitignore`: to produkty kompilacji i programu, a nie kod źródłowy.
- Baza z listopadem (zadanie domowe z zajęć 5):
  `java -cp "lib/*:out" ZaladujDane z5_listopad.db dane/klienci.csv dane/transakcje_2026-10.csv dane/transakcje_2026-11.csv`

Dane są fikcyjne.
