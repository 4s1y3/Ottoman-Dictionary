/**
 * Osmanlıca bir sözlük kelimesini temsil eden model sınıfı.
 */
public class OttomanWord implements Comparable<OttomanWord> {

    private String ottoman;  // Latin harfli Osmanlıca kelime
    private String arabic;   // Arapça harfli yazılış
    private String meaning;  // Türkçe anlam
    private String origin;   // Köken (Arapça, Farsça, Türkçe...)
    private String category; // Kategori (Devlet, Din, Edebiyat...)
    private String example;  // Örnek cümle

    public OttomanWord(String ottoman, String arabic, String meaning,
                       String origin, String category, String example) {
        this.ottoman  = ottoman;
        this.arabic   = arabic;
        this.meaning  = meaning;
        this.origin   = origin;
        this.category = category;
        this.example  = example;
    }

    public String getOttoman()  { return ottoman; }
    public String getArabic()   { return arabic; }
    public String getMeaning()  { return meaning; }
    public String getOrigin()   { return origin; }
    public String getCategory() { return category; }
    public String getExample()  { return example; }

    @Override
    public int compareTo(OttomanWord other) {
        return this.ottoman.compareToIgnoreCase(other.ottoman);
    }

    @Override
    public String toString() { return ottoman + " → " + meaning; }
}