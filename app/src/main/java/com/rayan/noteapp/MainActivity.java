package com.rayan.noteapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private NotesDB db;

    private FrameLayout root;
    private LinearLayout main;
    private LinearLayout notesContainer;
    private LinearLayout bottomBar;

    private EditText search;

    private TextView dayText;
    private TextView timeText;
    private TextView dateText;

    private TextView tabNotes;
    private TextView tabFavorites;
    private TextView tabArchive;
    private TextView tabTrash;

    private LinearLayout selectionBar;
    private TextView selectionCount;
    private TextView selectAll;
    private TextView deleteSelected;
    private TextView deleteAll;

    private TextView floatingPlus;

    private String mode = "all";
    private String searchText = "";
    private String sort = "updated";

    private boolean selectionMode = false;
private boolean lockChecked = false;

private static final int LOCK_REQUEST = 9001;

    private final ArrayList<Long> selectedIds =
            new ArrayList<>();

    private final Handler clockHandler =
            new Handler(Looper.getMainLooper());

    private int statusInset = 0;
    private int navigationInset = 0;

    private static final int BG =
            Color.rgb(2, 5, 12);

    private static final int WHITE =
            Color.rgb(248, 249, 255);

    private static final int MUTED =
            Color.rgb(157, 170, 205);

    private static final int LIGHT =
            Color.rgb(181, 194, 244);

    private static final int BLUE =
            Color.rgb(82, 112, 255);

    private static final int PURPLE =
            Color.rgb(120, 62, 246);

    private static final int ACTIVE_PURPLE =
            Color.rgb(164, 101, 255);

    private static final int BORDER =
            Color.rgb(70, 95, 170);

    private static final int[] NOTE_COLORS = {

            Color.rgb(15, 22, 39),
            Color.rgb(84, 59, 31),
            Color.rgb(27, 79, 67),
            Color.rgb(68, 43, 96),
            Color.rgb(34, 58, 100),
            Color.rgb(90, 40, 69),
            Color.rgb(90, 39, 43),
            Color.rgb(87, 72, 28),
            Color.rgb(92, 52, 28),
            Color.rgb(26, 77, 76),
            Color.rgb(45, 48, 94),
            Color.rgb(70, 48, 36)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        db = new NotesDB(this);

        setupWindow();

        buildUI();

        setupInsets();

        refresh();

        startClock();

        lockChecked = false;
    }

    @Override
    protected void onResume() {

        super.onResume();

        updateClock();

        if (db != null) {
            refresh();
        }

        if (AppLock.isEnabled(this) && !lockChecked) {

            lockChecked = true;

            Intent intent =
                    new Intent(
                            this,
                            LockActivity.class
                    );

            startActivityForResult(
                    intent,
                    LOCK_REQUEST
            );
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == LOCK_REQUEST
                        && resultCode != RESULT_OK
        ) {

            finishAffinity();
        }
    }

    @Override
    protected void onDestroy() {

        clockHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    // =========================================================
    // WINDOW
    // =========================================================

    private void setupWindow() {

        Window window = getWindow();

        window.setStatusBarColor(BG);

        window.setNavigationBarColor(BG);

        if (Build.VERSION.SDK_INT >= 30) {

            window.setDecorFitsSystemWindows(false);
        }

        window.getDecorView().setSystemUiVisibility(0);
    }

    // =========================================================
    // INSETS
    // =========================================================

    private void setupInsets() {

        getWindow()
                .getDecorView()
                .setOnApplyWindowInsetsListener(
                        (view, insets) -> {

                            if (Build.VERSION.SDK_INT >= 30) {

                                android.graphics.Insets bars =
                                        insets.getInsets(
                                                WindowInsets.Type.statusBars()
                                                        | WindowInsets.Type.displayCutout()
                                                        | WindowInsets.Type.navigationBars()
                                        );

                                statusInset = bars.top;

                                navigationInset = bars.bottom;

                            } else {

                                statusInset = dp(28);

                                navigationInset = dp(24);
                            }

                            applyInsets();

                            return insets;
                        }
                );
    }

    private void applyInsets() {

        if (main != null) {

            main.setPadding(
                    dp(16),
                    statusInset + dp(6),
                    dp(16),
                    0
            );
        }

        if (bottomBar != null) {

            FrameLayout.LayoutParams lp =
                    (FrameLayout.LayoutParams)
                            bottomBar.getLayoutParams();

            if (lp != null) {

                lp.bottomMargin =
                        navigationInset + dp(8);

                bottomBar.setLayoutParams(lp);
            }
        }

        if (notesContainer != null) {

            notesContainer.setPadding(
                    0,
                    dp(6),
                    0,
                    dp(190) + navigationInset
            );
        }

        if (floatingPlus != null) {

            FrameLayout.LayoutParams lp =
                    (FrameLayout.LayoutParams)
                            floatingPlus.getLayoutParams();

            if (lp != null) {

                lp.gravity =
                        Gravity.BOTTOM |
                                Gravity.RIGHT;

                lp.rightMargin =
                        dp(24);

                lp.bottomMargin =
                        navigationInset + dp(102);

                floatingPlus.setLayoutParams(lp);
            }
        }
    }

    // =========================================================
    // BUILD
    // =========================================================

    private void buildUI() {

        root = new FrameLayout(this);

        root.setBackgroundColor(BG);

        setContentView(root);

        main = new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        root.addView(
                main,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        buildHeader();

        buildSearch();

        buildTabs();

        buildSelectionBar();

        buildNotes();

        buildFloatingButton();

        buildBottomBar();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void buildHeader() {

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(13),
                dp(7),
                dp(13),
                dp(7)
        );

        header.setBackground(
                gradient(
                        Color.rgb(20, 52, 94),
                        Color.rgb(48, 27, 78),
                        BORDER,
                        27
                )
        );

        LinearLayout left =
                new LinearLayout(this);

        left.setOrientation(
                LinearLayout.VERTICAL
        );

        left.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                new TextView(this);

        title.setText("Notes");

        title.setTextColor(WHITE);

        title.setTextSize(30);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setSingleLine(true);

        title.setIncludeFontPadding(false);

        left.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Your thoughts,\nbeautifully organized"
        );

        subtitle.setTextColor(LIGHT);

        subtitle.setTextSize(13);

        subtitle.setMaxLines(2);

        subtitle.setIncludeFontPadding(false);

        left.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(44)
                )
        );

        LinearLayout headerButtons =
                new LinearLayout(this);

        headerButtons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        headerButtons.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView settings =
                roundButton("⚙");

        settings.setTextSize(20);

        TextView plus =
                roundButton("+");

        plus.setTextSize(30);

        settings.setOnClickListener(v -> {

            bounce(v);

            showSettings();
        });

        plus.setOnClickListener(v -> {

            bounce(v);

            openEditor(-1);
        });

        headerButtons.addView(
                settings,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(45)
                )
        );

        LinearLayout.LayoutParams plusLp =
                new LinearLayout.LayoutParams(
                        dp(49),
                        dp(49)
                );

        plusLp.leftMargin = dp(9);

        headerButtons.addView(
                plus,
                plusLp
        );

        left.addView(
                headerButtons,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(51)
                )
        );

        LinearLayout.LayoutParams leftLp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1.08f
                );

        leftLp.rightMargin = dp(8);

        header.addView(
                left,
                leftLp
        );

        View divider = new View(this);

        divider.setBackgroundColor(
                Color.rgb(91, 113, 183)
        );

        LinearLayout.LayoutParams dividerLp =
                new LinearLayout.LayoutParams(
                        dp(2),
                        dp(140)
                );

        dividerLp.rightMargin = dp(9);

        header.addView(
                divider,
                dividerLp
        );

        LinearLayout right =
                new LinearLayout(this);

        right.setOrientation(
                LinearLayout.VERTICAL
        );

        right.setGravity(
                Gravity.CENTER_VERTICAL
        );

        dayText =
                new TextView(this);

        dayText.setTextColor(LIGHT);

        dayText.setTextSize(16);

        dayText.setSingleLine(true);

        dayText.setIncludeFontPadding(false);

        right.addView(
                dayText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        timeText =
                new TextView(this);

        timeText.setTextColor(WHITE);

        timeText.setTextSize(23);

        timeText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        timeText.setSingleLine(true);

        timeText.setIncludeFontPadding(false);

        right.addView(
                timeText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(38)
                )
        );

        LinearLayout dateBox =
                new LinearLayout(this);

        dateBox.setGravity(
                Gravity.CENTER_VERTICAL
        );

        dateBox.setPadding(
                dp(4),
                0,
                dp(5),
                0
        );

        dateBox.setBackground(
                outline(
                        Color.rgb(18, 24, 49),
                        BORDER,
                        26
                )
        );

        TextView calendar =
                new TextView(this);

        calendar.setText("▣");

        calendar.setTextColor(WHITE);

        calendar.setTextSize(11);

        calendar.setGravity(
                Gravity.CENTER
        );

        dateBox.addView(
                calendar,
                new LinearLayout.LayoutParams(
                        dp(25),
                        -1
                )
        );

        dateText =
                new TextView(this);

        dateText.setTextColor(WHITE);

        dateText.setTextSize(14);

        dateText.setGravity(
                Gravity.CENTER_VERTICAL
        );

        dateText.setSingleLine(true);

        dateText.setEllipsize(
                TextUtils.TruncateAt.END
        );

        dateBox.addView(
                dateText,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1f
                )
        );

        right.addView(
                dateBox,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(43)
                )
        );

        LinearLayout.LayoutParams rightLp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        0.92f
                );

        header.addView(
                right,
                rightLp
        );

        main.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(198)
                )
        );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void buildSearch() {

        search =
                new EditText(this);

        search.setSingleLine(true);

        search.setTextSize(16);

        search.setTextColor(WHITE);

        search.setHintTextColor(MUTED);

        search.setHint(
                "⌕  Search notes, folders, tags..."
        );

        search.setPadding(
                dp(13),
                0,
                dp(13),
                0
        );

        search.setBackground(
                outline(
                        Color.TRANSPARENT,
                        Color.rgb(52, 70, 119),
                        29
                )
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        lp.topMargin = dp(9);

        lp.bottomMargin = dp(6);

        main.addView(
                search,
                lp
        );

        search.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        searchText =
                                s == null
                                        ? ""
                                        : s.toString();

                        refresh();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );
    }

    // =========================================================
    // TOP TABS
    // =========================================================

    private void buildTabs() {

        LinearLayout tabs =
                new LinearLayout(this);

        tabs.setOrientation(
                LinearLayout.HORIZONTAL
        );

        tabs.setGravity(
                Gravity.CENTER
        );

        tabNotes =
                createTab("Notes");

        tabFavorites =
                createTab("Favorites");

        tabArchive =
                createTab("Archive");

        tabTrash =
                createTab("Trash");

        tabs.addView(
                tabNotes,
                equalTabParams()
        );

        tabs.addView(
                tabFavorites,
                equalTabParams()
        );

        tabs.addView(
                tabArchive,
                equalTabParams()
        );

        tabs.addView(
                tabTrash,
                equalTabParams()
        );

        main.addView(
                tabs,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );

        tabNotes.setOnClickListener(v -> {

            bounce(v);

            changeMode("all");
        });

        tabFavorites.setOnClickListener(v -> {

            bounce(v);

            changeMode("favorites");
        });

        tabArchive.setOnClickListener(v -> {

            bounce(v);

            changeMode("archive");
        });

        tabTrash.setOnClickListener(v -> {

            bounce(v);

            changeMode("trash");
        });
    }

    private LinearLayout.LayoutParams equalTabParams() {

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(46),
                        1f
                );

        lp.leftMargin = dp(3);

        lp.rightMargin = dp(3);

        return lp;
    }

    private TextView createTab(
            String text) {

        TextView t =
                new TextView(this);

        t.setText(text);

        t.setTextSize(13);

        t.setGravity(
                Gravity.CENTER
        );

        t.setSingleLine(true);

        t.setTextColor(LIGHT);

        return t;
    }

    private void updateTabs() {

        setTab(
                tabNotes,
                mode.equals("all")
        );

        setTab(
                tabFavorites,
                mode.equals("favorites")
        );

        setTab(
                tabArchive,
                mode.equals("archive")
        );

        setTab(
                tabTrash,
                mode.equals("trash")
        );

        updateBottomNavigation();
    }

    private void setTab(
            TextView tab,
            boolean selected) {

        if (selected) {

            tab.setTextColor(WHITE);

            tab.setBackground(
                    gradient(
                            BLUE,
                            PURPLE,
                            BORDER,
                            25
                    )
            );

        } else {

            tab.setTextColor(LIGHT);

            tab.setBackground(
                    outline(
                            Color.TRANSPARENT,
                            Color.rgb(48, 65, 112),
                            25
                    )
            );
        }
    }

    private void changeMode(
            String value) {

        mode = value;

        selectionMode = false;

        selectedIds.clear();

        refresh();
    }

    // =========================================================
    // SELECTION BAR
    // =========================================================

    private void buildSelectionBar() {

        selectionBar =
                new LinearLayout(this);

        selectionBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        selectionBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        selectionBar.setPadding(
                dp(5),
                dp(3),
                dp(5),
                dp(3)
        );

        selectionBar.setBackground(
                gradient(
                        Color.rgb(16, 27, 54),
                        Color.rgb(29, 22, 57),
                        Color.rgb(55, 76, 135),
                        18
                )
        );

        selectionCount =
                new TextView(this);

        selectionCount.setTextColor(WHITE);

        selectionCount.setTextSize(10);

        selectionCount.setGravity(
                Gravity.CENTER_VERTICAL
        );

        selectionBar.addView(
                selectionCount,
                new LinearLayout.LayoutParams(
                        0,
                        dp(34),
                        1f
                )
        );

        selectAll =
                miniButton("SELECT ALL");

        deleteSelected =
                miniButton("DELETE");

        deleteAll =
                miniButton("ALL DELETE");

        selectionBar.addView(
                selectAll,
                new LinearLayout.LayoutParams(
                        dp(76),
                        dp(34)
                )
        );

        LinearLayout.LayoutParams deleteLp =
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(34)
                );

        deleteLp.leftMargin = dp(4);

        selectionBar.addView(
                deleteSelected,
                deleteLp
        );

        LinearLayout.LayoutParams allLp =
                new LinearLayout.LayoutParams(
                        dp(76),
                        dp(34)
                );

        allLp.leftMargin = dp(4);

        selectionBar.addView(
                deleteAll,
                allLp
        );

        main.addView(
                selectionBar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        selectionBar.setVisibility(
                View.GONE
        );

        selectAll.setOnClickListener(v -> {

            bounce(v);

            if (allSelected()) {

                selectedIds.clear();

            } else {

                selectAllVisible();
            }

            updateSelectionBar();

            refresh();
        });

        deleteSelected.setOnClickListener(v -> {

            bounce(v);

            deleteSelectedNotes();
        });

        deleteAll.setOnClickListener(v -> {

            bounce(v);

            confirmDeleteAll();
        });
    }

    private TextView miniButton(
            String text) {

        TextView v =
                new TextView(this);

        v.setText(text);

        v.setTextColor(LIGHT);

        v.setTextSize(8);

        v.setGravity(
                Gravity.CENTER
        );

        v.setSingleLine(true);

        v.setBackground(
                outline(
                        Color.rgb(22, 30, 55),
                        BORDER,
                        15
                )
        );

        return v;
    }

    private void selectAllVisible() {

        ArrayList<Note> list =
                db.query(
                        mode,
                        searchText,
                        sort
                );

        selectedIds.clear();

        if (list != null) {

            for (Note note : list) {

                selectedIds.add(
                        note.id
                );
            }
        }
    }

    private boolean allSelected() {

        ArrayList<Note> list =
                db.query(
                        mode,
                        searchText,
                        sort
                );

        return list != null
                && !list.isEmpty()
                && selectedIds.size()
                == list.size();
    }

    private void updateSelectionBar() {

        if (!selectionMode) {

            selectionBar.setVisibility(
                    View.GONE
            );

            return;
        }

        selectionBar.setVisibility(
                View.VISIBLE
        );

        selectionCount.setText(
                selectedIds.size()
                        + " selected"
        );

        selectAll.setText(
                allSelected()
                        ? "CLEAR"
                        : "SELECT ALL"
        );
    }

    // =========================================================
    // NOTES
    // =========================================================

    private void buildNotes() {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        scroll.setVerticalScrollBarEnabled(false);

        notesContainer =
                new LinearLayout(this);

        notesContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        notesContainer.setPadding(
                0,
                dp(6),
                0,
                dp(190)
        );

        scroll.addView(
                notesContainer,
                new ScrollView.LayoutParams(
                        -1,
                        -2
                )
        );

        main.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1f
                )
        );
    }

    private void refresh() {

        if (notesContainer == null) {
            return;
        }

        notesContainer.removeAllViews();

        ArrayList<Note> list =
                db.query(
                        mode,
                        searchText,
                        sort
                );

        if (list == null
                || list.isEmpty()) {

            emptyCard();

        } else {

            for (Note note : list) {

                View card =
                        noteCard(note);

                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(
                                -1,
                                -2
                        );

                lp.topMargin = dp(5);

                lp.bottomMargin = dp(5);

                notesContainer.addView(
                        card,
                        lp
                );
            }
        }

        updateTabs();

        updateSelectionBar();
    }

    // =========================================================
    // FIXED FAB
    // =========================================================

    private void buildFloatingButton() {

        floatingPlus =
                new TextView(this);

        floatingPlus.setText("+");

        floatingPlus.setTextColor(WHITE);

        floatingPlus.setTextSize(34);

        floatingPlus.setGravity(
                Gravity.CENTER
        );

        floatingPlus.setBackground(
                gradient(
                        BLUE,
                        PURPLE,
                        BORDER,
                        60
                )
        );

        floatingPlus.setElevation(
                dp(10)
        );

        floatingPlus.setOnClickListener(v -> {

            bounce(v);

            openEditor(-1);
        });

        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(
                        dp(62),
                        dp(62)
                );

        lp.gravity =
                Gravity.BOTTOM |
                        Gravity.RIGHT;

        lp.rightMargin =
                dp(24);

        lp.bottomMargin =
                navigationInset + dp(102);

        root.addView(
                floatingPlus,
                lp
        );
    }

    // =========================================================
    // NOTE CARD
    // =========================================================

    private View noteCard(
            Note note) {

        int colorIndex =
                note.color >= 0
                        && note.color < NOTE_COLORS.length
                        ? note.color
                        : 0;

        int noteColor =
                NOTE_COLORS[colorIndex];

        boolean selected =
                selectedIds.contains(note.id);

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(11),
                dp(9),
                dp(11),
                dp(9)
        );

        card.setBackground(
                gradient(
                        selected
                                ? Color.rgb(28, 45, 84)
                                : noteColor,

                        selected
                                ? Color.rgb(48, 29, 81)
                                : darken(noteColor),

                        selected
                                ? Color.rgb(112, 136, 235)
                                : Color.rgb(53, 70, 116),

                        22
                )
        );

        LinearLayout top =
                new LinearLayout(this);

        top.setOrientation(
                LinearLayout.HORIZONTAL
        );

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView dot =
                new TextView(this);

        dot.setBackground(
                outline(
                        noteColor,
                        Color.rgb(155, 175, 242),
                        60
                )
        );

        top.addView(
                dot,
                new LinearLayout.LayoutParams(
                        dp(34),
                        dp(34)
                )
        );

        TextView title =
                new TextView(this);

        String titleValue =
                note.title == null
                        || note.title.trim().isEmpty()
                        ? "Untitled"
                        : note.title.trim();

        title.setText(
                titleValue
        );

        title.setTextColor(WHITE);

        title.setTextSize(16);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setSingleLine(true);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams titleLp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(38),
                        1f
                );

        titleLp.leftMargin = dp(9);

        top.addView(
                title,
                titleLp
        );

        TextView menu =
                roundButton("⋮");

        menu.setTextSize(21);

        menu.setOnClickListener(v -> {

            bounce(v);

            showNoteMenu(note);
        });

        top.addView(
                menu,
                new LinearLayout.LayoutParams(
                        dp(40),
                        dp(40)
                )
        );

        card.addView(
                top,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        String bodyValue =
                note.body == null
                        ? ""
                        : plain(note.body);

        if (bodyValue.trim().isEmpty()) {

            bodyValue = "No content";
        }

        TextView body =
                new TextView(this);

        body.setText(
                bodyValue.trim()
        );

        body.setTextColor(
                Color.rgb(
                        190,
                        201,
                        226
                )
        );

        body.setTextSize(12);

        body.setSingleLine(true);

        body.setEllipsize(
                TextUtils.TruncateAt.END
        );

        card.addView(
                body,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(29)
                )
        );

        LinearLayout meta =
                new LinearLayout(this);

        meta.setOrientation(
                LinearLayout.HORIZONTAL
        );

        meta.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView folder =
                new TextView(this);

        String folderName =
                note.folder == null
                        || note.folder.isEmpty()
                        ? "General"
                        : note.folder;

        folder.setText(
                "▱  " + folderName
        );

        folder.setTextColor(LIGHT);

        folder.setTextSize(11);

        folder.setSingleLine(true);

        meta.addView(
                folder,
                new LinearLayout.LayoutParams(
                        0,
                        dp(30),
                        1f
                )
        );

        TextView date =
                new TextView(this);

        date.setText(
                formatDate(note.updated)
        );

        date.setTextColor(LIGHT);

        date.setTextSize(10);

        date.setGravity(
                Gravity.RIGHT |
                        Gravity.CENTER_VERTICAL
        );

        date.setMaxLines(2);

        meta.addView(
                date,
                new LinearLayout.LayoutParams(
                        dp(135),
                        dp(35)
                )
        );

        card.addView(
                meta,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(35)
                )
        );

        LinearLayout actions =
                new LinearLayout(this);

        actions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        actions.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView favorite =
                action(
                        note.isFavorite()
                                ? "♥ Favorite"
                                : "♡ Favorite"
                );

        TextView archive =
                action(
                        note.isArchived()
                                ? "□ Restore"
                                : "□ Archive"
                );

        TextView share =
                action(
                        "↥ Share"
                );

        favorite.setOnClickListener(v -> {

            bounce(v);

            db.favorite(
                    note.id,
                    !note.isFavorite()
            );

            refresh();
        });

        archive.setOnClickListener(v -> {

            bounce(v);

            db.archive(
                    note.id,
                    !note.isArchived()
            );

            refresh();
        });

        share.setOnClickListener(v -> {

            bounce(v);

            shareNote(note);
        });

        actions.addView(
                favorite,
                actionLp()
        );

        actions.addView(
                archive,
                actionLp()
        );

        actions.addView(
                share,
                actionLp()
        );

        card.addView(
                actions,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(36)
                )
        );

        card.setOnClickListener(v -> {

            bounce(v);

            if (selectionMode) {

                toggleSelection(note.id);

                refresh();

            } else {

                openEditor(note.id);
            }
        });

        card.setOnLongClickListener(v -> {

            bounce(v);

            selectionMode = true;

            if (!selectedIds.contains(note.id)) {

                selectedIds.add(note.id);
            }

            refresh();

            return true;
        });

        return card;
    }

    private LinearLayout.LayoutParams actionLp() {

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(35),
                        1f
                );

        lp.leftMargin = dp(2);

        lp.rightMargin = dp(2);

        return lp;
    }

    private TextView action(
            String text) {

        TextView v =
                new TextView(this);

        v.setText(text);

        v.setTextColor(WHITE);

        v.setTextSize(10);

        v.setGravity(
                Gravity.CENTER
        );

        v.setSingleLine(true);

        v.setEllipsize(
                TextUtils.TruncateAt.END
        );

        v.setBackground(
                outline(
                        Color.rgb(26, 34, 57),
                        BORDER,
                        18
                )
        );

        return v;
    }

    // =========================================================
    // EMPTY
    // =========================================================

    private void emptyCard() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

        card.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
        );

        card.setBackground(
                gradient(
                        Color.rgb(14, 23, 43),
                        Color.rgb(31, 23, 57),
                        BORDER,
                        24
                )
        );

        TextView icon =
                new TextView(this);

        icon.setText("✦");

        icon.setTextColor(LIGHT);

        icon.setTextSize(35);

        icon.setGravity(
                Gravity.CENTER
        );

        card.addView(
                icon,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );

        TextView title =
                new TextView(this);

        String emptyTitle;

        if (mode.equals("trash")) {

            emptyTitle = "Trash is empty";

        } else if (mode.equals("favorites")) {

            emptyTitle = "No favorite notes";

        } else if (mode.equals("archive")) {

            emptyTitle = "No archived notes";

        } else {

            emptyTitle = "No notes yet";
        }

        title.setText(emptyTitle);

        title.setTextColor(WHITE);

        title.setTextSize(19);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        card.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(38)
                )
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Tap + to create your first note"
        );

        subtitle.setTextColor(MUTED);

        subtitle.setTextSize(14);

        subtitle.setGravity(
                Gravity.CENTER
        );

        card.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(36)
                )
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(205)
                );

        lp.topMargin = dp(7);

        lp.bottomMargin = dp(7);

        notesContainer.addView(
                card,
                lp
        );
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void buildBottomBar() {

        bottomBar =
                new LinearLayout(this);

        bottomBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bottomBar.setGravity(
                Gravity.CENTER
        );

        bottomBar.setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(3)
        );

        bottomBar.setBackground(
                gradient(
                        Color.rgb(20, 38, 81),
                        Color.rgb(48, 25, 73),
                        BORDER,
                        28
                )
        );

        bottomItem(
                "⌂",
                "Notes",
                "all"
        );

        bottomItem(
                "★",
                "Favorites",
                "favorites"
        );

        bottomItem(
                "□",
                "Archive",
                "archive"
        );

        bottomItem(
                "⚙",
                "Settings",
                "settings"
        );

        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(82)
                );

        lp.gravity =
                Gravity.BOTTOM |
                        Gravity.CENTER_HORIZONTAL;

        lp.leftMargin = dp(16);

        lp.rightMargin = dp(16);

        lp.bottomMargin =
                navigationInset + dp(8);

        root.addView(
                bottomBar,
                lp
        );
    }

    /*
     * NEW:
     *
     * Every bottom navigation item has:
     *
     * 1. icon
     * 2. label
     * 3. active indicator
     *
     * Only the currently selected item shows
     * the bright purple indicator.
     */

    private void bottomItem(
            String icon,
            String label,
            String action) {

        LinearLayout item =
                new LinearLayout(this);

        item.setOrientation(
                LinearLayout.VERTICAL
        );

        item.setGravity(
                Gravity.CENTER
        );

        TextView iconText =
                new TextView(this);

        iconText.setText(icon);

        iconText.setTextColor(LIGHT);

        iconText.setTextSize(
                icon.equals("★")
                        ? 32
                        : 25
        );

        iconText.setGravity(
                Gravity.CENTER
        );

        iconText.setIncludeFontPadding(false);

        item.addView(
                iconText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(38)
                )
        );

        TextView labelText =
                new TextView(this);

        labelText.setText(label);

        labelText.setTextColor(LIGHT);

        labelText.setTextSize(12);

        labelText.setGravity(
                Gravity.CENTER
        );

        labelText.setSingleLine(true);

        labelText.setIncludeFontPadding(false);

        item.addView(
                labelText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                )
        );

        /*
         * ACTIVE INDICATOR
         */

        View indicator =
                new View(this);

        indicator.setTag(
                "bottom_indicator_" + action
        );

        LinearLayout.LayoutParams indicatorLp =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(3)
                );

        indicatorLp.gravity =
                Gravity.CENTER_HORIZONTAL;

        indicatorLp.topMargin =
                dp(2);

        item.addView(
                indicator,
                indicatorLp
        );

        item.setTag(
                "bottom_item_" + action
        );

        item.setOnClickListener(v -> {

            bounce(v);

            if (action.equals("settings")) {

                showSettings();

            } else {

                changeMode(action);
            }
        });

        bottomBar.addView(
                item,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1f
                )
        );
    }

    /*
     * Update the four bottom navigation items.
     */

    private void updateBottomNavigation() {

        if (bottomBar == null) {
            return;
        }

        updateBottomItem(
                "all",
                mode.equals("all")
        );

        updateBottomItem(
                "favorites",
                mode.equals("favorites")
        );

        updateBottomItem(
                "archive",
                mode.equals("archive")
        );

        /*
         * Settings isn't a mode, therefore it
         * becomes active while its dialog is open
         * only through the normal settings click.
         *
         * Keep it inactive on the main content.
         */

        updateBottomItem(
                "settings",
                false
        );
    }

    private void updateBottomItem(
            String action,
            boolean active) {

        View item =
                bottomBar.findViewWithTag(
                        "bottom_item_" + action
                );

        if (!(item instanceof LinearLayout)) {
            return;
        }

        LinearLayout layout =
                (LinearLayout) item;

        if (layout.getChildCount() < 3) {
            return;
        }

        TextView icon =
                (TextView)
                        layout.getChildAt(0);

        TextView label =
                (TextView)
                        layout.getChildAt(1);

        View indicator =
                layout.getChildAt(2);

        if (active) {

            icon.setTextColor(
                    ACTIVE_PURPLE
            );

            label.setTextColor(
                    WHITE
            );

            indicator.setVisibility(
                    View.VISIBLE
            );

            indicator.setBackground(
                    gradient(
                            BLUE,
                            PURPLE,
                            Color.TRANSPARENT,
                            10
                    )
            );

            item.setAlpha(1f);

        } else {

            icon.setTextColor(
                    LIGHT
            );

            label.setTextColor(
                    LIGHT
            );

            indicator.setVisibility(
                    View.VISIBLE
            );

            indicator.setBackground(
                    outline(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                            10
                    )
            );

            item.setAlpha(0.82f);
        }
    }

    // =========================================================
    // SHARE
    // =========================================================

    private void shareNote(
            Note note) {

        String title =
                note.title == null
                        || note.title.trim().isEmpty()
                        ? "Untitled"
                        : note.title.trim();

        String body =
                note.body == null
                        ? ""
                        : plain(note.body);

        StringBuilder message =
                new StringBuilder();

        message.append(title);

        message.append("\n\n");

        if (!body.trim().isEmpty()) {

            message.append(
                    body.trim()
            );

            message.append("\n\n");
        }

        message.append("Folder: ");

        message.append(
                note.folder == null
                        || note.folder.isEmpty()
                        ? "General"
                        : note.folder
        );

        if (note.tags != null
                && !note.tags.trim().isEmpty()) {

            message.append("\nTags: ");

            message.append(
                    note.tags.trim()
            );
        }

        Intent intent =
                new Intent(
                        Intent.ACTION_SEND
                );

        intent.setType(
                "text/plain"
        );

        intent.putExtra(
                Intent.EXTRA_SUBJECT,
                title
        );

        intent.putExtra(
                Intent.EXTRA_TEXT,
                message.toString()
        );

        startActivity(
                Intent.createChooser(
                        intent,
                        "Share note"
                )
        );
    }

    // =========================================================
    // NOTE MENU
    // =========================================================

    private void showNoteMenu(
            Note note) {

        Dialog dialog =
                glassDialog();

        LinearLayout box =
                dialogBox();

        dialogItem(
                box,
                "Edit",
                () -> {

                    dialog.dismiss();

                    openEditor(note.id);
                }
        );

        dialogItem(
                box,
                "Share",
                () -> {

                    dialog.dismiss();

                    shareNote(note);
                }
        );

        dialogItem(
                box,
                "Change color",
                () -> {

                    dialog.dismiss();

                    showColors(note);
                }
        );

        dialogItem(
                box,
                "Duplicate",
                () -> {

                    dialog.dismiss();

                    db.duplicate(note.id);

                    refresh();
                }
        );

        dialogItem(
                box,
                note.isPinned()
                        ? "Unpin"
                        : "Pin",
                () -> {

                    dialog.dismiss();

                    db.pin(
                            note.id,
                            !note.isPinned()
                    );

                    refresh();
                }
        );

        dialogItem(
                box,
                "Select",
                () -> {

                    dialog.dismiss();

                    selectionMode = true;

                    selectedIds.clear();

                    selectedIds.add(note.id);

                    refresh();
                }
        );

        if (mode.equals("trash")) {

            dialogItem(
                    box,
                    "Restore",
                    () -> {

                        dialog.dismiss();

                        db.restore(note.id);

                        refresh();
                    }
            );

            dialogItem(
                    box,
                    "Delete permanently",
                    () -> {

                        dialog.dismiss();

                        confirmPermanent(note.id);
                    }
            );

        } else {

            dialogItem(
                    box,
                    "Move to trash",
                    () -> {

                        dialog.dismiss();

                        db.trash(note.id);

                        refresh();
                    }
            );
        }

        dialog.setContentView(box);

        dialog.show();

        styleDialog(dialog, 0.91f);
    }

    // =========================================================
    // COLOR
    // =========================================================

    private void showColors(
            Note note) {

        Dialog dialog =
                glassDialog();

        LinearLayout box =
                dialogBox();

        TextView title =
                new TextView(this);

        title.setText(
                "Choose note color"
        );

        title.setTextColor(WHITE);

        title.setTextSize(20);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        box.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        scroll.setHorizontalScrollBarEnabled(
                false
        );

        LinearLayout colors =
                new LinearLayout(this);

        colors.setOrientation(
                LinearLayout.HORIZONTAL
        );

        for (int i = 0;
             i < NOTE_COLORS.length;
             i++) {

            final int index = i;

            TextView color =
                    new TextView(this);

            color.setText("•");

            color.setTextColor(WHITE);

            color.setTextSize(27);

            color.setGravity(
                    Gravity.CENTER
            );

            color.setBackground(
                    outline(
                            NOTE_COLORS[i],
                            BORDER,
                            60
                    )
            );

            color.setOnClickListener(v -> {

                bounce(v);

                db.color(
                        note.id,
                        index
                );

                dialog.dismiss();

                refresh();
            });

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            dp(62),
                            dp(62)
                    );

            lp.leftMargin = dp(4);

            lp.rightMargin = dp(4);

            colors.addView(
                    color,
                    lp
            );
        }

        scroll.addView(
                colors,
                new HorizontalScrollView
                        .LayoutParams(
                                -2,
                                dp(70)
                        )
        );

        box.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(75)
                )
        );

        TextView close =
                new TextView(this);

        close.setText("CLOSE");

        close.setTextColor(LIGHT);

        close.setTextSize(14);

        close.setGravity(
                Gravity.CENTER
        );

        close.setOnClickListener(v -> {

            bounce(v);

            dialog.dismiss();
        });

        box.addView(
                close,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        dialog.setContentView(box);

        dialog.show();

        styleDialog(dialog, 0.91f);
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void showSettings() {

        Dialog dialog =
                glassDialog();

        LinearLayout box =
                dialogBox();

        TextView title =
                new TextView(this);

        title.setText("Settings");

        title.setTextColor(WHITE);

        title.setTextSize(20);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        box.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        dialogItem(
                box,
                "Sort by updated",
                () -> {

                    sort = "updated";

                    dialog.dismiss();

                    refresh();
                }
        );

        dialogItem(
                box,
                "Sort by oldest",
                () -> {

                    sort = "oldest";

                    dialog.dismiss();

                    refresh();
                }
        );

        dialogItem(
                box,
                "Sort by title",
                () -> {

                    sort = "title";

                    dialog.dismiss();

                    refresh();
                }
        );

        dialogItem(
                box,
                AppLock.isEnabled(this)
                        ? "🔓 Disable App Lock"
                        : "🔐 Enable App Lock",
                () -> {

                    dialog.dismiss();

                    if (AppLock.isEnabled(this)) {
                        showDisableLock();
                    } else {
                        showEnableLock();
                    }
                }
        );

        dialogItem(
                box,
                "Select notes",
                () -> {

                    dialog.dismiss();

                    selectionMode = true;

                    refresh();
                }
        );

        dialogItem(
                box,
                "Delete all",
                () -> {

                    dialog.dismiss();

                    confirmDeleteAll();
                }
        );

        dialogItem(
                box,
                "Close",
                dialog::dismiss
        );

        dialog.setContentView(box);

        dialog.show();

        styleDialog(dialog, 0.91f);
    }

    // =========================================================
    // APP LOCK
    // =========================================================

    private void showEnableLock() {

        Dialog dialog =
                glassDialog();

        LinearLayout box =
                dialogBox();

        TextView title =
                new TextView(this);

        title.setText(
                "🔐 Create App Lock"
        );

        title.setTextColor(WHITE);
        title.setTextSize(19);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        box.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        EditText pin =
                new EditText(this);

        pin.setHint(
                "4–8 digit PIN"
        );

        pin.setTextColor(WHITE);
        pin.setHintTextColor(MUTED);
        pin.setTextSize(18);

        pin.setGravity(
                Gravity.CENTER
        );

        pin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        pin.setSingleLine(true);

        pin.setBackground(
                outline(
                        Color.rgb(18, 25, 48),
                        BORDER,
                        20
                )
        );

        box.addView(
                pin,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        TextView save =
                miniButton("ENABLE");

        LinearLayout.LayoutParams saveLp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(44)
                );

        saveLp.topMargin = dp(12);

        box.addView(
                save,
                saveLp
        );

        save.setOnClickListener(v -> {

            bounce(v);

            String value =
                    pin.getText()
                            .toString()
                            .trim();

            if (
                    value.length() < 4
                            || value.length() > 8
            ) {

                Toast.makeText(
                        this,
                        "PIN must be 4–8 digits",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            AppLock.enable(
                    this,
                    value
            );

            dialog.dismiss();

            Toast.makeText(
                    this,
                    "App Lock enabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        dialog.setContentView(box);

        dialog.show();

        styleDialog(
                dialog,
                0.91f
        );
    }

    private void showDisableLock() {

        Dialog dialog =
                glassDialog();

        LinearLayout box =
                dialogBox();

        TextView title =
                new TextView(this);

        title.setText(
                "🔓 Disable App Lock"
        );

        title.setTextColor(WHITE);
        title.setTextSize(19);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        box.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        EditText pin =
                new EditText(this);

        pin.setHint(
                "Current PIN"
        );

        pin.setTextColor(WHITE);
        pin.setHintTextColor(MUTED);
        pin.setTextSize(18);

        pin.setGravity(
                Gravity.CENTER
        );

        pin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        pin.setSingleLine(true);

        pin.setBackground(
                outline(
                        Color.rgb(18, 25, 48),
                        BORDER,
                        20
                )
        );

        box.addView(
                pin,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        TextView disable =
                miniButton("DISABLE");

        LinearLayout.LayoutParams disableLp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(44)
                );

        disableLp.topMargin = dp(12);

        box.addView(
                disable,
                disableLp
        );

        disable.setOnClickListener(v -> {

            bounce(v);

            if (
                    AppLock.verify(
                            this,
                            pin.getText()
                                    .toString()
                    )
            ) {

                AppLock.disable(this);

                dialog.dismiss();

                Toast.makeText(
                        this,
                        "App Lock disabled",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                pin.setText("");

                Toast.makeText(
                        this,
                        "Wrong PIN",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        dialog.setContentView(box);

        dialog.show();

        styleDialog(
                dialog,
                0.91f
        );
    }

    // =========================================================
    // DELETE SELECTED
    // =========================================================

    private void deleteSelectedNotes() {

        if (selectedIds.isEmpty()) {
            return;
        }

        ArrayList<Long> ids =
                new ArrayList<>(
                        selectedIds
                );

        boolean permanent =
                mode.equals("trash");

        confirm(
                permanent
                        ? "Delete selected permanently?"
                        : "Move selected to Trash?",

                permanent
                        ? "Selected notes cannot be recovered."
                        : "Selected notes will be moved to Trash.",

                "DELETE",

                () -> {

                    for (Long id : ids) {

                        if (permanent) {

                            db.deleteForever(id);

                        } else {

                            db.trash(id);
                        }
                    }

                    selectedIds.clear();

                    selectionMode = false;

                    refresh();
                }
        );
    }

    // =========================================================
    // DELETE ALL
    // =========================================================

    private void confirmDeleteAll() {

        ArrayList<Note> list =
                db.query(
                        mode,
                        searchText,
                        sort
                );

        if (list == null
                || list.isEmpty()) {

            return;
        }

        boolean permanent =
                mode.equals("trash");

        confirm(
                permanent
                        ? "Delete all permanently?"
                        : "Move all to Trash?",

                permanent
                        ? "All visible trash notes will be permanently deleted."
                        : "All visible notes will be moved to Trash.",

                "DELETE ALL",

                () -> {

                    for (Note note : list) {

                        if (permanent) {

                            db.deleteForever(note.id);

                        } else {

                            db.trash(note.id);
                        }
                    }

                    selectedIds.clear();

                    selectionMode = false;

                    refresh();
                }
        );
    }

    private void confirmPermanent(
            long id) {

        confirm(
                "Delete permanently?",
                "This note cannot be recovered.",
                "DELETE",

                () -> {

                    db.deleteForever(id);

                    refresh();
                }
        );
    }

    // =========================================================
    // CONFIRM
    // =========================================================

    private void confirm(
            String title,
            String message,
            String action,
            Runnable runnable) {

        Dialog dialog =
                glassDialog();

        LinearLayout box =
                dialogBox();

        TextView titleView =
                new TextView(this);

        titleView.setText(title);

        titleView.setTextColor(WHITE);

        titleView.setTextSize(18);

        titleView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        box.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(46)
                )
        );

        TextView messageView =
                new TextView(this);

        messageView.setText(message);

        messageView.setTextColor(MUTED);

        messageView.setTextSize(13);

        box.addView(
                messageView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setGravity(
                Gravity.RIGHT
        );

        TextView cancel =
                miniButton("CANCEL");

        TextView ok =
                miniButton(action);

        cancel.setOnClickListener(v -> {

            bounce(v);

            dialog.dismiss();
        });

        ok.setOnClickListener(v -> {

            bounce(v);

            dialog.dismiss();

            runnable.run();
        });

        buttons.addView(
                cancel,
                new LinearLayout.LayoutParams(
                        dp(85),
                        dp(40)
                )
        );

        LinearLayout.LayoutParams okLp =
                new LinearLayout.LayoutParams(
                        dp(105),
                        dp(40)
                );

        okLp.leftMargin = dp(6);

        buttons.addView(
                ok,
                okLp
        );

        box.addView(
                buttons,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(43)
                )
        );

        dialog.setContentView(box);

        dialog.show();

        styleDialog(dialog, 0.91f);
    }

    // =========================================================
    // DIALOG
    // =========================================================

    private Dialog glassDialog() {

        Dialog dialog =
                new Dialog(this);

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        dialog.setCanceledOnTouchOutside(
                true
        );

        return dialog;
    }

    private LinearLayout dialogBox() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(16)
        );

        box.setBackground(
                gradient(
                        Color.rgb(20, 29, 57),
                        Color.rgb(31, 23, 61),
                        BORDER,
                        27
                )
        );

        return box;
    }

    private void dialogItem(
            LinearLayout box,
            String text,
            Runnable action) {

        TextView item =
                new TextView(this);

        item.setText(text);

        item.setTextColor(WHITE);

        item.setTextSize(15);

        item.setGravity(
                Gravity.CENTER_VERTICAL
        );

        item.setSingleLine(true);

        item.setOnClickListener(v -> {

            bounce(v);

            action.run();
        });

        box.addView(
                item,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(51)
                )
        );
    }

    private void styleDialog(
            Dialog dialog,
            float ratio) {

        Window window =
                dialog.getWindow();

        if (window == null) {
            return;
        }

        window.setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT
                )
        );

        WindowManager.LayoutParams lp =
                window.getAttributes();

        lp.width =
                (int)
                        (
                                getResources()
                                        .getDisplayMetrics()
                                        .widthPixels
                                        * ratio
                        );

        lp.height =
                WindowManager.LayoutParams.WRAP_CONTENT;

        lp.dimAmount = 0.73f;

        window.addFlags(
                WindowManager.LayoutParams
                        .FLAG_DIM_BEHIND
        );

        window.setAttributes(lp);
    }

    // =========================================================
    // EDITOR
    // =========================================================

    private void openEditor(
            long id) {

        Intent intent =
                new Intent(
                        this,
                        EditorActivity.class
                );

        intent.putExtra(
                "note_id",
                id
        );

        startActivity(intent);
    }

    // =========================================================
    // CLOCK
    // =========================================================

    private void startClock() {

        updateClock();

        clockHandler.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        updateClock();

                        clockHandler.postDelayed(
                                this,
                                30000
                        );
                    }
                },
                30000
        );
    }

    private void updateClock() {

        if (dayText == null
                || timeText == null
                || dateText == null) {

            return;
        }

        Date now =
                new Date();

        Locale locale =
                Locale.getDefault();

        dayText.setText(
                new SimpleDateFormat(
                        "EEEE",
                        locale
                ).format(now)
        );

        timeText.setText(
                new SimpleDateFormat(
                        "hh:mm a",
                        locale
                ).format(now)
        );

        dateText.setText(
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        locale
                ).format(now)
        );
    }

    // =========================================================
    // SELECTION
    // =========================================================

    private void toggleSelection(
            long id) {

        if (selectedIds.contains(id)) {

            selectedIds.remove(id);

        } else {

            selectedIds.add(id);
        }
    }

    // =========================================================
    // DATE
    // =========================================================

    private String formatDate(
            long timestamp) {

        if (timestamp <= 0) {
            return "";
        }

        return new SimpleDateFormat(
                "dd MMM yyyy\nhh:mm a",
                Locale.getDefault()
        ).format(
                new Date(timestamp)
        );
    }

    // =========================================================
    // HTML
    // =========================================================

    private String plain(
            String html) {

        if (html == null) {
            return "";
        }

        try {

            return android.text.Html
                    .fromHtml(
                            html,
                            android.text.Html
                                    .FROM_HTML_MODE_LEGACY
                    )
                    .toString()
                    .replace(
                            "\u00a0",
                            " "
                    )
                    .trim();

        } catch (Exception e) {

            return html
                    .replaceAll(
                            "<[^>]*>",
                            ""
                    )
                    .trim();
        }
    }

    // =========================================================
    // GRADIENT
    // =========================================================

    private GradientDrawable gradient(
            int top,
            int bottom,
            int stroke,
            int radius) {

        GradientDrawable drawable =
                new GradientDrawable(
                        GradientDrawable
                                .Orientation
                                .TL_BR,
                        new int[]{
                                top,
                                bottom
                        }
                );

        drawable.setCornerRadius(
                dp(radius)
        );

        drawable.setStroke(
                dp(2),
                stroke
        );

        return drawable;
    }

    // =========================================================
    // OUTLINE
    // =========================================================

    private GradientDrawable outline(
            int fill,
            int stroke,
            int radius) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(fill);

        drawable.setCornerRadius(
                dp(radius)
        );

        drawable.setStroke(
                dp(2),
                stroke
        );

        return drawable;
    }

    // =========================================================
    // ROUND BUTTON
    // =========================================================

    private TextView roundButton(
            String text) {

        TextView button =
                new TextView(this);

        button.setText(text);

        button.setTextColor(WHITE);

        button.setGravity(
                Gravity.CENTER
        );

        button.setBackground(
                outline(
                        Color.TRANSPARENT,
                        BORDER,
                        60
                )
        );

        return button;
    }

    // =========================================================
    // BOUNCE
    // =========================================================

    private void bounce(
            View view) {

        view.animate().cancel();

        view.setScaleX(1f);

        view.setScaleY(1f);

        view.animate()
                .scaleX(0.93f)
                .scaleY(0.93f)
                .setDuration(55)
                .withEndAction(() -> {

                    view.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(70)
                            .withEndAction(() -> {

                                view.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(65)
                                        .start();

                            })
                            .start();

                })
                .start();
    }

    // =========================================================
    // DARKEN
    // =========================================================

    private int darken(
            int color) {

        return Color.rgb(
                Math.max(
                        0,
                        Color.red(color) - 8
                ),
                Math.max(
                        0,
                        Color.green(color) - 8
                ),
                Math.max(
                        0,
                        Color.blue(color) - 8
                )
        );
    }

    // =========================================================
    // DP
    // =========================================================

    private int dp(
            int value) {

        return (int)
                (
                        value
                                * getResources()
                                        .getDisplayMetrics()
                                        .density
                                + 0.5f
                );
    }
}
