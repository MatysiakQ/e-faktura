# e-faktura 📄💰

[![Status](https://img.shields.io/badge/Status-Closed-red)](https://github.com/MatysiakQ/e-faktura)
[![Tech](https://img.shields.io/badge/Stack-PHP%20%7C%20MySQL%20%7C%20HTML%20%7C%20CSS-blue)](https://github.com/MatysiakQ/e-faktura)

## 📝 O Projekcie
**e-faktura** to aplikacja webowa służąca do kompleksowego zarządzania procesem fakturowania w firmie. System pozwala na szybkie wystawianie dokumentów sprzedażowych, zarządzanie bazą klientów oraz automatyczne generowanie profesjonalnych plików PDF.

## 🛠️ Stos Technologiczny
- **Backend:** PHP (logika biznesowa, obliczenia VAT).
- **Baza danych:** MySQL (przechowywanie kontrahentów i historii faktur).
- **Frontend:** HTML5, CSS3 (responsywny interfejs).
- **Biblioteki:** PDF Generator (FPDF/TCPDF lub pokrewne).

## ✨ Główne Funkcjonalności
- **Generator PDF:** Automatyczne tworzenie dokumentów gotowych do wysyłki e-mailem lub druku.
- **Baza Kontrahentów:** Zarządzanie danymi klientów (NIP, adresy, dane kontaktowe).
- **Zarządzanie Produktami:** Katalog towarów i usług z predefiniowanymi stawkami VAT.
- **Automatyzacja Obliczeń:** System sam wylicza sumy netto, kwoty podatku oraz wartości brutto.
- **Walidacja danych:** Sprawdzanie poprawności numerów NIP i terminów płatności.

## 🚀 Uruchomienie
1. Sklonuj repozytorium:
   ```bash
   git clone [https://github.com/MatysiakQ/e-faktura.git](https://github.com/MatysiakQ/e-faktura.git)
Skonfiguruj bazę danych MySQL przy użyciu dołączonego pliku .sql.

Skonfiguruj dane połączenia w pliku konfiguracyjnym (np. config.php).

Uruchom projekt na serwerze obsługującym PHP (np. XAMPP, Apache).

👥 Autor
MatysiakQ - GitHub Profile
