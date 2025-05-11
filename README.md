# Algorytm optymalizacji płatności

Aplikacja konsolowa wykorzystująca algorytm zachłanny do optymalizacji płatności za zamówienia w celu znalezienia optymalnego wyboru metody płatności z uwzględnieniem ich rabatów oraz limitów.

## Wymagania
* Java 21
* Maven
* Git (opcjonalnie do sklonowania repozytorium)

## Uruchamianie
1) **Pobranie aplikacji:**

Aplikację można pobrać jako archiwum .zip lub sklonować repozytorium przy pomocy komendy (jednak wymaga to zainstalowania Git):
```bash
git clone https://github.com/KonradStr/OcadoTask.git
```

2) **Zbudowanie aplikacji:**
 * Przejdź do głównego folderu aplikacji (tam, gdzie znajduje się plik pom.xml)
```bash
cd OcadoTask
```
 * Uruchom komendę do zbudowania aplikacji:
```bash
mvn install
```
3) **Uruchomienie aplikacji:**
 * Przejdź do folderu 'target'
```bash
cd target 
```
 * Uruchom aplikację, podając ścieżki do plików z zamówieniami oraz metodami płatności
```bash
java -jar app.jar <ścieżka_do_json_z_zamówieniami> <ścieżka_do_json_z_metodami_płatności>
```
Na przykład:
```bash
java -jar app.jar C:\Users\Konrad\Desktop\ocado\orders.json C:\Users\Konrad\Desktop\ocado\paymentmethods.json
```
4) **(opcjonalne) Uruchomienie testów:**
 * Jeśli znajdujesz się w folderze 'target', cofnij się do folderu głównego:
```bash
cd ..
```
 * Uruchom testy:
```bash
mvn test
```