import java.util.HashMap;
import java.util.Map;

/**
 * Trie veri yapısının her bir düğümünü temsil eder.
 * Her düğüm: çocuk düğümler haritası, kelime bitiş bayrağı ve kelime verisi içerir.
 */
public class TrieNode {

    private Map<Character, TrieNode> children; // harf → çocuk düğüm
    private boolean isEndOfWord;               // bu düğümde bir kelime bitiyor mu?
    private OttomanWord wordData;              // biten kelimenin tam verisi

    public TrieNode() {
        this.children    = new HashMap<>();
        this.isEndOfWord = false;
        this.wordData    = null;
    }

    public Map<Character, TrieNode> getChildren()        { return children; }
    public boolean hasChild(char c)                       { return children.containsKey(c); }
    public TrieNode getChild(char c)                      { return children.get(c); }
    public void addChild(char c, TrieNode node)           { children.put(c, node); }
    public boolean isEndOfWord()                          { return isEndOfWord; }
    public void setEndOfWord(boolean endOfWord)           { this.isEndOfWord = endOfWord; }
    public OttomanWord getWordData()                      { return wordData; }
    public void setWordData(OttomanWord wordData)         { this.wordData = wordData; }
    public boolean hasChildren()                          { return !children.isEmpty(); }
}