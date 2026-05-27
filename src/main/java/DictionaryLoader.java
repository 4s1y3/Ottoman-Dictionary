import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DictionaryLoader {

    public static List<OttomanWord> loadFromCSV(String filePath) {
        List<OttomanWord> words = new ArrayList<>();

        try {
            // Dosyayı tek seferde okuyup karakter karakter (State Machine) işliyoruz.
            // Bu sayede tırnak (") içindeki virgüller veya alt satıra inen metinler hataya yol açmaz.
            String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);

            boolean inQuotes = false;
            List<String> currentRow = new ArrayList<>();
            StringBuilder currentCell = new StringBuilder();

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '"') {
                    inQuotes = !inQuotes; // Tırnak içine girdik veya çıktık
                } else if (c == ',' && !inQuotes) {
                    currentRow.add(currentCell.toString().trim());
                    currentCell.setLength(0); // Hücreyi sıfırla
                } else if ((c == '\n' || c == '\r') && !inQuotes) {
                    // Satır sonu (Windows için \r\n kontrolü)
                    if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                        i++;
                    }
                    currentRow.add(currentCell.toString().trim());

                    // Başlık satırını atla ve dolu satırları listeye ekle
                    if (!currentRow.isEmpty() && currentRow.size() >= 5 && !currentRow.get(0).equalsIgnoreCase("ottoman")) {
                        String ottoman = currentRow.get(0);
                        String arabic = currentRow.get(1);
                        String meaning = currentRow.get(2).replace("\"\"", "\""); // Çift tırnakları temizle
                        String origin = currentRow.get(3);
                        String category = currentRow.get(4);
                        String example = currentRow.size() > 5 ? currentRow.get(5).replace("\"\"", "\"") : "";

                        if (!ottoman.isEmpty() && !meaning.isEmpty()) {
                            words.add(new OttomanWord(ottoman, arabic, meaning, origin, category, example));
                        }
                    }
                    currentRow.clear();
                    currentCell.setLength(0);
                } else {
                    currentCell.append(c);
                }
            }

            // Dosya sonunda yeni satır yoksa kalan son satırı ekle
            if (!currentRow.isEmpty() || currentCell.length() > 0) {
                currentRow.add(currentCell.toString().trim());
                if (currentRow.size() >= 5 && !currentRow.get(0).equalsIgnoreCase("ottoman")) {
                    words.add(new OttomanWord(currentRow.get(0), currentRow.get(1), currentRow.get(2),
                            currentRow.get(3), currentRow.get(4), currentRow.size() > 5 ? currentRow.get(5) : ""));
                }
            }

            System.out.println("✔ Güvenli CSV Ayrıştırıcı Çalıştı. Yüklenen kelime: " + words.size());

        } catch (IOException e) {
            System.err.println("⚠ CSV Okuma Hatası: Lütfen 'data/sozluk.csv' dosyasının varlığından emin olun.");
        }

        return words;
    }

    public static String[] getCategoriesFromWords(List<OttomanWord> words) {
        Set<String> cats = new LinkedHashSet<>();
        for (OttomanWord w : words) cats.add(w.getCategory());
        List<String> list = new ArrayList<>();
        list.add("Tümü");
        list.addAll(cats);
        return list.toArray(new String[0]);
    }
}