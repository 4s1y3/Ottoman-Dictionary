import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Osmanlıca sözlük için Trie (Ön Ek Ağacı) veri yapısı.
 *
 * Zaman Karmaşıklığı:
 *   insert       → O(m)      m = kelimenin uzunluğu
 *   search       → O(m)
 *   searchByPrefix → O(p+k)  p = önek uzunluğu, k = bulunan kelime sayısı
 *   delete       → O(m)
 *
 * Alan Karmaşıklığı: O(N * M)  N = kelime sayısı, M = ortalama uzunluk
 */
public class Trie {

    private final TrieNode root;
    private int wordCount;

    public Trie() {
        this.root      = new TrieNode();
        this.wordCount = 0;
    }

    // ── INSERT ──────────────────────────────────────────────
    public void insert(OttomanWord word) {
        if (word == null || word.getOttoman() == null || word.getOttoman().isEmpty()) return;

        String key     = word.getOttoman().toLowerCase();
        TrieNode cur   = root;

        for (char c : key.toCharArray()) {
            if (!cur.hasChild(c)) cur.addChild(c, new TrieNode());
            cur = cur.getChild(c);
        }

        if (!cur.isEndOfWord()) {
            cur.setEndOfWord(true);
            cur.setWordData(word);
            wordCount++;
        }
    }

    // ── SEARCH (tam kelime) ──────────────────────────────────
    public OttomanWord search(String key) {
        if (key == null || key.isEmpty()) return null;
        TrieNode node = getNode(key.toLowerCase());
        return (node != null && node.isEndOfWord()) ? node.getWordData() : null;
    }

    public boolean contains(String key) { return search(key) != null; }

    // ── PREFIX SEARCH (autocomplete) ────────────────────────
    public List<OttomanWord> searchByPrefix(String prefix) {
        List<OttomanWord> results = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) {
            collectAll(root, results);
            Collections.sort(results);
            return results;
        }
        TrieNode prefixNode = getNode(prefix.toLowerCase());
        if (prefixNode == null) return results;
        collectWordsFromNode(prefixNode, results);
        Collections.sort(results);
        return results;
    }

    // ── MEANING SEARCH ──────────────────────────────────────
    public List<OttomanWord> searchInMeaning(String keyword) {
        List<OttomanWord> all    = getAllWords();
        List<OttomanWord> result = new ArrayList<>();
        String lower = keyword.toLowerCase();
        for (OttomanWord w : all)
            if (w.getMeaning().toLowerCase().contains(lower)) result.add(w);
        Collections.sort(result);
        return result;
    }

    // ── CATEGORY SEARCH ─────────────────────────────────────
    public List<OttomanWord> searchByCategory(String category) {
        List<OttomanWord> all    = getAllWords();
        List<OttomanWord> result = new ArrayList<>();
        for (OttomanWord w : all)
            if (w.getCategory().equalsIgnoreCase(category)) result.add(w);
        Collections.sort(result);
        return result;
    }

    // ── DELETE ──────────────────────────────────────────────
    public boolean delete(String key) {
        if (!contains(key)) return false;
        deleteHelper(root, key.toLowerCase(), 0);
        wordCount--;
        return true;
    }

    private boolean deleteHelper(TrieNode cur, String key, int idx) {
        if (idx == key.length()) {
            if (!cur.isEndOfWord()) return false;
            cur.setEndOfWord(false);
            cur.setWordData(null);
            return !cur.hasChildren();
        }
        char c = key.charAt(idx);
        if (!cur.hasChild(c)) return false;
        TrieNode child = cur.getChild(c);
        boolean shouldDelete = deleteHelper(child, key, idx + 1);
        if (shouldDelete) {
            cur.getChildren().remove(c);
            return !cur.isEndOfWord() && !cur.hasChildren();
        }
        return false;
    }

    // ── HELPERS ─────────────────────────────────────────────
    private TrieNode getNode(String key) {
        TrieNode cur = root;
        for (char c : key.toCharArray()) {
            if (!cur.hasChild(c)) return null;
            cur = cur.getChild(c);
        }
        return cur;
    }

    private void collectWordsFromNode(TrieNode node, List<OttomanWord> results) {
        if (node.isEndOfWord() && node.getWordData() != null) results.add(node.getWordData());
        for (TrieNode child : node.getChildren().values()) collectWordsFromNode(child, results);
    }

    private void collectAll(TrieNode node, List<OttomanWord> results) {
        collectWordsFromNode(node, results);
    }

    public List<OttomanWord> getAllWords()  { return searchByPrefix(""); }
    public int  getWordCount()             { return wordCount; }
    public boolean isEmpty()               { return wordCount == 0; }

    public String getStats() {
        List<OttomanWord> all = getAllWords();
        int total = 0;
        for (OttomanWord w : all) total += w.getOttoman().length();
        double avg = all.isEmpty() ? 0 : (double) total / all.size();
        return String.format("Toplam: %d kelime  |  Ort. uzunluk: %.1f harf", wordCount, avg);
    }
}