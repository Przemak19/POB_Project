package pob.pob_project.crc;

import pob.pob_project.network.Packet;

import java.nio.charset.StandardCharsets;

/**
 * Klasa obliczająca kod CRC metodą dzielenia modulo-2 (XOR).
 * Działa dla dowolnego wielomianu binarnego wprowadzonego przez użytkownika.
 */
public class CRCUtil {

    /**
     * Oblicza CRC dla dowolnego tekstu.
     * @param message Tekst do zabezpieczenia
     * @param polynomial dowolny wielomian
     * @return CRC w postaci binarnej.
     */
    public String computeCRC(String message, String polynomial) {
        //jeśli wiadomość jest pusta zwracamy puste CRC
        if (message == null || message.isEmpty()) return "";

        //M(x) - zmiana wiadomości w ciąg bitów
        String bits = toBinary(message);
        //k - długość wielomianu G(x)
        int polyLen = polynomial.length();

        //M'(x) - dopisujemy do danych k-1 zer, gdzie k to długość wielomianu
        StringBuilder padded = new StringBuilder(bits);
        for (int i = 0; i < polyLen - 1; i++) padded.append('0');

        //M'(x)
        char[] data = padded.toString().toCharArray();
        //G(x)
        char[] poly = polynomial.toCharArray();

        //Dzielimy M'(x) przez G(x) modulo 2
        //Działanie modulo 2 oznacza, że zamiast odejmowania robimy XOR
        //Jeśli pierwszy bit dzielnej jest 1, wykonujemy XOR z wielomianem
        //Jeśli jest 0, przesuwamy się dalej
        for (int i = 0; i <= data.length - polyLen; i++) {
            if (data[i] == '1') {
                for (int j = 0; j < polyLen; j++) {
                    data[i + j] = xorBit(data[i + j], poly[j]);
                }
            }
        }

        //CRC - pozostałe bity
        StringBuilder crc = new StringBuilder();
        for (int i = data.length - (polyLen - 1); i < data.length; i++) {
            crc.append(data[i]);
        }

        //Zwróć CRC
        return crc.toString();
    }

    /**
     * Pomocnicza metoda — używa StandardCharsets.UTF_8.
     * Tworzy prawdziwe bajty a nie pojedyncze znaki aby korzystać z polskich znaków.
     * @param message Wiadomość do przekształcenia
     * @return Wiadomość w postaci binarnej
     */
    private String toBinary(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        StringBuilder bits = new StringBuilder();
        for (byte b : bytes) {
            bits.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return bits.toString();
    }

    //metoda wykonuje xor na bitach

    /**
     * Wykonuje xpr na bitach.
     * @param a Pierwszy bit
     * @param b Drugi bit
     * @return Wynik działania xor
     */
    private char xorBit(char a, char b) {
        return (a == b) ? '0' : '1';
    }

    /**
     * Łączy wiadomość z wygenerowanym CRC.
     * @param message Wiadomość do połączenia z CRC.
     * @param polynomial Wielomian do zabezpieczenia wiadomości
     * @return Ciąg bitów z CRC
     */
    public String appendCRC(String message, String polynomial) {
        return toBinary(message) + computeCRC(message, polynomial);
    }

    /**
     * Sprawdza poprawność CRC, jeśli wynik jest zerami zwraca true.
     * @param packet Pakiet z danymi
     * @param polynomial Wielomian przy pomocy którego dane są zabezpieczone
     * @return true lub false
     */
    public boolean validateCRC(Packet packet, String polynomial) {

        int polyLen = polynomial.length();
        char[] data = packet.getData().toCharArray();
        char[] poly = polynomial.toCharArray();

        // Dzielenie modulo 2 (XOR)
        for (int i = 0; i <= data.length - polyLen; i++) {
            if (data[i] == '1') {
                for (int j = 0; j < polyLen; j++) {
                    data[i + j] = xorBit(data[i + j], poly[j]);
                }
            }
        }

        // Sprawdź, czy reszta = same zera
        for (int i = data.length - (polyLen - 1); i < data.length; i++) {
            if (data[i] != '0') {
                return false; // CRC błędne
            }
        }

        return true; // CRC poprawne
    }

    /**
     * Wyodrębnia wiadomość z ciągu bitów zawierającego CRC.
     * @param dataWithCrc Ciąg bitów wiadomości + CRC
     * @param polynomial Wielomian przy pomocy którego dane są zabezpieczone
     * @return Część wiadomości (bity bez CRC).
     */
    public String extractMessage(String dataWithCrc, String polynomial) {
        if (dataWithCrc == null || polynomial == null || polynomial.isEmpty()) return "";

        int crcLength = polynomial.length() - 1;
        if (dataWithCrc.length() <= crcLength) return "";

        return fromBinary(dataWithCrc.substring(0, dataWithCrc.length() - crcLength));
    }

    /**
     * Wyodrębnia CRC z ciągu bitów wiadomości + CRC.
     * @param dataWithCrc ciąg bitów wiadomości + CRC
     * @param polynomial Wielomian przy pomocy którego dane są zabezpieczone
     * @return część CRC (reszta z dzielenia)
     */
    public String extractCRC(String dataWithCrc, String polynomial) {
        if (dataWithCrc == null || polynomial == null || polynomial.isEmpty()) return "";

        int crcLength = polynomial.length() - 1;
        if (dataWithCrc.length() < crcLength) return "";

        return dataWithCrc.substring(dataWithCrc.length() - crcLength);
    }

    /**
     * Zamienia ciąg bitów na String.
     * @param bits Ciąg bitów
     * @return String z bitów
     */
    public String fromBinary(String bits) {
        if (bits == null || bits.isEmpty()) return "";

        int byteCount = bits.length() / 8;
        byte[] bytes = new byte[byteCount];

        for (int i = 0; i < byteCount; i++) {
            String byteString = bits.substring(i * 8, i * 8 + 8);
            bytes[i] = (byte) Integer.parseInt(byteString, 2);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

}
