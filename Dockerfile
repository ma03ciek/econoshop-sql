# Render nie ma natywnego srodowiska Java: budujemy wlasny obraz z JDK 21.
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
# kompilacja, budowa bazy z CSV, test zgodnosci z Java z zajec 4
RUN javac -cp "lib/*" -d out src/*.java \
 && java -cp "lib/*:out" ZaladujDane z5.db dane/klienci.csv dane/transakcje_2026-10.csv \
 && java -cp "lib/*:out" Kontrola z5.db
CMD ["java", "-cp", "lib/*:out", "Serwer"]
