import java.util.Map;
import java.util.TreeMap;

/**
 * Trie veri yapısının her bir düğümünü temsil eder.
 */
public class TrieNode {

    // Doğal alfabetik sıralama için HashMap yerine TreeMap kullanıyoruz
    private Map<Character, TrieNode> children;
    private boolean isEndOfWord;
    private OttomanWord wordData;

    public TrieNode() {
        this.children    = new TreeMap<>();
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