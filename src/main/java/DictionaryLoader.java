import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * CSV dosyasından Osmanlıca kelime yükleyen sınıf.
 *
 * CSV Formatı (başlık satırı zorunlu):
 *   ottoman,arabic,meaning,origin,category,example
 *
 * Virgül içeren alanlar çift tırnak içine alınmalıdır:
 *   Sultan,سلطان,"Hükümdar, padişah",Arapça,Devlet,Örnek cümle.
 *
 * Neden CSV?
 *   200 kelimeyi koda yazmak yerine dosyada tutmak:
 *   - Kelime ekleme/silme için kodu değiştirmek gerekmez
 *   - 200.000 kelime de olsa aynı kod çalışır
 *   - Farklı dil dosyaları kolayca değiştirilebilir
 */
public class DictionaryLoader {

    /**
     * Verilen dosya yolundaki CSV'yi okuyup OttomanWord listesi döner.
     *
     * @param filePath  CSV dosyasının yolu (ör. "data/sozluk.csv")
     * @return          Yüklenen OttomanWord nesnelerinin listesi
     */
    public static List<OttomanWord> loadFromCSV(String filePath) {
        List<OttomanWord> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true; // ilk satır başlık, atla

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Boş satır veya başlık satırını atla
                if (line.isEmpty()) continue;
                if (firstLine) {
                    firstLine = false;
                    continue; // "ottoman,arabic,meaning,origin,category,example" başlığını atla
                }

                // Satırı virgüle göre parçala (tırnak içi virgüllere dikkat)
                String[] parts = parseCSVLine(line);

                // 6 sütun bekliyoruz (example opsiyonel olabilir)
                if (parts.length < 5) {
                    System.err.println("Hatalı satır atlandı: " + line);
                    continue;
                }

                String ottoman  = parts[0].trim();
                String arabic   = parts[1].trim();
                String meaning  = parts[2].trim();
                String origin   = parts[3].trim();
                String category = parts[4].trim();
                String example  = parts.length > 5 ? parts[5].trim() : "";

                if (!ottoman.isEmpty() && !meaning.isEmpty()) {
                    words.add(new OttomanWord(ottoman, arabic, meaning, origin, category, example));
                }
            }

            System.out.println("✔ CSV yüklendi: " + words.size() + " kelime ← " + filePath);

        } catch (FileNotFoundException e) {
            System.err.println("⚠ CSV dosyası bulunamadı: " + filePath);
            System.err.println("  Lütfen 'data/sozluk.csv' dosyasının proje klasöründe olduğundan emin olun.");
        } catch (IOException e) {
            System.err.println("⚠ Dosya okuma hatası: " + e.getMessage());
        }

        return words;
    }

    /**
     * CSV satırını doğru şekilde parçalar.
     * Tırnak içindeki virgülleri görmezden gelir.
     *
     * Örnek:
     *   Sultan,سلطان,"Hükümdar, padişah",Arapça,Devlet,Cümle.
     *   → ["Sultan", "سلطان", "Hükümdar, padişah", "Arapça", "Devlet", "Cümle."]
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes; // tırnağa gir veya çık
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString()); // alan bitti
                current.setLength(0);           // temizle
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString()); // son alan

        return fields.toArray(new String[0]);
    }

    /**
     * CSV'deki benzersiz kategorileri döner.
     * Önce "Tümü" gelir, sonra dosyadaki sıraya göre kategoriler.
     */
    public static String[] getCategoriesFromWords(List<OttomanWord> words) {
        Set<String> cats = new LinkedHashSet<>();
        for (OttomanWord w : words) cats.add(w.getCategory());
        List<String> list = new ArrayList<>();
        list.add("Tümü");
        list.addAll(cats);
        return list.toArray(new String[0]);
    }
}