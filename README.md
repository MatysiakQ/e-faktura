E-Faktura 📑
Nowoczesna aplikacja mobilna na system Android służąca do kompleksowego zarządzania fakturami i danymi kontrahentów. Aplikacja oferuje automatyzację procesów księgowych dzięki integracji z oficjalnymi bazami państwowymi oraz generatorowi dokumentów PDF.
🚀 Główne Funkcjonalności
 * Zarządzanie Firmami: Pełny moduł dodawania i edycji danych kontrahentów z możliwością przypisania własnego logo lub ikony.
 * Integracja z Białą Listą MF: Automatyczne pobieranie danych firm (nazwa, adres, numer konta) bezpośrednio z serwerów Ministerstwa Finansów po podaniu numeru NIP.
 * Inteligentne Przetwarzanie Adresów: System automatycznie rozdziela pobrany ciąg adresowy na ulicę, kod pocztowy oraz miasto przy użyciu wyrażeń regularnych (Regex).
 * Generator Faktur PDF: Tworzenie profesjonalnych dokumentów faktur w formacie PDF, gotowych do otwarcia lub udostępnienia kontrahentowi.
 * Statystyki i Dashboard: Monitorowanie przychodów (tylko opłacone faktury), kosztów oraz faktur oczekujących na zapłatę.
 * Synchronizacja w Chmurze: Integracja z Firebase (Authentication i Firestore) zapewniająca bezpieczne przechowywanie danych i dostęp z wielu urządzeń.
🛠️ Stos Technologiczny
 * Język: Kotlin
 * UI: Jetpack Compose (Modern Declarative UI)
 * Architektura: MVVM (Model-View-ViewModel) z wykorzystaniem StateFlow i SharedFlow
 * Baza Danych: Room (lokalna persistencja danych)
 * Networking: Retrofit z konwerterami Gson i Scalars
 * Obrazy: Coil (ładowanie grafik i logo firm)
 * Generowanie Dokumentów: Android PdfDocument API
📋 Konfiguracja
Uprawnienia
Aplikacja wymaga następujących uprawnień systemowych:
 * INTERNET: Do komunikacji z API Ministerstwa Finansów i Firebase.
 * CAMERA: Do skanowania kodów (opcjonalnie).
 * WRITE_EXTERNAL_STORAGE: Do zapisu generowanych faktur PDF (na starszych wersjach systemu).
Integracja API
Komunikacja z Wykazem Podatników VAT odbywa się poprzez oficjalny endpoint:
https://wl-api.mf.gov.pl/.
FileProvider
Do poprawnego otwierania wygenerowanych plików PDF aplikacja korzysta z FileProvider zdefiniowanego w manifeście pod adresem ${applicationId}.provider.
🏗️ Struktura Projektu
 * data/api: Konfiguracja Retrofit i definicja serwisów sieciowych.
 * data/repository: Logika biznesowa pobierania i przetwarzania danych.
 * ui/company: Ekrany i ViewModele zarządzania kontrahentami.
 * utils: Narzędzia pomocnicze, w tym generator PDF.
 * model: Definicje struktur danych (POJO/DTO).
Możesz skopiować ten tekst do pliku o nazwie README.md w głównym folderze swojego projektu. Chciałbyś, abym dopisał coś jeszcze, np. instrukcję instalacji dla innych programistów?
