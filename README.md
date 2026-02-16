E-Faktura 📑 – Twoje Centrum Finansów
Aplikacja mobilna na system Android ułatwiająca życie przedsiębiorcy. Pozwala na błyskawiczne wystawianie faktur, zarządzanie bazą kontrahentów oraz generowanie dokumentów PDF zgodnie z polskimi standardami księgowymi.
🚀 Kluczowe Funkcje
 * Integracja z Białą Listą Ministerstwa Finansów: Zapomnij o ręcznym wpisywaniu danych. Aplikacja pobiera nazwę firmy, adres i numer konta prosto z oficjalnych baz państwowych po wpisaniu NIP.
 * Inteligentny Parser Adresów: Dzięki autorskiemu rozwiązaniu opartemu na wyrażeniach regularnych (Regex), aplikacja automatycznie rozdziela pobrany ciąg adresowy na ulicę, kod pocztowy i miasto.
 * Profesjonalny Generator PDF: Twórz dokumenty faktur w formacie PDF jednym kliknięciem. Dokumenty są gotowe do wysyłki bezpośrednio z aplikacji.
 * Zarządzanie Kosztami i Przychodami: Rozróżnienie faktur sprzedażowych i zakupowych wraz z dynamicznym systemem etykietowania (Nabywca/Sprzedawca).
 * Dashboard Finansowy: Śledzenie realnego przychodu (tylko opłacone faktury), kosztów oraz faktur oczekujących na zapłatę.
🛠️ Stack Technologiczny
 * Język: Kotlin
 * UI: Jetpack Compose (Modern Declarative UI)
 * Architektura: MVVM (Model-View-ViewModel) z wykorzystaniem StateFlow i SharedFlow do komunikacji UI
 * Sieć: Retrofit + Gson (obsługa API MF)
 * Baza Danych: Room (lokalne przechowywanie danych) + Firebase Firestore (synchronizacja w chmurze)
 * Przechowywanie Plików: FileProvider do bezpiecznego udostępniania wygenerowanych PDF-ów
🔧 Instalacja i Konfiguracja
 * Klonowanie repozytorium: git clone https://github.com/MatysiakQ/e-faktura.git
 * Firebase: Dodaj swój plik google-services.json do folderu app/.
 * Biała Lista MF: Aplikacja korzysta z publicznego API pod adresem https://wl-api.mf.gov.pl/. Nie wymaga dodatkowych kluczy dla zapytań o NIP.
 * Budowa: Zrób Sync Project with Gradle Files i uruchom na emulatorze lub fizycznym urządzeniu.
📂 Struktura Projektu
 * ui/: Komponenty Compose, ekrany oraz ViewModele obsługujące logikę UI.
 * data/: Repozytoria, definicje API i kontenery wstrzykiwania zależności.
 * model/: Definicje obiektów biznesowych i struktur odpowiedzi z API.
 * utils/: Narzędzia pomocnicze, w tym silnik generowania PDF.
> Notatka techniczna: Aplikacja implementuje bezpieczne zarządzanie zasobami poprzez system FileProvider, co pozwala na generowanie plików PDF w pamięci cache bez zaśmiecania pamięci użytkownika.
