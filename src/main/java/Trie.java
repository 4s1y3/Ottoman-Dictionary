import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Trie {

    private final TrieNode root;
    private int wordCount;
    private final Locale trLocale = new Locale("tr", "TR");

    public Trie() {
        this.root      = new TrieNode();
        this.wordCount = 0;
    }


    public void insert(OttomanWord word) {
        if (word == null || word.getOttoman() == null || word.getOttoman().isEmpty()) return;

        // Türkçe karakter uyumlu küçük harf çevirimi
        String key     = word.getOttoman().toLowerCase(trLocale);
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


    public OttomanWord search(String key) {
        if (key == null || key.isEmpty()) return null;
        TrieNode node = getNode(key.toLowerCase(trLocale));
        return (node != null && node.isEndOfWord()) ? node.getWordData() : null;
    }

    public boolean contains(String key) { return search(key) != null; }


    public List<OttomanWord> searchByPrefix(String prefix) {
        List<OttomanWord> results = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) {
            collectAll(root, results);
            return results;
        }

        TrieNode prefixNode = getNode(prefix.toLowerCase(trLocale));
        if (prefixNode == null) return results;

        collectWordsFromNode(prefixNode, results);
        return results;
    }


    public List<OttomanWord> searchInMeaning(String keyword) {
        List<OttomanWord> all    = getAllWords();
        List<OttomanWord> result = new ArrayList<>();
        String lower = keyword.toLowerCase(trLocale);

        for (OttomanWord w : all) {
            if (w.getMeaning().toLowerCase(trLocale).contains(lower)) {
                result.add(w);
            }
        }
        return result;
    }


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


    public List<OttomanWord> getAllWords() { return searchByPrefix(""); }
    public int getWordCount()              { return wordCount; }
    public boolean isEmpty()               { return wordCount == 0; }



    public TrieNode getRoot()              { return root; }
}