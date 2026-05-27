import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class OttomanDictionaryApp extends Application {

    // ═══════════════════════════════════════════════════
    // RENK PALETİ
    // ═══════════════════════════════════════════════════
    private static String C_INK, C_PANEL, C_CARD, C_HOVER, C_SEL;
    private static String C_GOLD, C_GOLD_LT, C_IVORY, C_PARCHMENT, C_FADED;
    private static String C_BORDER, C_DIVIDER;
    private static String T_BG, T_GRID, T_NODE, T_NODE_HL, T_NODE_END, T_NODE_PATH;
    private static String T_LINE, T_LINE_HL, T_TEXT, T_TEXT_HL;

    private static final String O_ARABIC  = "#8B6914";
    private static final String O_PERSIAN = "#6B4E71";
    private static final String O_TURKISH = "#2E5E4E";
    private static final String O_GREEK   = "#4A5E7A";
    private static final String O_OTHER   = "#5A4A3A";

    private static final Map<String, String> CAT_COLORS;
    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Devlet", "#5A7A8A");
        map.put("Din", "#7A5A8A");
        map.put("Edebiyat", "#5A8A6A");
        map.put("Hukuk", "#8A7A4A");
        map.put("Felsefe", "#7A4A4A");
        map.put("Askerlik", "#6A6A5A");
        map.put("Bilim", "#4A7A6A");
        map.put("Sanat", "#8A6A4A");
        map.put("Coğrafya", "#5A7A5A");
        map.put("Müzik", "#7A5A7A");
        map.put("Tıp", "#4A7A4A");
        map.put("Ticaret", "#8A6A3A");
        map.put("İnsan", "#7A5A5A");
        map.put("Doğa", "#4A6A4A");
        CAT_COLORS = java.util.Collections.unmodifiableMap(map);
    }

    // ═══════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════
    private boolean isLightMode = true;
    private Scene mainScene;

    private Trie trie;
    private String[] categories;
    private OttomanWord currentWord = null;
    private String currentTriePrefix = "";

    private final LinkedList<String> searchHistory  = new LinkedList<>();
    private final Set<String>        favorites       = new LinkedHashSet<>();

    // Animasyon state
    private Timeline traverseAnim   = null;
    private int      animStep        = 0;
    private String   animTargetPath  = "";

    // Sidebar
    private TextField searchField;
    private ComboBox<String> categoryCombo;
    private ToggleGroup modeGroup;
    private ListView<OttomanWord> wordList;
    private ObservableList<OttomanWord> wordItems;
    private Label resultCountLbl, complexityLbl;
    private HBox historyRow;
    private ToggleButton favFilterBtn;

    // Detail
    private StackPane detailStack;
    private VBox detailRoot, emptyState;
    private Label lbOttoman, lbArabic, lbMeaning, lbExample, lbTrieInfo;
    private Label lbOrigin, lbCategory;
    private Button favBtn;
    private VBox similarFlowWrap, derivedFlowWrap;
    private ScrollPane detailScroll;

    // Trie paneli sekmeleri
    private Canvas trieCanvas;
    private GraphicsContext gc;
    private Label triePrefixLbl, trieStatsLbl;
    private VBox statsPanel;
    private VBox addWordPanel;
    private StackPane trieContentStack;

    // Trie sabitleri
    private static final double NR   = 19;
    private static final double XGAP = 112;
    private static final double YGAP = 42;
    private static final double PL   = 72;
    private static final double PT   = 52;

    // Düğüm pozisyon kaydı (tıklama için)
    private final List<double[]> drawnNodes = new ArrayList<>();
    private final List<String>   drawnPaths = new ArrayList<>();

    // Animasyon için vurgulanan düğümler
    private final Set<String> animHighlightPaths = new LinkedHashSet<>();

    // ═══════════════════════════════════════════════════
    // TEMA
    // ═══════════════════════════════════════════════════
    private void applyTheme() {
        if (isLightMode) {
            C_INK       = "#F7F3EC"; C_PANEL    = "#FFFFFF"; C_CARD   = "#FDFCF9";
            C_HOVER     = "#EFE9DF"; C_SEL      = "#E8DFD0";
            C_GOLD      = "#9B2B2B"; C_GOLD_LT  = "#C04040";
            C_IVORY     = "#1A1A1A"; C_PARCHMENT= "#3D3530"; C_FADED  = "#7A7065";
            C_BORDER    = "#DDD5C5"; C_DIVIDER  = "#EAE2D3";
            T_BG        = "#FAFAF8"; T_GRID     = "#EBEBEB";
            T_NODE      = "#E8E2D8"; T_NODE_HL  = "#9B2B2B";
            T_NODE_END  = "#4A8A6A"; T_NODE_PATH= "#F5DFD0";
            T_LINE      = "#C8BEA8"; T_LINE_HL  = "#9B2B2B";
            T_TEXT      = "#3A3028"; T_TEXT_HL  = "#FFFFFF";
        } else {
            C_INK       = "#0A111E"; C_PANEL    = "#111928"; C_CARD   = "#192436";
            C_HOVER     = "#233147"; C_SEL      = "#2D3E59";
            C_GOLD      = "#D4AF37"; C_GOLD_LT  = "#E5C86C";
            C_IVORY     = "#F1F5F9"; C_PARCHMENT= "#94A3B8"; C_FADED  = "#64748B";
            C_BORDER    = "#2A3950"; C_DIVIDER  = "#1E2B40";
            T_BG        = "#070C15"; T_GRID     = "#0F1820";
            T_NODE      = "#192436"; T_NODE_HL  = "#D4AF37";
            T_NODE_END  = "#3B7A5B"; T_NODE_PATH= "#2A3420";
            T_LINE      = "#253347"; T_LINE_HL  = "#D4AF37";
            T_TEXT      = "#94A3B8"; T_TEXT_HL  = "#0A111E";
        }
    }

    private void toggleTheme() {
        String q = searchField != null ? searchField.getText() : "";
        OttomanWord sel = wordList != null ? wordList.getSelectionModel().getSelectedItem() : null;
        isLightMode = !isLightMode;
        applyTheme();
        buildUI();
        if (searchField != null) searchField.setText(q);
        performSearch();
        if (sel != null) selectWordInList(sel);
    }

    // ═══════════════════════════════════════════════════
    // START
    // ═══════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        applyTheme();
        List<OttomanWord> words = DictionaryLoader.loadFromCSV("data/sozluk.csv");
        categories = DictionaryLoader.getCategoriesFromWords(words);
        trie = new Trie();
        for (OttomanWord w : words) trie.insert(w);

        mainScene = new Scene(new Region(), 1480, 960);
        mainScene.setOnKeyPressed(e -> handleKey(e));
        mainScene.widthProperty().addListener((o, ov, nv) -> {
            if (trieCanvas != null) { trieCanvas.setWidth(Math.max(nv.doubleValue() - 20, 1200)); redrawTrie(); }
        });

        buildUI();
        stage.setTitle("Osmanlıca Sözlük");
        stage.setScene(mainScene);
        stage.show();
    }

    private void buildUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_INK + ";");
        root.setTop(buildHeader());

        SplitPane hSplit = new SplitPane();
        hSplit.setStyle("-fx-background-color:" + C_INK + "; -fx-border-width:0; -fx-padding:0;");
        hSplit.getItems().addAll(buildSidebar(), buildDetailPanel());
        hSplit.setDividerPositions(0.265);

        SplitPane vSplit = new SplitPane();
        vSplit.setStyle("-fx-background-color:" + C_INK + "; -fx-border-width:0;");
        vSplit.setOrientation(Orientation.VERTICAL);
        vSplit.getItems().addAll(hSplit, buildTriePanel());
        vSplit.setDividerPositions(0.55);

        root.setCenter(vSplit);
        mainScene.setRoot(root);
        mainScene.setFill(Color.web(C_INK));
        loadAll();
        if (!wordItems.isEmpty()) wordList.getSelectionModel().select(0);
    }

    // ═══════════════════════════════════════════════════
    // KLAVYE
    // ═══════════════════════════════════════════════════
    private void handleKey(KeyEvent e) {
        switch (e.getCode()) {
            case SLASH  -> { if (!searchField.isFocused()) { searchField.requestFocus(); e.consume(); } }
            case ESCAPE -> { if (searchField.isFocused()) { searchField.clear(); searchField.getParent().requestFocus(); } }
            case UP     -> { if (!searchField.isFocused()) { int i = wordList.getSelectionModel().getSelectedIndex(); if (i>0) wordList.getSelectionModel().select(i-1); e.consume(); } }
            case DOWN   -> { if (!searchField.isFocused()) { int i = wordList.getSelectionModel().getSelectedIndex(); if (i<wordItems.size()-1) wordList.getSelectionModel().select(i+1); e.consume(); } }
            case F      -> { if (e.isControlDown() && currentWord != null) toggleFavorite(currentWord.getOttoman()); }
            default     -> {}
        }
    }

    // ═══════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════
    private HBox buildHeader() {
        HBox h = new HBox(14);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12, 24, 12, 24));
        h.setStyle("-fx-background-color:" + C_PANEL + "; -fx-border-color:" + C_GOLD + "; -fx-border-width:0 0 2 0;");

        Canvas ico = drawOttomanIcon();

        VBox titles = new VBox(2);
        Label t1 = new Label("Osmanlıca Sözlük");
        t1.setStyle("-fx-font-size:21; -fx-font-weight:bold; -fx-font-family:Georgia; -fx-text-fill:" + C_IVORY + ";");
        Label t2 = new Label("Trie (Ön Ek Ağacı) Veri Yapısı — Prefix Arama Motoru");
        t2.setStyle("-fx-font-size:10; -fx-text-fill:" + C_FADED + ";");
        titles.getChildren().addAll(t1, t2);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Karmaşıklık göstergesi — header'da küçük chip
        complexityLbl = new Label("O(m) · m=0");
        complexityLbl.setStyle(
                "-fx-background-color:" + C_GOLD + "18; -fx-text-fill:" + C_GOLD + ";" +
                        "-fx-border-color:" + C_GOLD + "40; -fx-border-radius:4; -fx-background-radius:4;" +
                        "-fx-font-size:11; -fx-font-family:monospace; -fx-padding:4 10;"
        );

        Button themeBtn = new Button(isLightMode ? "🌙 Koyu Mod" : "☀️ Açık Mod");
        themeBtn.setStyle(
                "-fx-background-color:" + C_CARD + "; -fx-text-fill:" + C_GOLD + ";" +
                        "-fx-border-color:" + C_BORDER + "; -fx-border-radius:5; -fx-background-radius:5;" +
                        "-fx-font-size:11; -fx-cursor:hand; -fx-padding:6 14;"
        );
        themeBtn.setOnAction(e -> toggleTheme());

        HBox stats = new HBox(7);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
                makeStatChip(String.valueOf(trie.getWordCount()), "kelime", C_GOLD),
                makeStatChip(String.valueOf(categories.length - 1), "kategori", C_PARCHMENT)
        );

        h.getChildren().addAll(ico, titles, sp, complexityLbl, themeBtn, makeSep(), stats);
        return h;
    }

    private Canvas drawOttomanIcon() {
        Canvas c = new Canvas(46, 46);
        GraphicsContext g = c.getGraphicsContext2D();

        // Dış daire
        g.setStroke(Color.web(C_GOLD)); g.setLineWidth(1.8);
        g.strokeOval(2, 2, 42, 42);
        g.setFill(Color.web(C_GOLD + "18")); g.fillOval(2, 2, 42, 42);

        // Hilal
        g.setFill(Color.web(C_GOLD));
        g.fillOval(8, 13, 20, 20);
        g.setFill(Color.web(C_PANEL));
        g.fillOval(13, 11, 20, 20);

        // Yıldız
        g.setFill(Color.web(C_GOLD));
        double cx = 31, cy = 21.5, ro = 5.0, ri = 2.0;
        double[] xs = new double[10], ys = new double[10];
        double ang = -Math.PI / 2;
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? ro : ri;
            xs[i] = cx + Math.cos(ang) * r; ys[i] = cy + Math.sin(ang) * r;
            ang += Math.PI / 5;
        }
        g.fillPolygon(xs, ys, 10);
        return c;
    }

    private Label makeSep() {
        Label l = new Label("|"); l.setStyle("-fx-text-fill:" + C_DIVIDER + "; -fx-padding:0 3;"); return l;
    }

    private HBox makeStatChip(String val, String lbl, String color) {
        HBox b = new HBox(5); b.setAlignment(Pos.CENTER); b.setPadding(new Insets(5, 12, 5, 12));
        b.setStyle("-fx-background-color:" + C_CARD + "; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-background-radius:6;");
        Label vl = new Label(val); vl.setStyle("-fx-font-size:14; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
        Label ll = new Label(lbl); ll.setStyle("-fx-font-size:10; -fx-text-fill:" + C_FADED + ";");
        b.getChildren().addAll(vl, ll); return b;
    }

    // ═══════════════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox box = new VBox(0);
        box.setMinWidth(285);
        box.setStyle("-fx-background-color:" + C_PANEL + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 1 0 0;");

        VBox searchArea = new VBox(8);
        searchArea.setPadding(new Insets(14, 12, 10, 12));
        searchArea.setStyle("-fx-background-color:" + C_PANEL + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");

        searchField = new TextField();
        searchField.setPromptText("Kelime veya anlam ara...  [ / ]");
        searchField.setStyle(inputStyle(false));
        searchField.focusedProperty().addListener((o, ov, nv) -> searchField.setStyle(inputStyle(nv)));

        HBox modeRow = new HBox(6);
        modeGroup = new ToggleGroup();
        ToggleButton tbW = makeToggle("Kelime Ara", true);
        ToggleButton tbM = makeToggle("Anlam Ara", false);
        tbW.setToggleGroup(modeGroup); tbM.setToggleGroup(modeGroup);
        modeRow.getChildren().addAll(tbW, tbM);
        HBox.setHgrow(tbW, Priority.ALWAYS); HBox.setHgrow(tbM, Priority.ALWAYS);
        tbW.setMaxWidth(Double.MAX_VALUE); tbM.setMaxWidth(Double.MAX_VALUE);

        HBox filterRow = new HBox(6); filterRow.setAlignment(Pos.CENTER_LEFT);
        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(categories));
        categoryCombo.setValue("Tümü");
        categoryCombo.setStyle(comboStyle());
        HBox.setHgrow(categoryCombo, Priority.ALWAYS);
        categoryCombo.setMaxWidth(Double.MAX_VALUE);

        favFilterBtn = new ToggleButton("★");
        favFilterBtn.setStyle(iconToggleStyle(false));
        favFilterBtn.selectedProperty().addListener((o, ov, nv) -> { favFilterBtn.setStyle(iconToggleStyle(nv)); performSearch(); });
        filterRow.getChildren().addAll(categoryCombo, favFilterBtn);

        resultCountLbl = new Label();
        resultCountLbl.setStyle("-fx-font-size:10; -fx-text-fill:" + C_FADED + ";");

        historyRow = new HBox(6);
        historyRow.setAlignment(Pos.CENTER_LEFT);
        historyRow.setPadding(new Insets(1, 0, 0, 0));

        searchArea.getChildren().addAll(searchField, modeRow, filterRow, historyRow, resultCountLbl);

        wordItems = FXCollections.observableArrayList();
        wordList  = new ListView<>(wordItems);
        wordList.setStyle("-fx-background-color:" + C_PANEL + "; -fx-border-width:0; -fx-padding:3 0 3 0;");
        VBox.setVgrow(wordList, Priority.ALWAYS);

        wordList.setCellFactory(lv -> new ListCell<OttomanWord>() {            final Rectangle accent  = new Rectangle(3, 60);
            final Label nmLbl       = new Label();
            final Label mnLbl       = new Label();
            final Label catBadge    = new Label();
            final Label favStar     = new Label("★");
            final HBox  row         = new HBox(0);
            final VBox  content     = new VBox(3);
            {
                accent.setArcWidth(2); accent.setArcHeight(2);
                HBox bottom = new HBox(6, catBadge, favStar);
                bottom.setAlignment(Pos.CENTER_LEFT);
                content.getChildren().addAll(nmLbl, mnLbl, bottom);
                content.setPadding(new Insets(9, 12, 9, 10));
                HBox.setHgrow(content, Priority.ALWAYS);
                row.getChildren().addAll(accent, content);
                setGraphic(row);
                row.setOnMouseEntered(e -> { if (!isSelected()) paint(false, true); });
                row.setOnMouseExited(e  -> { if (!isSelected()) paint(false, false); });
            }

            void paint(boolean sel, boolean hov) {
                OttomanWord w = getItem(); if (w == null) return;
                String cc = getCatColor(w.getCategory());
                String oc = getOriginColor(w.getOrigin());
                accent.setFill(sel ? Color.web(C_GOLD) : hov ? Color.web(oc + "CC") : Color.TRANSPARENT);
                nmLbl.setStyle("-fx-font-size:14; -fx-font-weight:bold; -fx-font-family:Georgia; -fx-text-fill:" + (sel ? C_GOLD_LT : C_IVORY) + ";");
                mnLbl.setStyle("-fx-font-size:11; -fx-text-fill:" + (sel ? C_PARCHMENT : C_FADED) + ";");
                catBadge.setStyle("-fx-font-size:10; -fx-padding:1 7; -fx-background-radius:3; -fx-background-color:" + cc + "28; -fx-text-fill:" + cc + ";");
                favStar.setStyle("-fx-font-size:11; -fx-text-fill:" + C_GOLD + ";");
                favStar.setVisible(favorites.contains(w.getOttoman()));
                row.setStyle("-fx-background-color:" + (sel ? C_SEL : hov ? C_HOVER : "transparent") + "; -fx-cursor:hand;");
                setStyle("-fx-background-color:transparent; -fx-padding:0;");
            }

            @Override
            protected void updateItem(OttomanWord w, boolean empty) {
                super.updateItem(w, empty);
                if (empty || w == null) { setGraphic(null); setStyle("-fx-background-color:transparent;"); return; }
                nmLbl.setText(w.getOttoman());
                String mn = w.getMeaning();
                mnLbl.setText(mn.length() > 40 ? mn.substring(0, 37) + "…" : mn);
                catBadge.setText(w.getCategory());
                paint(isSelected(), false);
                setGraphic(row);
            }
        });

        wordList.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            if (sel != null) { showDetail(sel); startTraverseAnimation(sel.getOttoman()); wordList.refresh(); }
        });
        searchField.textProperty().addListener((o, ov, nv) -> {
            performSearch();
            if (nv != null && nv.length() >= 2) addToHistory(nv.trim());
        });
        categoryCombo.valueProperty().addListener((o, ov, nv) -> performSearch());
        modeGroup.selectedToggleProperty().addListener((o, ov, nv) -> performSearch());

        box.getChildren().addAll(searchArea, wordList);
        return box;
    }

    // ═══════════════════════════════════════════════════
    // DETAIL PANEL
    // ═══════════════════════════════════════════════════
    private StackPane buildDetailPanel() {
        detailStack = new StackPane();
        detailStack.setStyle("-fx-background-color:" + C_INK + ";");

        emptyState = new VBox(14); emptyState.setAlignment(Pos.CENTER);
        Canvas mot = drawLargeMotif();
        Label eT = new Label("Bir kelime seçin");
        eT.setStyle("-fx-font-size:22; -fx-font-weight:bold; -fx-text-fill:" + C_PARCHMENT + "; -fx-font-family:Georgia;");
        Label eS = new Label("Sol listeden seçin veya  /  ile arama yapın");
        eS.setStyle("-fx-font-size:12; -fx-text-fill:" + C_FADED + ";");
        emptyState.getChildren().addAll(mot, eT, eS);

        detailRoot = new VBox(0);
        detailRoot.setVisible(false);
        detailRoot.setStyle("-fx-background-color:" + C_INK + ";");

        // Header
        VBox hdrSec = new VBox(12);
        hdrSec.setPadding(new Insets(28, 40, 22, 40));
        hdrSec.setStyle("-fx-background-color:" + C_PANEL + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");

        HBox nameRow = new HBox(14); nameRow.setAlignment(Pos.BOTTOM_LEFT);
        VBox nameBlock = new VBox(6);
        HBox.setHgrow(nameBlock, Priority.ALWAYS);
        lbOttoman = new Label();
        lbOttoman.setStyle("-fx-font-size:50; -fx-font-weight:bold; -fx-font-family:Georgia; -fx-text-fill:" + C_IVORY + ";");
        lbArabic  = new Label();
        lbArabic.setStyle("-fx-font-size:26; -fx-text-fill:" + C_GOLD + "; -fx-font-family:'Arial Unicode MS';");
        nameBlock.getChildren().addAll(lbOttoman, lbArabic);

        VBox actionBlock = new VBox(7); actionBlock.setAlignment(Pos.TOP_RIGHT);
        favBtn = new Button("☆  Favoriye Ekle");
        favBtn.setStyle(favBtnStyle(false));
        favBtn.setOnMouseClicked(e -> { if (currentWord != null) toggleFavorite(currentWord.getOttoman()); });
        Button btnCopy = new Button("⎘  Kopyala");
        btnCopy.setStyle(copyStyle(false));
        btnCopy.setOnMouseEntered(e -> btnCopy.setStyle(copyStyle(true)));
        btnCopy.setOnMouseExited(e  -> btnCopy.setStyle(copyStyle(false)));
        btnCopy.setOnAction(e -> {
            if (currentWord != null) {
                ClipboardContent cc = new ClipboardContent();
                cc.putString(currentWord.getOttoman() + " → " + currentWord.getMeaning());
                Clipboard.getSystemClipboard().setContent(cc);
                btnCopy.setText("✓  Kopyalandı");
                PauseTransition pt = new PauseTransition(Duration.seconds(2));
                pt.setOnFinished(ev -> btnCopy.setText("⎘  Kopyala"));
                pt.play();            }
        });
        actionBlock.getChildren().addAll(favBtn, btnCopy);
        nameRow.getChildren().addAll(nameBlock, actionBlock);

        HBox tagRow = new HBox(10); tagRow.setAlignment(Pos.CENTER_LEFT);
        lbOrigin = new Label(); lbCategory = new Label(); lbTrieInfo = new Label();
        lbTrieInfo.setStyle("-fx-font-size:11; -fx-text-fill:" + C_FADED + "; -fx-padding:0 0 0 6;");
        tagRow.getChildren().addAll(lbOrigin, lbCategory, lbTrieInfo);
        hdrSec.getChildren().addAll(nameRow, tagRow);

        // Body
        VBox bodySec = new VBox(16); bodySec.setPadding(new Insets(24, 40, 32, 40));

        VBox meaningCard = makeCard(C_GOLD, "ANLAM");
        lbMeaning = new Label();
        lbMeaning.setStyle("-fx-font-size:16; -fx-text-fill:" + C_IVORY + "; -fx-wrap-text:true; -fx-line-spacing:0.3em;");
        lbMeaning.setWrapText(true);
        meaningCard.getChildren().add(lbMeaning);

        VBox exCard = makeCard(C_PARCHMENT, "ÖRNEK KULLANIM");
        lbExample = new Label();
        lbExample.setStyle("-fx-font-size:14; -fx-text-fill:" + C_PARCHMENT + "; -fx-font-style:italic; -fx-wrap-text:true;");
        lbExample.setWrapText(true);
        exCard.getChildren().add(lbExample);

        VBox derivedCard = makeCard(C_GOLD_LT, "BU ÖNEKTEN TÜREYENLER  —  Trie Traversal");
        derivedFlowWrap = new VBox(0);
        derivedCard.getChildren().add(derivedFlowWrap);

        VBox simCard = makeCard(C_PARCHMENT, "AYNI ÖNEKE SAHİP DİĞER KELİMELER");
        similarFlowWrap = new VBox(0);
        simCard.getChildren().add(similarFlowWrap);

        bodySec.getChildren().addAll(meaningCard, exCard, derivedCard, simCard);

        VBox scrollContent = new VBox(0, hdrSec, bodySec);
        detailScroll = new ScrollPane(scrollContent);
        detailScroll.setFitToWidth(true);
        detailScroll.setStyle("-fx-background-color:" + C_INK + "; -fx-background:" + C_INK + "; -fx-border-width:0;");
        VBox.setVgrow(detailScroll, Priority.ALWAYS);
        detailRoot.getChildren().add(detailScroll);
        detailStack.getChildren().addAll(emptyState, detailRoot);
        return detailStack;
    }

    // ═══════════════════════════════════════════════════
    // TRİE PANELİ (Sekmeli)
    // ═══════════════════════════════════════════════════
    private VBox buildTriePanel() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color:" + T_BG + "; -fx-border-color:" + C_GOLD + "; -fx-border-width:1 0 0 0;");

        // Başlık + sekmeler
        HBox bar = new HBox(0);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:" + C_INK + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");

        // Sol: İkon + başlık
        HBox left = new HBox(10); left.setAlignment(Pos.CENTER_LEFT); left.setPadding(new Insets(8, 16, 8, 16));
        Label ico = new Label("◈"); ico.setStyle("-fx-text-fill:" + C_GOLD + "; -fx-font-size:14;");
        Label ttl = new Label("TRİE AĞACI  —  Ön Ek Görselleştirmesi");
        ttl.setStyle("-fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:" + C_IVORY + ";");
        left.getChildren().addAll(ico, ttl);

        // Sekmeler
        ToggleGroup tabGroup = new ToggleGroup();
        ToggleButton tabTree  = makeTabBtn("🌳 Ağaç Görünümü", tabGroup, true);
        ToggleButton tabStats = makeTabBtn("📊 İstatistikler",  tabGroup, false);
        ToggleButton tabAdd   = makeTabBtn("➕ Kelime Ekle",    tabGroup, false);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Sağ: legend + istatistikler
        HBox right = new HBox(12); right.setAlignment(Pos.CENTER); right.setPadding(new Insets(8, 16, 8, 8));
        HBox legend = new HBox(12); legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                makeLeg("Normal", T_NODE, false),
                makeLeg("Seçili Yol", T_NODE_HL, false),
                makeLeg("Kelime Sonu", T_NODE_END, true)
        );
        triePrefixLbl = new Label("Tüm ağaç");
        triePrefixLbl.setStyle("-fx-font-size:11; -fx-text-fill:" + C_FADED + "; -fx-padding:0 0 0 10;");
        trieStatsLbl = new Label();
        trieStatsLbl.setStyle("-fx-font-size:11; -fx-text-fill:" + C_FADED + "; -fx-padding:0 0 0 8;");
        right.getChildren().addAll(legend, triePrefixLbl, trieStatsLbl);

        bar.getChildren().addAll(left, tabTree, tabStats, tabAdd, sp, right);

        // İçerik stack
        trieContentStack = new StackPane();
        VBox.setVgrow(trieContentStack, Priority.ALWAYS);

        // --- Sekme 1: Ağaç ---
        trieCanvas = new Canvas(1460, 320);
        gc = trieCanvas.getGraphicsContext2D();
        trieCanvas.setOnMouseClicked(e -> handleTrieClick(e.getX(), e.getY()));
        trieCanvas.setOnMouseMoved(e -> handleTrieHover(e.getX(), e.getY()));
        ScrollPane cScroll = new ScrollPane(trieCanvas);
        cScroll.setStyle("-fx-background-color:" + T_BG + "; -fx-background:" + T_BG + "; -fx-border-width:0;");
        cScroll.setPannable(true);

        // --- Sekme 2: İstatistikler ---
        statsPanel = buildStatsPanel();
        statsPanel.setVisible(false);

        // --- Sekme 3: Kelime Ekle ---
        addWordPanel = buildAddWordPanel();
        addWordPanel.setVisible(false);

        trieContentStack.getChildren().addAll(cScroll, statsPanel, addWordPanel);

        tabTree.selectedProperty().addListener((o, ov, nv) -> {
            if (nv) { cScroll.setVisible(true); statsPanel.setVisible(false); addWordPanel.setVisible(false); redrawTrie(); }
        });
        tabStats.selectedProperty().addListener((o, ov, nv) -> {
            if (nv) { cScroll.setVisible(false); statsPanel.setVisible(true); addWordPanel.setVisible(false); refreshStats(); }
        });
        tabAdd.selectedProperty().addListener((o, ov, nv) -> {
            if (nv) { cScroll.setVisible(false); statsPanel.setVisible(false); addWordPanel.setVisible(true); }
        });

        panel.getChildren().addAll(bar, trieContentStack);
        return panel;
    }

    // ═══════════════════════════════════════════════════
    // İSTATİSTİK PANELİ
    // ═══════════════════════════════════════════════════
    private VBox buildStatsPanel() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color:" + T_BG + ";");
        panel.setPadding(new Insets(24, 32, 24, 32));
        return panel;
    }

    private void refreshStats() {
        statsPanel.getChildren().clear();

        // Metrikleri hesapla
        int totalNodes   = countAllNodes(trie.getRoot());
        int totalWords   = trie.getWordCount();
        int maxDepth     = calcMaxDepth(trie.getRoot(), 0);
        int rootBreadth  = trie.getRoot().getChildren().size();
        double avgLen    = trie.getAllWords().stream().mapToInt(w -> w.getOttoman().length()).average().orElse(0);
        double shareRate = totalWords == 0 ? 0 : (double)(totalNodes - totalWords) / totalNodes * 100;

        // Kategori dağılımı
        Map<String, Long> catDist = trie.getAllWords().stream()
                .collect(Collectors.groupingBy(OttomanWord::getCategory, Collectors.counting()));
        Map<String, Long> originDist = trie.getAllWords().stream()
                .collect(Collectors.groupingBy(OttomanWord::getOrigin, Collectors.counting()));

        // Başlık
        Label title = new Label("TRİE İSTATİSTİKLERİ");
        title.setStyle("-fx-font-size:13; -fx-font-weight:bold; -fx-text-fill:" + C_GOLD + "; -fx-letter-spacing:2;");
        statsPanel.getChildren().add(title);

        // Genel metrik satırı
        HBox metricsRow = new HBox(12);
        metricsRow.setPadding(new Insets(14, 0, 18, 0));
        metricsRow.getChildren().addAll(
                makeMetricCard("Toplam Düğüm",   String.valueOf(totalNodes),           C_GOLD),
                makeMetricCard("Kelime Sayısı",  String.valueOf(totalWords),            C_GOLD_LT),
                makeMetricCard("Max Derinlik",   String.valueOf(maxDepth),              C_PARCHMENT),
                makeMetricCard("Kök Genişliği",  String.valueOf(rootBreadth) + " harf", C_PARCHMENT),
                makeMetricCard("Ort. Uzunluk",   String.format("%.1f", avgLen),         C_FADED),
                makeMetricCard("Paylaşım Oranı", String.format("%.0f%%", shareRate),    C_FADED)
        );
        statsPanel.getChildren().add(metricsRow);

        // İki sütun: Kategori + Köken
        HBox chartsRow = new HBox(20);
        chartsRow.getChildren().addAll(
                buildBarChart("KATEGORİ DAĞILIMI", catDist, CAT_COLORS),
                buildBarChart("KÖKEN DAĞILIMI",    originDist, Map.of(
                        "Arapça", O_ARABIC, "Farsça", O_PERSIAN, "Türkçe", O_TURKISH,
                        "Rumca", O_GREEK, "Diğer", O_OTHER
                ))
        );
        HBox.setHgrow(chartsRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(chartsRow.getChildren().get(1), Priority.ALWAYS);
        statsPanel.getChildren().add(chartsRow);
    }

    private VBox makeMetricCard(String lbl, String val, String color) {
        VBox c = new VBox(4); c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(12, 16, 12, 16));
        c.setStyle("-fx-background-color:" + C_CARD + "; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8;");
        Label v = new Label(val); v.setStyle("-fx-font-size:20; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
        Label l = new Label(lbl); l.setStyle("-fx-font-size:10; -fx-text-fill:" + C_FADED + ";");
        c.getChildren().addAll(v, l);
        return c;
    }

    private VBox buildBarChart(String title, Map<String, Long> data, Map<String, String> colorMap) {
        VBox chart = new VBox(8);
        chart.setPadding(new Insets(14, 18, 14, 18));
        chart.setStyle("-fx-background-color:" + C_CARD + "; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8;");

        Label t = new Label(title);
        t.setStyle("-fx-font-size:10; -fx-font-weight:bold; -fx-text-fill:" + C_GOLD + "; -fx-letter-spacing:1.5;");
        chart.getChildren().add(t);

        long max = data.values().stream().mapToLong(Long::longValue).max().orElse(1);

        data.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
                    Label name = new Label(entry.getKey());
                    name.setMinWidth(80); name.setMaxWidth(80);
                    name.setStyle("-fx-font-size:11; -fx-text-fill:" + C_PARCHMENT + ";");

                    String barColor = colorMap.getOrDefault(entry.getKey(), C_FADED);

                    double ratio = (double) entry.getValue() / max;
                    Rectangle bar = new Rectangle(ratio * 140, 14);
                    bar.setArcWidth(4); bar.setArcHeight(4);
                    bar.setFill(Color.web(barColor));

                    Label cnt = new Label(String.valueOf(entry.getValue()));
                    cnt.setStyle("-fx-font-size:10; -fx-text-fill:" + C_FADED + ";");

                    // Animasyon
                    bar.setWidth(0);
                    Timeline anim = new Timeline(new KeyFrame(Duration.millis(600),
                            new KeyValue(bar.widthProperty(), ratio * 140, Interpolator.EASE_OUT)));
                    anim.setDelay(Duration.millis(50));
                    anim.play();

                    row.getChildren().addAll(name, bar, cnt);
                    chart.getChildren().add(row);
                });

        return chart;
    }

    private int countAllNodes(TrieNode node) {
        int c = 1;
        for (TrieNode ch : node.getChildren().values()) c += countAllNodes(ch);
        return c;
    }

    private int calcMaxDepth(TrieNode node, int depth) {
        if (node.getChildren().isEmpty()) return depth;
        int max = depth;
        for (TrieNode ch : node.getChildren().values())
            max = Math.max(max, calcMaxDepth(ch, depth + 1));
        return max;
    }

    // ═══════════════════════════════════════════════════
    // KELIME EKLEME PANELİ
    // ═══════════════════════════════════════════════════
    private VBox buildAddWordPanel() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(28, 40, 28, 40));
        panel.setStyle("-fx-background-color:" + T_BG + ";");

        Label title = new Label("CANLI KELİME EKLE — Trie'ye Anında Insert");
        title.setStyle("-fx-font-size:13; -fx-font-weight:bold; -fx-text-fill:" + C_GOLD + "; -fx-letter-spacing:2;");

        // Form alanları
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);
        form.setMaxWidth(700);

        TextField[] fields = new TextField[6];
        String[] labels = {"Osmanlıca (Latin)", "Arapça Yazılış", "Anlam", "Köken", "Kategori", "Örnek Cümle"};
        for (int i = 0; i < labels.length; i++) {
            Label lbl = new Label(labels[i]);
            lbl.setStyle("-fx-font-size:11; -fx-text-fill:" + C_PARCHMENT + "; -fx-min-width:130;");
            fields[i] = new TextField();
            fields[i].setStyle(inputStyle(false));
            fields[i].setMinWidth(350);
            form.add(lbl, 0, i);
            form.add(fields[i], 1, i);
        }

        // Ekle butonu
        Button addBtn = new Button("  ➕  Trie'ye Ekle  ");
        addBtn.setStyle(
                "-fx-background-color:" + C_GOLD + "; -fx-text-fill:" + C_INK + ";" +
                        "-fx-font-weight:bold; -fx-font-size:13; -fx-cursor:hand; -fx-padding:10 24;" +
                        "-fx-border-radius:6; -fx-background-radius:6;"
        );

        Label feedbackLbl = new Label();
        feedbackLbl.setStyle("-fx-font-size:12; -fx-text-fill:" + C_FADED + ";");

        addBtn.setOnAction(e -> {
            String ottoman = fields[0].getText().trim();
            String arabic  = fields[1].getText().trim();
            String meaning = fields[2].getText().trim();
            String origin  = fields[3].getText().trim();
            String cat     = fields[4].getText().trim();
            String example = fields[5].getText().trim();

            if (ottoman.isEmpty() || meaning.isEmpty()) {
                feedbackLbl.setText("⚠  Osmanlıca kelime ve anlam zorunludur.");
                feedbackLbl.setStyle("-fx-font-size:12; -fx-text-fill:#C04040;");
                return;
            }

            if (trie.contains(ottoman)) {
                feedbackLbl.setText("⚠  \"" + ottoman + "\" zaten Trie'de mevcut.");
                feedbackLbl.setStyle("-fx-font-size:12; -fx-text-fill:#C04040;");
                return;
            }

            OttomanWord newWord = new OttomanWord(ottoman, arabic, meaning,
                    origin.isEmpty() ? "Belirsiz" : origin,
                    cat.isEmpty() ? "Diğer" : cat, example);
            trie.insert(newWord);

            // Listeyi güncelle
            wordItems.add(newWord);
            wordList.getSelectionModel().select(newWord);
            wordList.scrollTo(newWord);

            // Header kelime sayısını güncelle (yeniden build etmeden)
            feedbackLbl.setText("✓  \"" + ottoman + "\" başarıyla Trie'ye eklendi!  Toplam: " + trie.getWordCount() + " kelime");
            feedbackLbl.setStyle("-fx-font-size:12; -fx-text-fill:" + C_GOLD + ";");

            // Alanları temizle
            for (TextField f : fields) f.clear();

            // Ağaç animasyonu ile göster
            startTraverseAnimation(ottoman);
        });

        panel.getChildren().addAll(title, form, addBtn, feedbackLbl);
        return panel;
    }

    // ═══════════════════════════════════════════════════
    // TRİE TRAVERSE ANİMASYONU — En kritik özellik
    // ═══════════════════════════════════════════════════
    private void startTraverseAnimation(String word) {
        if (traverseAnim != null) { traverseAnim.stop(); }
        animHighlightPaths.clear();
        animStep = 0;
        animTargetPath = word.toLowerCase(new Locale("tr", "TR"));

        // Önce ağacı o kelimenin prefix'i ile çiz (tüm yolu görünür yap)
        drawTrie(animTargetPath);

        // Adım adım vurgulama
        traverseAnim = new Timeline();
        for (int i = 1; i <= animTargetPath.length(); i++) {
            final String stepPath = animTargetPath.substring(0, i);
            KeyFrame kf = new KeyFrame(Duration.millis(i * 220), ev -> {
                animHighlightPaths.add(stepPath);
                drawTrieWithAnimHighlight(animTargetPath, animHighlightPaths);
            });
            traverseAnim.getKeyFrames().add(kf);
        }
        traverseAnim.play();
    }

    // ═══════════════════════════════════════════════════
    // TRİE TIKLAMA
    // ═══════════════════════════════════════════════════
    private void handleTrieClick(double mx, double my) {
        for (int i = 0; i < drawnNodes.size(); i++) {
            double[] n = drawnNodes.get(i);
            if ((mx-n[0])*(mx-n[0]) + (my-n[1])*(my-n[1]) <= NR*NR) {
                String path = drawnPaths.get(i);
                if (path.isEmpty()) return;
                List<OttomanWord> matches = trie.searchByPrefix(path);
                if (matches.isEmpty()) return;
                wordItems.setAll(matches);
                resultCountLbl.setText("\"" + path.toUpperCase(new Locale("tr","TR")) + "\" öneki: " + matches.size() + " kelime");
                wordList.getSelectionModel().select(0);
                if (triePrefixLbl != null)
                    triePrefixLbl.setText("Düğüm: \"" + path.toUpperCase(new Locale("tr","TR")) + "\"  →  " + matches.size() + " kelime");
                drawTrie(path);
                break;
            }
        }
    }

    private void handleTrieHover(double mx, double my) {
        boolean found = drawnNodes.stream().anyMatch(n -> (mx-n[0])*(mx-n[0])+(my-n[1])*(my-n[1]) <= NR*NR);
        trieCanvas.setCursor(found ? Cursor.HAND : Cursor.DEFAULT);
    }

    void selectWordInList(OttomanWord w) {
        for (int i = 0; i < wordItems.size(); i++) {
            if (wordItems.get(i).getOttoman().equalsIgnoreCase(w.getOttoman())) {
                wordList.getSelectionModel().select(i); wordList.scrollTo(i); return;
            }
        }
        loadAll();
        for (int i = 0; i < wordItems.size(); i++) {
            if (wordItems.get(i).getOttoman().equalsIgnoreCase(w.getOttoman())) {
                wordList.getSelectionModel().select(i); wordList.scrollTo(i); return;
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ARAMA & VERİ
    // ═══════════════════════════════════════════════════
    private void loadAll() {
        wordItems.setAll(trie.getAllWords());
        resultCountLbl.setText(trie.getWordCount() + " kelime listeleniyor");
        redrawTrie();
    }

    private void performSearch() {
        String q   = searchField.getText().trim();
        String cat = categoryCombo.getValue();
        Toggle t   = modeGroup.getSelectedToggle();
        boolean byM = t != null && ((ToggleButton) t).getText().equals("Anlam Ara");

        long startNs = System.nanoTime();

        List<OttomanWord> res = byM
                ? (q.isEmpty() ? trie.getAllWords() : trie.searchInMeaning(q))
                : trie.searchByPrefix(q);

        if (cat != null && !cat.equals("Tümü"))
            res = res.stream().filter(w -> w.getCategory().equals(cat)).collect(Collectors.toList());
        if (favFilterBtn != null && favFilterBtn.isSelected())
            res = res.stream().filter(w -> favorites.contains(w.getOttoman())).collect(Collectors.toList());

        long elapsedUs = (System.nanoTime() - startNs) / 1000;

        wordItems.setAll(res);

        // Karmaşıklık güncelle
        int m = q.length();
        if (complexityLbl != null)
            complexityLbl.setText("O(m)  ·  m=" + m + "  ·  " + elapsedUs + "μs");

        if (!q.isEmpty() && !byM)
            resultCountLbl.setText("\"" + q.toUpperCase(new Locale("tr","TR")) + "\" öneki: " + trie.searchByPrefix(q).size() + " kelime");
        else
            resultCountLbl.setText(res.size() + " sonuç");

        if (!res.isEmpty()) wordList.getSelectionModel().select(0);
        else if (!q.isEmpty()) drawTrie(q);
    }

    private void addToHistory(String q) {
        searchHistory.remove(q); searchHistory.addFirst(q);
        if (searchHistory.size() > 5) searchHistory.removeLast();
        refreshHistoryChips();
    }

    private void refreshHistoryChips() {
        historyRow.getChildren().clear();
        if (searchHistory.isEmpty()) return;
        Label l = new Label("Son:"); l.setStyle("-fx-font-size:10; -fx-text-fill:" + C_FADED + ";");
        historyRow.getChildren().add(l);
        for (String h : searchHistory) {
            Label chip = new Label(h);
            chip.setStyle("-fx-background-color:" + C_CARD + "; -fx-text-fill:" + C_PARCHMENT + "; -fx-border-color:" + C_BORDER + "; -fx-border-radius:4; -fx-background-radius:4; -fx-font-size:10; -fx-padding:2 8; -fx-cursor:hand;");
            chip.setOnMouseClicked(e -> { searchField.setText(h); });
            chip.setOnMouseEntered(e -> chip.setStyle(chip.getStyle().replace(C_CARD, C_HOVER)));
            chip.setOnMouseExited(e  -> chip.setStyle(chip.getStyle().replace(C_HOVER, C_CARD)));
            historyRow.getChildren().add(chip);
        }
    }

    private void toggleFavorite(String s) {
        if (favorites.contains(s)) favorites.remove(s); else favorites.add(s);
        wordList.refresh();
        if (currentWord != null && currentWord.getOttoman().equals(s)) updateFavBtn();
    }

    private void updateFavBtn() {
        if (currentWord == null || favBtn == null) return;
        boolean fav = favorites.contains(currentWord.getOttoman());
        favBtn.setText(fav ? "★  Favoriden Çıkar" : "☆  Favoriye Ekle");
        favBtn.setStyle(favBtnStyle(fav));
    }

    // ═══════════════════════════════════════════════════
    // DETAIL GÖSTER
    // ═══════════════════════════════════════════════════
    private void showDetail(OttomanWord w) {
        currentWord = w;
        emptyState.setVisible(false);
        detailRoot.setOpacity(0); detailRoot.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(180), detailRoot);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        lbOttoman.setText(w.getOttoman());
        lbArabic.setText(w.getArabic().isBlank() ? "" : w.getArabic());
        lbMeaning.setText(w.getMeaning());
        styleTag(lbOrigin,   w.getOrigin(),   getOriginColor(w.getOrigin()));
        styleTag(lbCategory, w.getCategory(), getCatColor(w.getCategory()));

        String pfx2 = w.getOttoman().length() >= 2
                ? w.getOttoman().substring(0, 2).toLowerCase(new Locale("tr","TR"))
                : w.getOttoman().toLowerCase(new Locale("tr","TR"));
        lbTrieInfo.setText("Derinlik: " + w.getOttoman().length() + "  ·  Bu önekten: " + trie.searchByPrefix(pfx2).size() + " kelime");

        String ex = w.getExample();
        lbExample.setText(ex == null || ex.isBlank() ? "Kayıtlı örnek cümle bulunmamaktadır." : ex);
        lbExample.setStyle("-fx-font-size:14; -fx-text-fill:" + (ex==null||ex.isBlank() ? C_FADED : C_PARCHMENT) + "; -fx-font-style:italic; -fx-wrap-text:true;");

        updateFavBtn();

        // Türeyenler
        derivedFlowWrap.getChildren().clear();
        List<OttomanWord> derived = trie.searchByPrefix(w.getOttoman().toLowerCase(new Locale("tr","TR")))
                .stream().filter(rw -> !rw.getOttoman().equalsIgnoreCase(w.getOttoman())).limit(10).collect(Collectors.toList());
        if (derived.isEmpty()) {
            Label n = new Label("Bu kelimeden başka önek türeyen kelime yok.");
            n.setStyle("-fx-text-fill:" + C_FADED + "; -fx-font-size:12;");
            derivedFlowWrap.getChildren().add(n);
        } else {
            FlowPane fp = new FlowPane(7, 6);
            derived.forEach(d -> fp.getChildren().add(makeWordChip(d, C_GOLD)));
            derivedFlowWrap.getChildren().add(fp);
        }

        // Benzerler
        similarFlowWrap.getChildren().clear();
        List<OttomanWord> similar = trie.searchByPrefix(pfx2)
                .stream().filter(rw -> !rw.getOttoman().equalsIgnoreCase(w.getOttoman())).limit(8).collect(Collectors.toList());
        if (similar.isEmpty()) {
            Label n = new Label("Aynı önekli başka kelime bulunamadı.");
            n.setStyle("-fx-text-fill:" + C_FADED + "; -fx-font-size:12;");
            similarFlowWrap.getChildren().add(n);
        } else {
            FlowPane fp = new FlowPane(7, 6);
            similar.forEach(s -> fp.getChildren().add(makeWordChip(s, getCatColor(s.getCategory()))));
            similarFlowWrap.getChildren().add(fp);
        }
        detailScroll.setVvalue(0);
    }

    // ═══════════════════════════════════════════════════
    // TRİE ÇİZİMİ
    // ═══════════════════════════════════════════════════
    private void redrawTrie() { drawTrie(currentTriePrefix); }

    private void drawTrie(String prefix) {
        currentTriePrefix = prefix == null ? "" : prefix.toLowerCase(new Locale("tr","TR"));
        animHighlightPaths.clear();
        drawTrieWithAnimHighlight(currentTriePrefix, animHighlightPaths);
    }

    private void drawTrieWithAnimHighlight(String target, Set<String> animPaths) {
        if (trieCanvas == null || gc == null) return;
        if (triePrefixLbl != null)
            triePrefixLbl.setText(target.isEmpty() ? "Tüm ağaç görünümü" : "Ön ek: \"" + target.toUpperCase(new Locale("tr","TR")) + "\"");

        double reqH = calcH(trie.getRoot(), "", target);
        reqH = Math.max(reqH, 300);
        trieCanvas.setHeight(reqH + PT + 40);

        gc.setFill(Color.web(T_BG));
        gc.fillRect(0, 0, trieCanvas.getWidth(), trieCanvas.getHeight());

        // Grid
        gc.setStroke(Color.web(T_GRID)); gc.setLineWidth(0.4); gc.setLineDashes(0);
        for (double x = 0; x < trieCanvas.getWidth(); x += 30) gc.strokeLine(x, 0, x, trieCanvas.getHeight());
        for (double y = 0; y < trieCanvas.getHeight(); y += 30) gc.strokeLine(0, y, trieCanvas.getWidth(), y);

        drawnNodes.clear(); drawnPaths.clear();

        double rootX = PL, rootY = PT + reqH / 2.0;
        paintEdges(trie.getRoot(), rootX, PT, PT + reqH, "", target, animPaths);
        paintNodes(trie.getRoot(), rootX, PT, PT + reqH, "", target, animPaths);
        paintNode(rootX, rootY, "KÖK", true, false, true, "");

        if (trieStatsLbl != null) {
            int vis = countVisible(trie.getRoot(), "", target);
            trieStatsLbl.setText("Düğüm: " + vis + (target.isEmpty() ? "" : "  ·  Derinlik: " + target.length()));
        }
    }

    private double calcH(TrieNode node, String path, String target) {
        if (node.getChildren().isEmpty()) return YGAP;
        if (!target.isEmpty() && !target.startsWith(path) && !path.startsWith(target)) return YGAP;
        double total = 0;
        for (Map.Entry<Character, TrieNode> e : node.getChildren().entrySet())
            total += calcH(e.getValue(), path + e.getKey(), target);
        return Math.max(total, YGAP);
    }

    private int countVisible(TrieNode node, String path, String target) {
        if (!target.isEmpty() && !target.startsWith(path) && !path.startsWith(target)) return 0;
        int c = 1;
        for (Map.Entry<Character, TrieNode> e : node.getChildren().entrySet()) c += countVisible(e.getValue(), path + e.getKey(), target);
        return c;
    }

    private void paintEdges(TrieNode node, double x, double yS, double yE, String path, String target, Set<String> anim) {
        if (node.getChildren().isEmpty()) return;
        if (!target.isEmpty() && !target.startsWith(path) && !path.startsWith(target)) return;
        double pY = yS + (yE - yS) / 2.0, cy = yS;
        for (Map.Entry<Character, TrieNode> e : node.getChildren().entrySet()) {
            String cp = path + e.getKey();
            double ch = calcH(e.getValue(), cp, target), cYE = cy + ch, cY = cy + ch / 2.0;
            double nx = x + XGAP, mx = x + XGAP / 2.0;
            boolean hl   = !target.isEmpty() && (target.startsWith(cp) || cp.startsWith(target));
            boolean aHl  = anim.contains(cp);

            String lineColor = aHl ? C_GOLD_LT : hl ? T_LINE_HL : T_LINE;
            gc.setStroke(Color.web(lineColor));
            gc.setLineWidth(aHl ? 3.0 : hl ? 2.2 : 1.0);
            gc.setLineDashes(hl || aHl ? null : new double[]{4, 6});
            gc.beginPath();
            gc.moveTo(x + NR, pY); gc.lineTo(mx, pY); gc.lineTo(mx, cY); gc.lineTo(nx - NR, cY);
            gc.stroke(); gc.setLineDashes((double[]) null);

            if (hl || aHl) {
                gc.setFill(Color.web(aHl ? C_GOLD_LT : T_LINE_HL));
                double ax = nx - NR;
                gc.fillPolygon(new double[]{ax, ax-8, ax-8}, new double[]{cY, cY-4.5, cY+4.5}, 3);
            }

            paintEdges(e.getValue(), nx, cy, cYE, cp, target, anim);
            cy = cYE;
        }
    }

    private void paintNodes(TrieNode node, double x, double yS, double yE, String path, String target, Set<String> anim) {
        if (node.getChildren().isEmpty()) return;
        if (!target.isEmpty() && !target.startsWith(path) && !path.startsWith(target)) return;
        double cy = yS;
        for (Map.Entry<Character, TrieNode> e : node.getChildren().entrySet()) {
            String cp = path + e.getKey();
            double ch = calcH(e.getValue(), cp, target), cYE = cy + ch, cY = cy + ch / 2.0, nx = x + XGAP;
            boolean hl = !target.isEmpty() && (target.startsWith(cp) || cp.startsWith(target));
            boolean aHl = anim.contains(cp);
            paintNode(nx, cY, String.valueOf(e.getKey()).toUpperCase(new Locale("tr","TR")), hl, e.getValue().isEndOfWord(), false, cp);
            // Animasyon halkası
            if (aHl) {
                gc.setStroke(Color.web(C_GOLD_LT));
                gc.setLineWidth(2.5);
                gc.strokeOval(nx - NR - 5, cY - NR - 5, (NR + 5) * 2, (NR + 5) * 2);
            }
            paintNodes(e.getValue(), nx, cy, cYE, cp, target, anim);
            cy = cYE;
        }
    }

    private void paintNode(double x, double y, String label, boolean hl, boolean end, boolean root, String path) {
        if (!root) { drawnNodes.add(new double[]{x, y}); drawnPaths.add(path); }

        // Gölge
        gc.setFill(Color.color(0, 0, 0, isLightMode ? 0.12 : 0.5));
        gc.fillOval(x - NR + 1, y - NR + 2, NR * 2, NR * 2);

        String fill = root ? C_GOLD : hl && end ? T_NODE_END : hl ? T_NODE_PATH : end ? T_NODE_END + "60" : T_NODE;
        gc.setFill(Color.web(fill));
        gc.fillOval(x - NR, y - NR, NR * 2, NR * 2);

        String stroke = root ? C_GOLD_LT : hl ? T_NODE_HL : end ? T_NODE_END : T_LINE;
        gc.setStroke(Color.web(stroke)); gc.setLineWidth(hl ? 2.2 : 1.2);
        gc.strokeOval(x - NR, y - NR, NR * 2, NR * 2);

        if (end && !root) {
            gc.setStroke(Color.web(T_NODE_END + "AA")); gc.setLineWidth(0.8);
            double ir = NR - 5;
            gc.strokeOval(x - ir, y - ir, ir * 2, ir * 2);
        }

        String tc = root || (hl && !end) ? T_TEXT_HL : T_TEXT;
        gc.setFill(Color.web(tc));
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, root ? 10 : 13));
        gc.fillText(label, x - label.length() * (root ? 5.5 : 7) / 2, y + 5);
    }

    // ═══════════════════════════════════════════════════
    // YARDIMCI METOTLAR
    // ═══════════════════════════════════════════════════
    private ToggleButton makeTabBtn(String text, ToggleGroup g, boolean sel) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(g); tb.setSelected(sel);
        tb.setStyle(tabStyle(sel));
        tb.selectedProperty().addListener((o, ov, nv) -> tb.setStyle(tabStyle(nv)));
        return tb;
    }

    private String tabStyle(boolean sel) {
        return "-fx-background-color:" + (sel ? C_GOLD + "25" : "transparent") + ";" +
                "-fx-text-fill:" + (sel ? C_GOLD : C_FADED) + ";" +
                "-fx-border-color:transparent transparent " + (sel ? C_GOLD : "transparent") + " transparent;" +
                "-fx-border-width:0 0 2 0; -fx-background-radius:0; -fx-font-size:12;" +
                "-fx-cursor:hand; -fx-padding:10 18;";
    }

    private HBox makeLeg(String txt, String color, boolean ring) {
        HBox h = new HBox(5); h.setAlignment(Pos.CENTER);
        Circle ci = new Circle(7, Color.web(color + "50"));
        ci.setStroke(Color.web(color)); ci.setStrokeWidth(ring ? 2 : 1.5);
        Label l = new Label(txt); l.setStyle("-fx-font-size:10; -fx-text-fill:" + C_FADED + ";");
        h.getChildren().addAll(ci, l); return h;
    }

    private VBox makeCard(String accentColor, String title) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color:" + C_CARD + "; -fx-border-radius:8; -fx-background-radius:8; -fx-border-color:" + C_BORDER + "; -fx-border-width:1;");
        Label hdr = new Label(title);
        hdr.setStyle("-fx-font-size:10; -fx-font-weight:bold; -fx-text-fill:" + accentColor + "; -fx-letter-spacing:2;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + C_BORDER + ";");
        card.getChildren().addAll(hdr, sep);
        return card;
    }

    private Canvas drawLargeMotif() {
        Canvas c = new Canvas(80, 80); GraphicsContext g = c.getGraphicsContext2D();
        g.setStroke(Color.web(C_DIVIDER)); g.setLineWidth(1.5); g.strokeOval(5,5,70,70);
        g.setLineWidth(0.8); g.strokeOval(12,12,56,56);
        g.setStroke(Color.web(C_BORDER)); g.setLineWidth(1.5);
        g.strokeLine(40,12,40,68); g.strokeLine(12,40,68,40);
        g.strokeLine(21,21,59,59); g.strokeLine(59,21,21,59);
        g.setFill(Color.web(C_BORDER));
        for (double[] p : new double[][]{{40,12},{40,68},{12,40},{68,40},{21,21},{59,59},{59,21},{21,59}})
            g.fillOval(p[0]-3,p[1]-3,6,6);
        g.setFill(Color.web(C_DIVIDER)); g.fillOval(34,34,12,12);
        return c;
    }

    private Label makeWordChip(OttomanWord w, String color) {
        Label chip = new Label(w.getOttoman());
        chip.setStyle("-fx-background-color:" + color + "18; -fx-text-fill:" + color + "; -fx-border-color:" + color + "50; -fx-border-radius:5; -fx-background-radius:5; -fx-font-size:12; -fx-padding:5 12; -fx-cursor:hand;");
        chip.setOnMouseEntered(e -> chip.setStyle(chip.getStyle().replace(color+"18", color+"32")));
        chip.setOnMouseExited(e  -> chip.setStyle(chip.getStyle().replace(color+"32", color+"18")));
        chip.setOnMouseClicked(e -> selectWordInList(w));
        return chip;
    }

    private void styleTag(Label lbl, String text, String color) {
        lbl.setText("  " + text + "  ");
        lbl.setStyle("-fx-background-color:" + color + "22; -fx-text-fill:" + color + "; -fx-border-color:" + color + "55; -fx-border-radius:4; -fx-background-radius:4; -fx-font-size:12; -fx-padding:4 10;");
    }

    private String inputStyle(boolean f)  { return "-fx-background-color:"+C_CARD+"; -fx-text-fill:"+C_IVORY+"; -fx-prompt-text-fill:"+C_FADED+"; -fx-border-color:"+(f?C_GOLD:C_BORDER)+"; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:9 13; -fx-font-size:12;"; }
    private String comboStyle()           { return "-fx-background-color:"+C_CARD+"; -fx-text-fill:"+C_IVORY+"; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:11;"; }
    private String toggleStyle(boolean s) { return "-fx-background-color:"+(s?C_GOLD:C_CARD)+"; -fx-text-fill:"+(s?C_INK:C_PARCHMENT)+"; -fx-font-weight:"+(s?"bold":"normal")+"; -fx-border-color:"+(s?C_GOLD:C_BORDER)+"; -fx-border-radius:5; -fx-background-radius:5; -fx-font-size:11; -fx-cursor:hand;"; }
    private ToggleButton makeToggle(String t, boolean s) { ToggleButton tb = new ToggleButton(t); tb.setSelected(s); tb.setStyle(toggleStyle(s)); tb.selectedProperty().addListener((o,ov,nv)->tb.setStyle(toggleStyle(nv))); return tb; }
    private String iconToggleStyle(boolean s) { return "-fx-background-color:"+(s?C_GOLD+"30":C_CARD)+"; -fx-text-fill:"+(s?C_GOLD:C_FADED)+"; -fx-border-color:"+(s?C_GOLD:C_BORDER)+"; -fx-border-radius:5; -fx-background-radius:5; -fx-font-size:14; -fx-cursor:hand; -fx-padding:4 10;"; }
    private String favBtnStyle(boolean a) { return "-fx-background-color:"+(a?C_GOLD+"28":C_CARD)+"; -fx-text-fill:"+(a?C_GOLD_LT:C_PARCHMENT)+"; -fx-border-color:"+(a?C_GOLD:C_BORDER)+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:11; -fx-cursor:hand; -fx-padding:7 14;"; }
    private String copyStyle(boolean h)   { return "-fx-background-color:"+(h?C_HOVER:C_CARD)+"; -fx-text-fill:"+(h?C_IVORY:C_PARCHMENT)+"; -fx-border-color:"+(h?C_DIVIDER:C_BORDER)+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:11; -fx-cursor:hand; -fx-padding:7 14;"; }
    private String getCatColor(String c)  { return CAT_COLORS.getOrDefault(c, C_FADED); }
    private String getOriginColor(String o) {
        if (o==null) return O_OTHER; String lo = o.toLowerCase(new Locale("tr","TR"));
        if (lo.contains("arap")) return O_ARABIC; if (lo.contains("fars")||lo.contains("iran")) return O_PERSIAN;
        if (lo.contains("türk")||lo.contains("turk")) return O_TURKISH; if (lo.contains("rum")||lo.contains("grek")) return O_GREEK;
        return O_OTHER;
    }

    public static void main(String[] args) { launch(args); }
}