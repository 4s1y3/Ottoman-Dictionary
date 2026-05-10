/**
 * JavaFX uygulamalarını Maven classpath üzerinden çalıştırırken
 * Application sınıfını doğrudan main class olarak ayarlamak hata verir.
 *
 * Bu Launcher sınıfı aracı görevi görür:
 * - Application'ı extend ETMEZ
 * - OttomanDictionaryApp.main() metodunu çağırır
 * - pom.xml'de mainClass olarak bu sınıf ayarlanır
 */
public class Launcher {
    public static void main(String[] args) {
        OttomanDictionaryApp.main(args);
    }
}