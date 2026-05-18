# E-Faktura 📑

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![Database](https://img.shields.io/badge/Database-Room%20%2B%20Firestore-blue)
![API](https://img.shields.io/badge/API-Biała%20Lista%20MF-red)

Nowoczesna aplikacja mobilna na system Android wspierająca przedsiębiorców w codziennym zarządzaniu finansami.  
Aplikacja umożliwia szybkie wystawianie faktur, zarządzanie kontrahentami oraz generowanie profesjonalnych dokumentów PDF zgodnych z polskimi standardami księgowymi.

---

# 📱 Zrzuty Ekranu

<p align="center">
  <img src="screenshots/dashboard.jpg" width="250"/>
  <img src="screenshots/invoice_create.jpg" width="250"/>
  <img src="screenshots/invoice_preview.jpg" width="250"/>
</p>

<p align="center">
  <img src="screenshots/contractors_list.jpg" width="250"/>
  <img src="screenshots/preview.jpg" width="250"/>
</p>

---

# ✨ Funkcje

## 📌 Zarządzanie Fakturami

- Tworzenie faktur sprzedażowych i zakupowych
- Dynamiczne oznaczanie stron transakcji (Nabywca / Sprzedawca)
- Status płatności faktur
- Automatyczne obliczanie wartości netto i brutto

---

## 🏢 Integracja z Białą Listą Ministerstwa Finansów

Aplikacja wykorzystuje publiczne API Ministerstwa Finansów:

- automatyczne pobieranie danych firmy po numerze NIP,
- weryfikacja numeru rachunku bankowego,
- pobieranie adresu działalności gospodarczej.

Źródło API: https://wl-api.mf.gov.pl/

---

## 🧠 Inteligentny Parser Adresów

Autorski mechanizm parsowania adresów oparty na wyrażeniach regularnych (Regex):

- rozdzielanie ulicy,
- kodu pocztowego,
- miasta,
- numerów lokali i budynków.

Pozwala to automatycznie uzupełniać formularze bez ręcznej edycji danych.

---

## 📄 Generowanie PDF

- generowanie profesjonalnych dokumentów PDF,
- gotowe pliki do wysłania klientowi,
- bezpieczne udostępnianie dokumentów poprzez `FileProvider`,
- przechowywanie plików w pamięci cache aplikacji.

---

## 📊 Dashboard Finansowy

Panel finansowy umożliwia:

- śledzenie rzeczywistego przychodu,
- kontrolę kosztów,
- monitorowanie nieopłaconych faktur,
- szybki podgląd aktualnej sytuacji finansowej.

---

# 🛠️ Stack Technologiczny

| Kategoria | Technologia |
|---|---|
| Język | Kotlin |
| UI | Jetpack Compose |
| Architektura | MVVM |
| Reactive Streams | StateFlow / SharedFlow |
| Networking | Retrofit + Gson |
| Baza Lokalna | Room |
| Chmura | Firebase Firestore |
| PDF | Android PDF APIs |
| Udostępnianie plików | FileProvider |

---

# 🏗️ Architektura

Projekt został oparty o architekturę MVVM z wyraźnym podziałem odpowiedzialności:

```text
ui/        -> Ekrany Compose, ViewModele, stany UI
data/      -> Repozytoria, API, źródła danych
model/     -> Modele biznesowe i DTO
utils/     -> Narzędzia pomocnicze, generator PDF
```

W komunikacji UI wykorzystano:

- `StateFlow` do zarządzania stanem,
- `SharedFlow` do obsługi zdarzeń jednorazowych,
- separację logiki biznesowej od warstwy prezentacji.

---

# 🔧 Instalacja

## 1. Klonowanie repozytorium

```bash
git clone https://github.com/MatysiakQ/e-faktura.git
```

## 2. Firebase

Dodaj plik:

```text
google-services.json
```

do katalogu:

```text
app/
```

---

## 3. Synchronizacja projektu

Uruchom:

```text
Sync Project with Gradle Files
```

następnie uruchom aplikację na:

- emulatorze Androida,
- lub fizycznym urządzeniu.

---

# 🔐 Bezpieczeństwo i Zarządzanie Plikami

Aplikacja implementuje bezpieczny mechanizm zarządzania dokumentami poprzez `FileProvider`.

Dzięki temu:

- pliki PDF nie są przechowywane permanentnie,
- aplikacja nie zaśmieca pamięci użytkownika,
- dokumenty mogą być bezpiecznie współdzielone z innymi aplikacjami.

---

# 🚀 Możliwe Rozszerzenia

- eksport danych do CSV,
- obsługa wielu firm,
- synchronizacja offline-first,
- wysyłka faktur e-mailem,
- integracja z płatnościami online,
- generowanie raportów miesięcznych.

---

# 👨‍💻 Autor

Projekt stworzony przez [Adam Jastrzębski](https://github.com/MatysiakQ)
