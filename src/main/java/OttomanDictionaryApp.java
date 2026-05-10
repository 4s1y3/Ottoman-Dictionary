import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * Osmanlıca Sözlük — Trie Veri Yapısı
 *
 * Layout:
 * ┌──────────────────────────────────────────────────────┐
 * │  HEADER                                              │
 * ├──────────┬───────────────────────────────────────────┤
 * │ KELİME   │  DETAY PANELİ                             │
 * │ LİSTESİ  │  (kelime, anlam, köken, örnek, algo tablo)│
 * ├──────────┴───────────────────────────────────────────┤
 * │  TRİE AĞACI — Tam Genişlik, Yatay Hiyerarşik Ağaç   │
 * │  [ROOT → 1.Harf → 2.Harf → ... arama yolu parlak]   │
 * ├──────────────────────────────────────────────────────┤
 * │  STATUS BAR                                          │
 * └──────────────────────────────────────────────────────┘
 */
public class OttomanDictionaryApp extends Application {

    // ═══════════════════════════════════════════════════
    // RENK PALETİ
    // ═══════════════════════════════════════════════════
    private static final String BG      = "#070B12";
    private static final String PANEL   = "#0C1220";
    private static final String CARD    = "#111A2E";
    private static final String CARD2   = "#0E1628";
    private static final String BORDER  = "#1C2E4A";
    private static final String BORDER2 = "#2A4070";

    private static final String TEAL    = "#00F5D4";
    private static final String TEAL2   = "#00B4A0";
    private static final String GOLD    = "#FFD166";
    private static final String ROSE    = "#FF6B9D";
    private static final String VIOLET  = "#A78BFA";
    private static final String AMBER   = "#FB8C00";
    private static final String GREEN   = "#06D6A0";

    private static final String TXT1    = "#E2EAF8";
    private static final String TXT2    = "#7A94B8";
    private static final String TXT3    = "#364760";
    private static final String TXT4    = "#1E2E42";

    // ═══════════════════════════════════════════════════
    // VERİ
    // ═══════════════════════════════════════════════════
    private Trie   trie;
    private String[] categories;

    // ═══════════════════════════════════════════════════
    // UI BİLEŞENLERİ
    // ═══════════════════════════════════════════════════
    private TextField        searchField;
    private ComboBox<String> categoryCombo;
    private ToggleGroup      modeGroup;
    private ListView<OttomanWord> wordList;
    private ObservableList<OttomanWord> wordItems;
    private Label            resultLbl;

    // Detay
    private Label   lbWord, lbArabic, lbMeaning, lbOrigin, lbCat, lbExample;
    private Label   lbRelated;
    private VBox    detailPane;
    private VBox    placeholder;
    private ScrollPane detailScroll;

    // Trie canvas (alta yerleşik)
    private Canvas trieCanvas;
    private GraphicsContext gc;
    private Label  trieTitleLbl;
    private SplitPane mainSplit;

    // ═══════════════════════════════════════════════════
    // START
    // ═══════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        List<OttomanWord> words = DictionaryLoader.loadFromCSV("data/sozluk.csv");
        categories = DictionaryLoader.getCategoriesFromWords(words);
        trie = new Trie();
        for (OttomanWord w : words) trie.insert(w);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG + ";");
        root.setTop(buildHeader());
        root.setBottom(buildStatusBar());

        // Üst kısım: liste + detay (yatay)
        HBox topRow = new HBox(0);
        topRow.getChildren().addAll(buildSidebar(), vSep(), buildDetailPanel());
        HBox.setHgrow(buildDetailPanel(), Priority.ALWAYS);

        // Alt kısım: Trie ağacı (tam genişlik)
        VBox trieBottom = buildTrieBottomPanel();

        // SplitPane: üst (%62) + alt (%38)
        mainSplit = new SplitPane();
        mainSplit.setOrientation(Orientation.VERTICAL);
        mainSplit.setStyle("-fx-background-color:" + BG + "; -fx-border-width:0;");
        mainSplit.getItems().addAll(topRow, trieBottom);
        mainSplit.setDividerPositions(0.62);

        root.setCenter(mainSplit);

        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.web(BG));
        stage.setTitle("Osmanlıca Sözlük  ✦  Trie Veri Yapısı");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(750);
        stage.show();

        // Canvas genişliğini pencere genişliğine bağla
        scene.widthProperty().addListener((o, ov, nv) -> {
            trieCanvas.setWidth(nv.doubleValue() - 20);
            redrawTrie();
        });

        loadAll();
        drawTrieDefault("");
    }

    // ═══════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════
    private HBox buildHeader() {
        HBox h = new HBox(18);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(14, 28, 14, 28));
        h.setStyle(
                "-fx-background-color:linear-gradient(to right,#050A14,#0D1E38,#050A14);" +
                        "-fx-border-color:" + TEAL + "; -fx-border-width:0 0 2 0;"
        );

        Label star = new Label("✦");
        star.setStyle("-fx-font-size:38; -fx-text-fill:" + TEAL + ";");
        star.setEffect(new DropShadow(22, Color.web(TEAL)));

        VBox titles = new VBox(3);
        Label t1 = new Label("Osmanlıca Sözlük");
        t1.setStyle("-fx-font-size:28; -fx-font-weight:bold; -fx-font-family:Georgia; -fx-text-fill:" + TXT1 + ";");
        Label t2 = new Label("Trie Veri Yapısı  •  " + trie.getWordCount() +
                " Kelime  •  CSV Veri Kaynağı  •  Veri Yapıları Dersi");
        t2.setStyle("-fx-font-size:11; -fx-text-fill:" + TXT2 + ";");
        titles.getChildren().addAll(t1, t2);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox chips = new HBox(10);
        chips.setAlignment(Pos.CENTER);
        chips.getChildren().addAll(
                chip("📚  " + trie.getWordCount() + " Kelime",  TEAL),
                chip("🌿  Trie Ağacı",                           GOLD),
                chip("📂  " + (categories.length - 1) + " Kategori", VIOLET),
                chip("⚡  O(m) Arama",                           GREEN)
        );

        h.getChildren().addAll(star, titles, sp, chips);
        return h;
    }

    private VBox chip(String text, String color) {
        VBox v = new VBox();
        v.setAlignment(Pos.CENTER);
        v.setPadding(new Insets(6, 14, 6, 14));
        v.setStyle(
                "-fx-background-color:" + color + "15;" +
                        "-fx-background-radius:8;" +
                        "-fx-border-color:" + color + "50;" +
                        "-fx-border-radius:8;"
        );
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12; -fx-text-fill:" + color + "; -fx-font-weight:bold;");
        v.getChildren().add(l);
        return v;
    }

    // ═══════════════════════════════════════════════════
    // SOL PANEL — Kelime Listesi
    // ═══════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox box = new VBox(0);
        box.setPrefWidth(300);
        box.setMinWidth(260);
        box.setStyle("-fx-background-color:" + PANEL + "; -fx-border-color:" + BORDER + "; -fx-border-width:0 1 0 0;");

        // Arama kutusu
        VBox searchArea = new VBox(8);
        searchArea.setPadding(new Insets(14, 12, 10, 12));
        searchArea.setStyle("-fx-background-color:" + PANEL + ";");

        Label lbl = new Label("ARAMA");
        lbl.setStyle("-fx-font-size:9; -fx-font-weight:bold; -fx-letter-spacing:2; -fx-text-fill:" + TEAL + ";");

        HBox sRow = new HBox(8);
        sRow.setAlignment(Pos.CENTER_LEFT);
        sRow.setPadding(new Insets(8, 10, 8, 10));
        sRow.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:9; -fx-border-color:" + BORDER2 + "; -fx-border-radius:9; -fx-border-width:1;");

        Label ico = new Label("⌕");
        ico.setStyle("-fx-font-size:17; -fx-text-fill:" + TEAL + ";");
        searchField = new TextField();
        searchField.setPromptText("Kelime ara...");
        searchField.setStyle("-fx-background-color:transparent; -fx-text-fill:" + TXT1 + "; -fx-prompt-text-fill:" + TXT3 + "; -fx-font-size:13; -fx-border-width:0;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        Button clr = new Button("✕");
        clr.setStyle("-fx-background-color:transparent; -fx-text-fill:" + TXT3 + "; -fx-font-size:11; -fx-border-width:0; -fx-cursor:hand;");
        clr.setOnAction(e -> searchField.clear());
        sRow.getChildren().addAll(ico, searchField, clr);

        HBox modeRow = new HBox(12);
        modeRow.setAlignment(Pos.CENTER_LEFT);
        modeGroup = new ToggleGroup();
        Label ml = new Label("Mod:");
        ml.setStyle("-fx-text-fill:" + TXT3 + "; -fx-font-size:11;");
        modeRow.getChildren().addAll(ml, radio("Osmanlıca", true), radio("Anlam içinde", false));

        HBox catRow = new HBox(8);
        catRow.setAlignment(Pos.CENTER_LEFT);
        Label cl = new Label("Kategori:");
        cl.setStyle("-fx-text-fill:" + TXT3 + "; -fx-font-size:11;");
        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(categories));
        categoryCombo.setValue("Tümü");
        categoryCombo.setPrefWidth(170);
        categoryCombo.setStyle("-fx-background-color:" + CARD + "; -fx-text-fill:" + TXT1 + "; -fx-border-color:" + BORDER + "; -fx-font-size:11;");
        catRow.getChildren().addAll(cl, categoryCombo);

        resultLbl = new Label();
        resultLbl.setStyle("-fx-font-size:10; -fx-text-fill:" + TXT3 + ";");

        searchArea.getChildren().addAll(lbl, sRow, modeRow, catRow, resultLbl);

        // Kelime listesi
        wordItems = FXCollections.observableArrayList();
        wordList  = new ListView<>(wordItems);
        wordList.setStyle("-fx-background-color:" + PANEL + "; -fx-border-width:0;");
        VBox.setVgrow(wordList, Priority.ALWAYS);

        wordList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OttomanWord w, boolean empty) {
                super.updateItem(w, empty);
                if (empty || w == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color:" + PANEL + "; -fx-border-width:0;");
                    return;
                }
                VBox cell = new VBox(3);
                cell.setPadding(new Insets(8, 12, 8, 12));

                HBox top = new HBox();
                top.setAlignment(Pos.CENTER_LEFT);
                Label nm = new Label(w.getOttoman());
                nm.setStyle("-fx-font-size:13; -fx-font-weight:bold; -fx-font-family:Georgia; -fx-text-fill:" +
                        (isSelected() ? TEAL : TXT1) + ";");
                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);
                Label ct = new Label(w.getCategory());
                ct.setStyle("-fx-font-size:9; -fx-text-fill:" + GOLD + "; -fx-background-color:" + GOLD + "18; -fx-background-radius:4; -fx-padding:1 5 1 5;");
                top.getChildren().addAll(nm, r, ct);

                String sh = w.getMeaning().length() > 44 ? w.getMeaning().substring(0, 41) + "…" : w.getMeaning();
                Label mn = new Label(sh);
                mn.setStyle("-fx-font-size:10; -fx-text-fill:" + TXT2 + ";");

                cell.getChildren().addAll(top, mn);
                setGraphic(cell); setText(null);
                setStyle(isSelected()
                        ? "-fx-background-color:#0A2040; -fx-border-color:" + TEAL + "; -fx-border-width:0 0 0 3;"
                        : "-fx-background-color:" + (getIndex() % 2 == 0 ? PANEL : CARD) + "; -fx-border-width:0;");
            }
        });

        wordList.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            if (sel != null) {
                showDetail(sel);
                drawTrieDefault(searchField.getText().trim());
            }
        });

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + BORDER + ";");
        box.getChildren().addAll(searchArea, sep, wordList);

        searchField.textProperty().addListener((o, ov, nv) -> { search(); drawTrieDefault(nv.trim()); });
        categoryCombo.valueProperty().addListener((o, ov, nv) -> search());
        modeGroup.selectedToggleProperty().addListener((o, ov, nv) -> search());

        return box;
    }

    private RadioButton radio(String text, boolean sel) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(modeGroup);
        rb.setSelected(sel);
        rb.setStyle("-fx-text-fill:" + TXT2 + "; -fx-font-size:11;");
        return rb;
    }

    // ═══════════════════════════════════════════════════
    // ORTA — Detay Paneli
    // ═══════════════════════════════════════════════════
    private StackPane buildDetailPanel() {
        StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color:" + BG + ";");

        // PLACEHOLDER
        placeholder = new VBox(20);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-background-color:" + BG + ";");

        Label ph1 = new Label("✦");
        ph1.setStyle("-fx-font-size:52; -fx-text-fill:" + TEAL2 + ";");
        ph1.setEffect(new DropShadow(20, Color.web(TEAL2)));
        Label ph2 = new Label("Soldaki listeden bir kelime seçin");
        ph2.setStyle("-fx-font-size:15; -fx-text-fill:" + TXT2 + ";");

        // Mini algoritma notu
        VBox mini = new VBox(7);
        mini.setAlignment(Pos.CENTER);
        mini.setPadding(new Insets(14, 22, 14, 22));
        mini.setMaxWidth(340);
        mini.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:10; -fx-border-color:" + BORDER + "; -fx-border-radius:10;");
        for (String s : new String[]{
                "⚡  insert  →  O(m)   her harf = bir düğüm",
                "🔍  search  →  O(m)   kökten yaprağa ini",
                "🌿  prefix  →  O(p+k) tüm dalları topla"
        }) {
            Label li = new Label(s);
            li.setStyle("-fx-font-family:Monospace; -fx-font-size:12; -fx-text-fill:" + TEAL + ";");
            mini.getChildren().add(li);
        }
        placeholder.getChildren().addAll(ph1, ph2, mini);

        // DETAY
        detailPane = new VBox(18);
        detailPane.setPadding(new Insets(30, 42, 30, 42));
        detailPane.setStyle("-fx-background-color:" + BG + ";");

        // Kelime başlığı + Arapça
        HBox nameRow = new HBox();
        nameRow.setAlignment(Pos.CENTER_LEFT);

        lbWord = new Label("—");
        lbWord.setStyle("-fx-font-size:44; -fx-font-weight:bold; -fx-font-family:Georgia; -fx-text-fill:" + TXT1 + ";");
        lbWord.setEffect(new DropShadow(14, Color.web(TEAL + "44")));

        Region nsp = new Region();
        HBox.setHgrow(nsp, Priority.ALWAYS);

        VBox arabicBox = new VBox(2);
        arabicBox.setAlignment(Pos.CENTER_RIGHT);
        lbArabic = new Label("—");
        lbArabic.setStyle("-fx-font-size:32; -fx-text-fill:" + TEAL + "; -fx-font-family:'Arial Unicode MS';");
        lbArabic.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Label arabicHint = new Label("Arapça yazılış");
        arabicHint.setStyle("-fx-font-size:9; -fx-text-fill:" + TXT3 + ";");
        arabicBox.getChildren().addAll(lbArabic, arabicHint);
        nameRow.getChildren().addAll(lbWord, nsp, arabicBox);

        // Renkli çizgi
        Pane divLine = new Pane();
        divLine.setPrefHeight(3);
        divLine.setStyle("-fx-background-color:linear-gradient(to right," + TEAL + "," + VIOLET + ",transparent); -fx-background-radius:2;");

        // İçerik iki sütun (sol: anlam+etiket+örnek, sağ: ilgili kelimeler)
        HBox contentRow = new HBox(20);

        // Sol sütun
        VBox leftCol = new VBox(14);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        VBox meanSec = sec("ANLAMI", TEAL);
        lbMeaning = new Label("—");
        lbMeaning.setStyle("-fx-font-size:17; -fx-text-fill:" + TXT1 + "; -fx-wrap-text:true; -fx-font-family:Georgia;");
        lbMeaning.setWrapText(true);
        meanSec.getChildren().add(cardBox(lbMeaning));

        HBox tags = new HBox(10);
        lbOrigin = new Label("—");
        lbCat    = new Label("—");
        tagStyle(lbOrigin, ROSE);
        tagStyle(lbCat, VIOLET);
        tags.getChildren().addAll(lbOrigin, lbCat);

        VBox exSec = sec("ÖRNEK KULLANIM", AMBER);
        lbExample = new Label("—");
        lbExample.setStyle("-fx-font-size:14; -fx-text-fill:" + TXT2 + "; -fx-font-style:italic; -fx-wrap-text:true;");
        lbExample.setWrapText(true);
        VBox exCard = cardBox(lbExample);
        exCard.setStyle(exCard.getStyle() + "-fx-border-color:" + AMBER + " transparent transparent transparent; -fx-border-width:0 0 0 4;");
        exSec.getChildren().add(exCard);

        leftCol.getChildren().addAll(meanSec, tags, exSec);

        // Sağ sütun — İlgili kelimeler (aynı önekle başlayanlar)
        VBox rightCol = new VBox(10);
        rightCol.setPrefWidth(210);
        rightCol.setMinWidth(180);

        VBox relSec = sec("AYNI ÖNEK — Trie Sonuçları", GREEN);
        lbRelated = new Label("—");
        lbRelated.setStyle("-fx-font-size:12; -fx-text-fill:" + TXT2 + "; -fx-wrap-text:true; -fx-font-family:Monospace;");
        lbRelated.setWrapText(true);

        VBox relCard = new VBox(6);
        relCard.setPadding(new Insets(12, 14, 12, 14));
        relCard.setStyle(
                "-fx-background-color:" + CARD + ";" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:" + GREEN + "44;" +
                        "-fx-border-radius:10; -fx-border-width:1;"
        );
        Label relHint = new Label("Bu kelimeyle aynı öneki\npaylaşan diğer kelimeler:");
        relHint.setStyle("-fx-font-size:10; -fx-text-fill:" + TXT3 + ";");
        relCard.getChildren().addAll(relHint, lbRelated);
        relSec.getChildren().add(relCard);

        // Algoritma tablosu (kompakt)
        VBox algoBox = buildAlgoTable();

        rightCol.getChildren().addAll(relSec, algoBox);
        contentRow.getChildren().addAll(leftCol, rightCol);

        detailPane.getChildren().addAll(nameRow, divLine, contentRow);

        detailScroll = new ScrollPane(detailPane);
        detailScroll.setFitToWidth(true);
        detailScroll.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + "; -fx-border-width:0;");
        detailScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailScroll.setVisible(false);

        // Viewport arka planını düzelt (scene render sonrası)
        detailScroll.skinProperty().addListener((o, ov, nv) -> {
            detailScroll.lookup(".viewport").setStyle("-fx-background-color:" + BG + ";");
        });

        stack.getChildren().addAll(placeholder, detailScroll);
        HBox.setHgrow(stack, Priority.ALWAYS);
        return stack;
    }

    private VBox sec(String title, String color) {
        VBox b = new VBox(7);
        Label l = new Label("▸  " + title);
        l.setStyle("-fx-font-size:9; -fx-font-weight:bold; -fx-letter-spacing:2; -fx-text-fill:" + color + ";");
        b.getChildren().add(l);
        return b;
    }

    private VBox cardBox(Label content) {
        VBox c = new VBox();
        c.setPadding(new Insets(12, 16, 12, 16));
        c.getChildren().add(content);
        c.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:9; -fx-border-color:" + BORDER + "; -fx-border-radius:9; -fx-border-width:1;");
        return c;
    }

    private void tagStyle(Label l, String color) {
        l.setPadding(new Insets(4, 14, 4, 14));
        l.setStyle(
                "-fx-background-color:" + color + "20;" +
                        "-fx-background-radius:18;" +
                        "-fx-border-color:" + color + "60;" +
                        "-fx-border-radius:18; -fx-border-width:1;" +
                        "-fx-text-fill:" + color + "; -fx-font-size:12; -fx-font-weight:bold;"
        );
    }

    private VBox buildAlgoTable() {
        VBox box = new VBox(9);
        box.setPadding(new Insets(12, 14, 12, 14));
        box.setStyle(
                "-fx-background-color:" + CARD2 + ";" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:" + GOLD + "50;" +
                        "-fx-border-radius:10; -fx-border-width:1;"
        );
        Label title = new Label("⚙  Trie Karmaşıklığı");
        title.setStyle("-fx-font-size:11; -fx-font-weight:bold; -fx-text-fill:" + GOLD + ";");

        GridPane g = new GridPane();
        g.setHgap(16); g.setVgap(6);
        String[][] rows = {
                {"insert",    "O(m)",    "m = harf sayısı"},
                {"search",    "O(m)",    "kök → yaprak"},
                {"prefix",    "O(p+k)",  "p=önek, k=sonuç"},
                {"delete",    "O(m)",    "özyinelemeli"},
                {"alan",      "O(N×M)",  "N=kelime, M=uzunluk"},
        };
        for (int i = 0; i < rows.length; i++) {
            Label op = new Label(rows[i][0]);
            op.setStyle("-fx-font-family:Monospace; -fx-font-size:11; -fx-text-fill:" + TXT2 + ";");
            Label cx = new Label(rows[i][1]);
            cx.setStyle("-fx-font-family:Monospace; -fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:" + TEAL + ";");
            Label nt = new Label(rows[i][2]);
            nt.setStyle("-fx-font-size:10; -fx-text-fill:" + TXT3 + ";");
            g.add(op, 0, i); g.add(cx, 1, i); g.add(nt, 2, i);
        }
        box.getChildren().addAll(title, g);
        return box;
    }

    // ═══════════════════════════════════════════════════
    // ALT — TRİE PANELİ (tam genişlik)
    // ═══════════════════════════════════════════════════
    private VBox buildTrieBottomPanel() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color:" + PANEL + ";");

        // Başlık çubuğu
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(9, 18, 9, 18));
        titleBar.setStyle(
                "-fx-background-color:" + CARD + ";" +
                        "-fx-border-color:" + TEAL + "; -fx-border-width:1 0 0 0;"
        );

        Label treeIco = new Label("🌿");
        treeIco.setStyle("-fx-font-size:16;");
        trieTitleLbl = new Label("TRİE AĞACI — Yatay Hiyerarşik Görünüm");
        trieTitleLbl.setStyle("-fx-font-size:11; -fx-font-weight:bold; -fx-letter-spacing:1; -fx-text-fill:" + TEAL + ";");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label legend = new Label(
                "◉ = Kelime sonu    ○ = Ara düğüm    " +
                        "━━ Türkuaz = Arama yolu    ━━ Altın = Prefix sonu    ━━ Gri = Diğer dallar"
        );
        legend.setStyle("-fx-font-family:Monospace; -fx-font-size:10; -fx-text-fill:" + TXT3 + ";");

        titleBar.getChildren().addAll(treeIco, trieTitleLbl, sp, legend);

        // Canvas
        trieCanvas = new Canvas(1380, 320);
        gc = trieCanvas.getGraphicsContext2D();

        ScrollPane cScroll = new ScrollPane(trieCanvas);
        cScroll.setStyle("-fx-background-color:" + PANEL + "; -fx-background:" + PANEL + "; -fx-border-width:0;");
        cScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cScroll.skinProperty().addListener((o, ov, nv) -> {
            cScroll.lookup(".viewport").setStyle("-fx-background-color:" + PANEL + ";");
        });
        VBox.setVgrow(cScroll, Priority.ALWAYS);

        panel.getChildren().addAll(titleBar, cScroll);
        return panel;
    }

    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(6, 20, 6, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-border-width:1 0 0 0;");

        Label l = new Label("Trie  •  insert O(m)  •  search O(m)  •  startsWith O(p+k)  •  delete O(m)  •  space O(N·M)");
        l.setStyle("-fx-font-family:Monospace; -fx-font-size:11; -fx-text-fill:" + TXT3 + ";");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label r = new Label("Veri Yapıları Dersi  ✦  Java 25  •  JavaFX 17  •  Trie + CSV");
        r.setStyle("-fx-font-size:11; -fx-text-fill:" + TXT3 + ";");
        bar.getChildren().addAll(l, sp, r);
        return bar;
    }

    private Region vSep() {
        Region r = new Region();
        r.setPrefWidth(1);
        r.setStyle("-fx-background-color:" + BORDER + ";");
        return r;
    }

    // ═══════════════════════════════════════════════════
    // METODLAR
    // ═══════════════════════════════════════════════════
    private void loadAll() {
        List<OttomanWord> ws = trie.getAllWords();
        wordItems.setAll(ws);
        resultLbl.setText(ws.size() + " kelime");
        if (!ws.isEmpty()) wordList.getSelectionModel().selectFirst();
    }

    private void search() {
        String q   = searchField.getText().trim();
        String cat = categoryCombo.getValue();
        RadioButton sel = (RadioButton) modeGroup.getSelectedToggle();
        boolean byMeaning = sel != null && sel.getText().equals("Anlam içinde");

        List<OttomanWord> res = byMeaning
                ? (q.isEmpty() ? trie.getAllWords() : trie.searchInMeaning(q))
                : trie.searchByPrefix(q);

        if (cat != null && !cat.equals("Tümü")) {
            List<OttomanWord> f = new ArrayList<>();
            for (OttomanWord w : res)
                if (w.getCategory().equals(cat)) f.add(w);
            res = f;
        }

        wordItems.setAll(res);
        resultLbl.setText(res.size() + " / " + trie.getWordCount() + " kelime");

        if (!res.isEmpty()) wordList.getSelectionModel().selectFirst();
        else { placeholder.setVisible(true); detailScroll.setVisible(false); }
    }

    private void showDetail(OttomanWord w) {
        lbWord.setText(w.getOttoman());
        lbArabic.setText(w.getArabic().isEmpty() ? "—" : w.getArabic());
        lbMeaning.setText(w.getMeaning());
        lbOrigin.setText("  🌐  " + w.getOrigin() + "  ");
        lbCat.setText("  📂  " + w.getCategory() + "  ");
        lbExample.setText(w.getExample().isEmpty()
                ? "— (örnek cümle bulunmuyor)"
                : "« " + w.getExample() + " »");

        // Aynı önekle başlayan kelimeler (Trie prefix araması)
        String prefix2 = w.getOttoman().length() >= 2
                ? w.getOttoman().substring(0, 2).toLowerCase() : w.getOttoman().toLowerCase();
        List<OttomanWord> related = trie.searchByPrefix(prefix2);
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (OttomanWord r : related) {
            if (!r.getOttoman().equals(w.getOttoman()) && cnt < 7) {
                sb.append("• ").append(r.getOttoman()).append("\n");
                cnt++;
            }
        }
        lbRelated.setText(cnt == 0 ? "— (eşleşme yok)" : sb.toString().trim());

        placeholder.setVisible(false);
        detailScroll.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(220), detailPane);
        ft.setFromValue(0.4); ft.setToValue(1.0); ft.play();
    }

    private String currentPrefix = "";

    private void redrawTrie() {
        drawTrieDefault(currentPrefix);
    }

    // ═══════════════════════════════════════════════════
    // TRİE GÖRSELLEŞTİRME — Yatay Hiyerarşik Ağaç
    // ═══════════════════════════════════════════════════

    /**
     * Ana çizim metodu.
     * prefix boşsa: tüm ilk harfleri gösteren genel ağaç
     * prefix doluysa: arama yolunu vurgular + genel ağaç soluklaşır
     */
    private void drawTrieDefault(String prefix) {
        currentPrefix = prefix;
        double W = trieCanvas.getWidth();
        double H = trieCanvas.getHeight();

        clearCanvas(W, H);

        List<OttomanWord> all = trie.getAllWords();

        // İlk harflere göre grupla (alfabetik)
        Map<Character, List<OttomanWord>> byChar = new TreeMap<>();
        for (OttomanWord w : all) {
            char c = Character.toUpperCase(w.getOttoman().charAt(0));
            byChar.computeIfAbsent(c, k -> new ArrayList<>()).add(w);
        }

        List<Character> chars = new ArrayList<>(byChar.keySet());
        int n = chars.size(); // toplam ilk harf sayısı

        // ── Düzen parametreleri ──────────────────────
        double rootX    = 55;
        double rootY    = H / 2;
        double lvl1X    = 140;      // 1. seviye (ilk harf) x
        double lvl2X    = 240;      // 2. seviye x
        double lvl3X    = 340;      // 3. seviye x (sadece arama yolunda)
        double nodeR    = 14;       // düğüm yarıçapı
        double bigR     = 20;       // vurgulanan düğüm yarıçapı

        // Harf satır yükseklikleri: eşit dağıtım
        double totalH   = H - 40;
        double rowH     = Math.max(totalH / n, 22);

        // Trie canvas boyutunu ayarla
        double neededH  = Math.max(H, n * rowH + 40);
        if (neededH > trieCanvas.getHeight()) {
            trieCanvas.setHeight(neededH);
            clearCanvas(W, neededH);
        }

        // Seçili prefix bilgisi
        char hlChar1 = (prefix != null && prefix.length() >= 1)
                ? Character.toUpperCase(prefix.charAt(0)) : 0;
        char hlChar2 = (prefix != null && prefix.length() >= 2)
                ? Character.toUpperCase(prefix.charAt(1)) : 0;

        // ── ROOT çiz ────────────────────────────────
        drawRootNode(rootX, H / 2, "ROOT");

        // ── Her ilk harf için ────────────────────────
        for (int i = 0; i < n; i++) {
            char c1    = chars.get(i);
            double y1  = 20 + i * rowH + rowH / 2;
            boolean hl = (c1 == hlChar1);
            String col = hl ? TEAL : TXT4;

            // ROOT → ilk harf kenarı
            drawLine(rootX + (hl ? bigR : nodeR), H / 2,
                    lvl1X - (hl ? bigR : nodeR), y1, col, hl ? 2.0 : 1.0);

            // İlk harf düğümü
            int cnt = byChar.get(c1).size();
            boolean isWordEnd1 = byChar.get(c1).stream().anyMatch(w -> w.getOttoman().length() == 1);

            if (hl) {
                drawBigNodeHL(lvl1X, y1, String.valueOf(c1), isWordEnd1, TEAL);
            } else {
                drawSmallNode(lvl1X, y1, String.valueOf(c1), isWordEnd1, col);
            }

            // Küçük kelime sayısı etiketi
            if (cnt > 1 && !hl) {
                gc.setFill(Color.web(TXT4));
                gc.setFont(Font.font("Monospace", 8));
                gc.fillText("×" + cnt, lvl1X + nodeR + 2, y1 + 4);
            }

            // ── 2. seviye: sadece vurgulanan veya ilk 4 harf ──
            if (hl || i < 4) {
                Map<Character, List<OttomanWord>> lvl2map = new LinkedHashMap<>();
                for (OttomanWord w : byChar.get(c1)) {
                    if (w.getOttoman().length() > 1) {
                        char c2 = Character.toUpperCase(w.getOttoman().charAt(1));
                        lvl2map.computeIfAbsent(c2, k -> new ArrayList<>()).add(w);
                    }
                }

                List<Character> chars2 = new ArrayList<>(new TreeSet<>(lvl2map.keySet()));
                int m = chars2.size();
                double spread2 = Math.min(rowH * 0.9, 20.0);
                double startY2 = y1 - (m - 1) * spread2 / 2.0;

                for (int j = 0; j < m; j++) {
                    char c2     = chars2.get(j);
                    double y2   = startY2 + j * spread2;
                    boolean hl2 = hl && (c2 == hlChar2);
                    String col2 = hl2 ? GOLD : (hl ? TEAL2 : TXT4);

                    // Kenar
                    drawLine(lvl1X + (hl ? bigR : nodeR), y1,
                            lvl2X - nodeR, y2, col2, hl2 ? 2.5 : (hl ? 1.5 : 0.8));

                    // Düğüm
                    boolean isWE2 = lvl2map.get(c2).stream().anyMatch(w -> w.getOttoman().length() == 2);
                    if (hl2) {
                        drawBigNodeHL(lvl2X, y2, String.valueOf(c2), isWE2, GOLD);
                    } else {
                        drawSmallNode(lvl2X, y2, String.valueOf(c2), isWE2, col2);
                    }

                    // 3. seviye: sadece vurgulanan 2. harfin altında
                    if (hl2 && prefix.length() >= 3) {
                        Map<Character, List<OttomanWord>> lvl3map = new LinkedHashMap<>();
                        for (OttomanWord w : lvl2map.get(c2)) {
                            if (w.getOttoman().length() > 2) {
                                char c3 = Character.toUpperCase(w.getOttoman().charAt(2));
                                lvl3map.computeIfAbsent(c3, k -> new ArrayList<>()).add(w);
                            }
                        }
                        char hlChar3 = Character.toUpperCase(prefix.charAt(2));
                        List<Character> chars3 = new ArrayList<>(new TreeSet<>(lvl3map.keySet()));
                        int p = chars3.size();
                        double spread3 = Math.min(spread2 * 0.8, 15.0);
                        double startY3 = y2 - (p - 1) * spread3 / 2.0;

                        for (int k = 0; k < p; k++) {
                            char c3   = chars3.get(k);
                            double y3 = startY3 + k * spread3;
                            boolean hl3 = (c3 == hlChar3);
                            String col3 = hl3 ? GREEN : TXT4;
                            drawLine(lvl2X + bigR, y2, lvl3X - nodeR, y3, col3, hl3 ? 2.0 : 0.8);
                            boolean isWE3 = lvl3map.get(c3).stream().anyMatch(w -> w.getOttoman().length() == 3);
                            if (hl3) drawBigNodeHL(lvl3X, y3, String.valueOf(c3), isWE3, GREEN);
                            else     drawSmallNode(lvl3X, y3, String.valueOf(c3), isWE3, col3);
                        }
                    }
                }
            }
        }

        // ── Arama sonuç kutusu (sağ taraf) ──────────
        if (prefix != null && !prefix.isEmpty()) {
            List<OttomanWord> results = trie.searchByPrefix(prefix);
            drawResultBox(W, H, prefix, results);
            trieTitleLbl.setText("TRİE AĞACI  —  \"" + prefix + "\" araması  →  " + results.size() + " eşleşme");
        } else {
            drawLegendBox(W, H, byChar.size());
            trieTitleLbl.setText("TRİE AĞACI  —  Yatay Hiyerarşik Görünüm  (" + n + " ilk harf, " + trie.getWordCount() + " kelime)");
        }
    }

    // ─ Canvas çizim yardımcıları ─────────────────────

    private void clearCanvas(double W, double H) {
        gc.setFill(Color.web(PANEL));
        gc.fillRect(0, 0, W, H);
        // Hafif ızgara
        gc.setStroke(Color.web(BORDER + "28"));
        gc.setLineWidth(0.4);
        for (int x = 0; x < W; x += 30) gc.strokeLine(x, 0, x, H);
        for (int y = 0; y < H; y += 30) gc.strokeLine(0, y, W, y);
    }

    /** ROOT düğümü — özel stil */
    private void drawRootNode(double x, double y, String label) {
        double r = 22;
        // Glow
        gc.setFill(Color.web(TEAL + "18"));
        gc.fillOval(x - r - 7, y - r - 7, (r + 7) * 2, (r + 7) * 2);
        // Dolgu
        gc.setFill(Color.web(CARD2));
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        // Çift kenarlık (ROOT'un özel işareti)
        gc.setStroke(Color.web(TEAL));
        gc.setLineWidth(2);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);
        gc.setLineWidth(1);
        gc.strokeOval(x - r + 4, y - r + 4, (r - 4) * 2, (r - 4) * 2);
        // Etiket
        gc.setFill(Color.web(TEAL));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.fillText(label, x - 13, y + 4);
    }

    /** Küçük düğüm — genel ağaç */
    private void drawSmallNode(double x, double y, String label, boolean wordEnd, String color) {
        double r = 13;
        gc.setFill(Color.web(CARD));
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        gc.setStroke(Color.web(color));
        gc.setLineWidth(1.0);
        if (wordEnd) { // kelime sonu = çift çember
            gc.setLineWidth(0.7);
            gc.strokeOval(x - r + 3, y - r + 3, (r - 3) * 2, (r - 3) * 2);
        }
        gc.setLineWidth(1.0);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);
        gc.setFill(Color.web(color));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        gc.fillText(label, x - 4, y + 4);
    }

    /** Büyük vurgulu düğüm — arama yolu */
    private void drawBigNodeHL(double x, double y, String label, boolean wordEnd, String color) {
        double r = 19;
        // Dış glow
        gc.setFill(Color.web(color + "20"));
        gc.fillOval(x - r - 8, y - r - 8, (r + 8) * 2, (r + 8) * 2);
        // Dolgu
        gc.setFill(Color.web(CARD2));
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        // Kenarlık
        gc.setStroke(Color.web(color));
        gc.setLineWidth(2.5);
        if (wordEnd) {
            gc.setLineWidth(1.5);
            gc.strokeOval(x - r + 4, y - r + 4, (r - 4) * 2, (r - 4) * 2);
        }
        gc.setLineWidth(2.5);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);
        // Etiket
        gc.setFill(Color.web(color));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        gc.fillText(label, x - 5, y + 5);
    }

    /** Kenar (çizgi) */
    private void drawLine(double x1, double y1, double x2, double y2, String color, double width) {
        gc.setStroke(Color.web(color));
        gc.setLineWidth(width);
        gc.setLineDashes(0);
        gc.strokeLine(x1, y1, x2, y2);
    }

    /** Arama sonuçları kutusu (sağ taraf) */
    private void drawResultBox(double W, double H, String prefix, List<OttomanWord> results) {
        double bx = 390, by = 12, bw = W - bx - 12, bh = H - 24;
        if (bw < 200) return;

        gc.setFill(Color.web(CARD2 + "EE"));
        gc.fillRoundRect(bx, by, bw, bh, 12, 12);
        gc.setStroke(Color.web(TEAL + "60"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(bx, by, bw, bh, 12, 12);

        gc.setFill(Color.web(TEAL));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        gc.fillText("Prefix: \"" + prefix + "\"  →  " + results.size() + " eşleşme", bx + 14, by + 22);

        // Ayraç
        gc.setStroke(Color.web(TEAL + "40"));
        gc.setLineWidth(1);
        gc.strokeLine(bx + 10, by + 28, bx + bw - 10, by + 28);

        int show = (int) Math.min(results.size(), (bh - 50) / 17);
        OttomanWord selWord = wordList.getSelectionModel().getSelectedItem();

        for (int i = 0; i < show; i++) {
            OttomanWord rw = results.get(i);
            boolean isSel = selWord != null && rw.getOttoman().equals(selWord.getOttoman());
            gc.setFill(isSel ? Color.web(TEAL) : Color.web(i == 0 ? TXT1 : TXT2));
            gc.setFont(Font.font("Georgia", isSel ? FontWeight.BOLD : FontWeight.NORMAL, 12));
            String mn = rw.getMeaning().length() > 30
                    ? rw.getMeaning().substring(0, 27) + "…" : rw.getMeaning();
            gc.fillText((isSel ? "▶ " : "• ") + rw.getOttoman() + "  →  " + mn,
                    bx + 14, by + 44 + i * 17);
        }
        if (results.size() > show) {
            gc.setFill(Color.web(TXT3));
            gc.setFont(Font.font("Monospace", 10));
            gc.fillText("  +" + (results.size() - show) + " kelime daha…", bx + 14, by + 44 + show * 17);
        }
    }

    /** Genel görünümde sağ taraf: istatistik kutusu */
    private void drawLegendBox(double W, double H, int firstCharCount) {
        double bx = 390, by = 12, bw = W - bx - 12, bh = H - 24;
        if (bw < 200) return;

        gc.setFill(Color.web(CARD2 + "EE"));
        gc.fillRoundRect(bx, by, bw, bh, 12, 12);
        gc.setStroke(Color.web(GOLD + "50"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(bx, by, bw, bh, 12, 12);

        gc.setFill(Color.web(GOLD));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        gc.fillText("Trie İstatistikleri", bx + 14, by + 22);

        gc.setStroke(Color.web(GOLD + "40"));
        gc.strokeLine(bx + 10, by + 28, bx + bw - 10, by + 28);

        String[][] info = {
                {"Toplam kelime",    String.valueOf(trie.getWordCount())},
                {"İlk harf çeşidi", firstCharCount + " farklı harf"},
                {"insert / search", "O(m)  →  m = harf sayısı"},
                {"startsWith",      "O(p+k)  →  p=önek, k=sonuç"},
                {"Alan",            "O(N×M)  →  N=kelime, M=ort. uzunluk"},
        };
        for (int i = 0; i < info.length; i++) {
            gc.setFill(Color.web(i % 2 == 0 ? TEAL : TXT2));
            gc.setFont(Font.font("Monospace", 11));
            gc.fillText(info[i][0] + "  :  " + info[i][1], bx + 14, by + 44 + i * 18);
        }

        gc.setFill(Color.web(TXT3));
        gc.setFont(Font.font("Monospace", 10));
        gc.fillText("Arama kutusuna yazarak ağaçta gezinin →", bx + 14, by + bh - 14);
    }

    // ═══════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════
    public static void main(String[] args) { launch(args); }
}