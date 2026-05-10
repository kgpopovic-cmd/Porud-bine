package com.alfa.orders;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    private LinearLayout list, statusBar;
    private EditText search;
    private TextView countView, totalView, urgentView;
    private final ArrayList<Order> orders = new ArrayList<>();
    private String filter = "Sve";
    private final String[] statuses = {"Sve", "Nova", "U izradi", "Spremno", "Isporučeno"};

    static class Order {
        String id, customer, phone, product, deadline, status, note;
        int amount;
        Order(String id, String customer, String phone, String product, int amount, String deadline, String status, String note) {
            this.id=id; this.customer=customer; this.phone=phone; this.product=product; this.amount=amount; this.deadline=deadline; this.status=status; this.note=note;
        }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(24,24,27));
        loadOrders();
        buildUi();
        render();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245,245,244));
        scroll.addView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(22), dp(20), dp(18));
        header.setBackgroundColor(Color.rgb(24,24,27));
        root.addView(header);

        TextView brand = text("Alfa Namestaj", 14, Color.rgb(180,180,185), false);
        TextView title = text("Organizer porudžbina", 26, Color.WHITE, true);
        header.addView(brand); header.addView(title);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setPadding(0, dp(16), 0, 0);
        header.addView(stats);
        countView = statBox(stats, "Porudžbine");
        totalView = statBox(stats, "Ukupno €");
        urgentView = statBox(stats, "Hitno");

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(dp(16), dp(16), dp(16), dp(8));
        root.addView(controls);

        Button add = new Button(this);
        add.setText("+ Nova porudžbina");
        add.setTextColor(Color.WHITE);
        add.setBackgroundColor(Color.rgb(24,24,27));
        add.setOnClickListener(v -> showAddDialog());
        controls.addView(add, new LinearLayout.LayoutParams(-1, dp(48)));

        search = new EditText(this);
        search.setHint("Pretraži kupca, proizvod ili broj...");
        search.setSingleLine(true);
        search.setPadding(dp(12), 0, dp(12), 0);
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int b,int c){ render(); }
            public void afterTextChanged(android.text.Editable e){}
        });
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(50)); sp.setMargins(0, dp(12),0,dp(8));
        controls.addView(search, sp);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        statusBar = new LinearLayout(this);
        statusBar.setOrientation(LinearLayout.HORIZONTAL);
        hsv.addView(statusBar);
        controls.addView(hsv);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(16), dp(8), dp(16), dp(30));
        root.addView(list);
        setContentView(scroll);
    }

    private TextView statBox(LinearLayout parent, String label) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(10), dp(10), dp(10));
        box.setBackgroundColor(Color.rgb(45,45,50));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(78), 1); lp.setMargins(dp(3),0,dp(3),0);
        parent.addView(box, lp);
        TextView value = text("0", 22, Color.WHITE, true);
        TextView lab = text(label, 11, Color.rgb(205,205,210), false);
        box.addView(value); box.addView(lab);
        return value;
    }

    private void render() {
        statusBar.removeAllViews();
        for (String s: statuses) {
            Button b = new Button(this); b.setText(s); b.setTextSize(12); b.setAllCaps(false);
            b.setTextColor(s.equals(filter)?Color.WHITE:Color.rgb(40,40,45));
            b.setBackgroundColor(s.equals(filter)?Color.rgb(24,24,27):Color.WHITE);
            b.setOnClickListener(v -> { filter = ((Button)v).getText().toString(); render(); });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(42)); lp.setMargins(0,0,dp(8),0);
            statusBar.addView(b, lp);
        }

        list.removeAllViews();
        String q = search == null ? "" : search.getText().toString().toLowerCase(Locale.ROOT);
        ArrayList<Order> visible = new ArrayList<>();
        for (Order o: orders) {
            String hay = (o.id+" "+o.customer+" "+o.phone+" "+o.product+" "+o.note).toLowerCase(Locale.ROOT);
            if ((filter.equals("Sve") || o.status.equals(filter)) && hay.contains(q)) visible.add(o);
        }
        Collections.sort(visible, Comparator.comparing(o -> o.deadline));
        int total = 0, urgent = 0;
        for (Order o: visible) { total += o.amount; if (isUrgent(o)) urgent++; addCard(o); }
        countView.setText(String.valueOf(visible.size())); totalView.setText(String.valueOf(total)); urgentView.setText(String.valueOf(urgent));
        if (visible.isEmpty()) list.addView(text("Nema pronađenih porudžbina.", 16, Color.rgb(90,90,95), false));
    }

    private void addCard(Order o) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2); cp.setMargins(0,0,0,dp(12));
        list.addView(card, cp);
        TextView top = text(o.id + "   •   " + o.status + "   •   " + o.amount + "€", 13, Color.rgb(100,100,105), true);
        TextView product = text(o.product, 20, Color.rgb(24,24,27), true);
        TextView details = text(o.customer + "\n" + o.phone + "\nRok: " + niceDate(o.deadline), 15, Color.rgb(70,70,75), false);
        card.addView(top); card.addView(product); card.addView(details);
        if (o.note != null && !o.note.isEmpty()) card.addView(text("Napomena: " + o.note, 14, Color.rgb(90,90,95), false));
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(0,dp(8),0,0); card.addView(row);
        for (int i=1; i<statuses.length; i++) {
            String s = statuses[i]; Button b = new Button(this); b.setText(s.equals("U izradi")?"Izrada":s); b.setTextSize(10); b.setAllCaps(false);
            b.setOnClickListener(v -> { o.status = s; saveOrders(); render(); });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(38), 1); lp.setMargins(dp(2),0,dp(2),0); row.addView(b, lp);
        }
    }

    private void showAddDialog() {
        final Dialog d = new Dialog(this);
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(20),dp(20),dp(20));
        d.setContentView(box);
        TextView h = text("Nova porudžbina", 22, Color.rgb(24,24,27), true); box.addView(h);
        EditText customer = input("Kupac"), phone = input("Telefon"), product = input("Proizvod"), amount = input("Iznos €"), deadline = input("Rok, npr. 2026-05-20"), note = input("Napomena");
        box.addView(customer); box.addView(phone); box.addView(product); box.addView(amount); box.addView(deadline); box.addView(note);
        Button save = new Button(this); save.setText("Sačuvaj"); save.setTextColor(Color.WHITE); save.setBackgroundColor(Color.rgb(24,24,27));
        box.addView(save, new LinearLayout.LayoutParams(-1, dp(48)));
        save.setOnClickListener(v -> {
            if (customer.getText().toString().trim().isEmpty() || product.getText().toString().trim().isEmpty()) return;
            int next = 1024 + orders.size() + 1;
            int amt = 0; try { amt = Integer.parseInt(amount.getText().toString().trim()); } catch(Exception ignored) {}
            orders.add(new Order("A-"+next, customer.getText().toString(), phone.getText().toString(), product.getText().toString(), amt, deadline.getText().toString(), "Nova", note.getText().toString()));
            saveOrders(); d.dismiss(); hideKeyboard(product); render();
        });
        d.show();
        Window w = d.getWindow(); if (w != null) w.setLayout(-1, -2);
    }

    private EditText input(String hint) { EditText e = new EditText(this); e.setHint(hint); e.setSingleLine(false); e.setPadding(dp(10),0,dp(10),0); return e; }
    private TextView text(String s, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setPadding(0,dp(3),0,dp(3)); if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); return t; }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private String niceDate(String d) { try { Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(d); return new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).format(date); } catch(Exception e) { return d; } }
    private boolean isUrgent(Order o) { try { Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(o.deadline); return date.getTime() - System.currentTimeMillis() <= 5L*24*60*60*1000 && !o.status.equals("Isporučeno"); } catch(Exception e) { return false; } }
    private void hideKeyboard(View v) { ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(),0); }

    private void loadOrders() {
        String json = getPreferences(0).getString("orders", null);
        if (json == null) {
            orders.add(new Order("A-1024", "Marko Petrović", "+381 64 123 4567", "Okrugli sto hrast 110cm", 640, "2026-05-18", "U izradi", "Metalne noge crne, mat finiš"));
            orders.add(new Order("A-1025", "Jelena Simić", "+381 63 777 8899", "TV komoda industrijski stil", 420, "2026-05-14", "Nova", "Potvrditi dimenzije pre sečenja"));
            orders.add(new Order("A-1026", "Milan Jovanović", "+381 65 333 2211", "Trpezarijski sto 180x90", 780, "2026-05-22", "Spremno", "Kupac preuzima u radionici"));
            return;
        }
        try { JSONArray a = new JSONArray(json); for (int i=0;i<a.length();i++){ JSONObject x=a.getJSONObject(i); orders.add(new Order(x.getString("id"),x.getString("customer"),x.getString("phone"),x.getString("product"),x.getInt("amount"),x.getString("deadline"),x.getString("status"),x.optString("note",""))); } } catch(Exception ignored) {}
    }
    private void saveOrders() {
        JSONArray a = new JSONArray();
        try { for (Order o: orders) { JSONObject x = new JSONObject(); x.put("id",o.id); x.put("customer",o.customer); x.put("phone",o.phone); x.put("product",o.product); x.put("amount",o.amount); x.put("deadline",o.deadline); x.put("status",o.status); x.put("note",o.note); a.put(x); } } catch(Exception ignored) {}
        getPreferences(0).edit().putString("orders", a.toString()).apply();
    }
}
