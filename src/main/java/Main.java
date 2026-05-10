/**
 * ╔══════════════════════════════════════════════════════╗
 * ║   Osmanlıca Sözlük — Başlangıç Noktası               ║
 * ╠══════════════════════════════════════════════════════╣
 * ║  Dosya Yapısı:                                       ║
 * ║    src/                                              ║
 * ║      TrieNode.java            → Trie düğümü          ║
 * ║      Trie.java                → Trie veri yapısı     ║
 * ║      OttomanWord.java         → Kelime modeli        ║
 * ║      DictionaryLoader.java    → CSV okuyucu          ║
 * ║      OttomanDictionaryApp.java→ JavaFX arayüzü       ║
 * ║      Main.java                → Bu dosya             ║
 * ║    data/                                             ║
 * ║      sozluk.csv               → Tüm kelimeler        ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * DERLEME (terminalde proje klasöründeyken):
 *   javac --module-path /path/to/javafx/lib \
 *         --add-modules javafx.controls,javafx.fxml \
 *         -encoding UTF-8 src/*.java -d out
 *
 * ÇALIŞTIRMA:
 *   java --module-path /path/to/javafx/lib \
 *        --add-modules javafx.controls,javafx.fxml \
 *        -cp out OttomanDictionaryApp
 *
 * IntelliJ ile çalıştırma → README.md'e bakın.
 */
public class Main {
    public static void main(String[] args) {
        // JavaFX Application.launch() aracılığıyla başlat
        OttomanDictionaryApp.main(args);
    }
}