package com.nguyendinhdoan.driverapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.adapter.HistoryAdapter;
import com.nguyendinhdoan.driverapp.model.History;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String HISTORY_TABLE_NAME = "history_driver";
    private Toolbar myRoutesToolbar;
    private RecyclerView myRoutesRecyclerView;
    private ImageView backImageView;

    private List<History> historyList = new ArrayList<>();

    public static Intent start(Context context) {
        return new Intent(context, HistoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupData();
        addEvents();
    }

    private void addEvents() {
        backImageView.setOnClickListener(this);
    }

    private void setupData() {
        if (getIntent() != null) {
            String driverId = getIntent().getStringExtra(DriverActivity.DRIVER_ID_KEY);
            if (driverId != null) {
                DatabaseReference historyTable = FirebaseDatabase.getInstance().getReference(HISTORY_TABLE_NAME);
                historyTable.child(driverId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            History history = dataSnapshot1.getValue(History.class);
                            historyList.add(history);
                        }
                        initRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Snackbar.make(findViewById(android.R.id.content), "Error load history of driver", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }

    }

    private void initRecyclerView() {
        myRoutesRecyclerView.setHasFixedSize(true);
        myRoutesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        HistoryAdapter adapter = new HistoryAdapter(this, historyList);
        myRoutesRecyclerView.setAdapter(adapter);
    }

    private void initViews() {
        myRoutesToolbar = findViewById(R.id.my_routes_toolbar);
        setupToolbar();
        myRoutesRecyclerView = findViewById(R.id.my_routes_recycler_view);
        backImageView = findViewById(R.id.back_image_view);
    }

    private void setupToolbar() {
        setSupportActionBar(myRoutesToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_image_view) {
            Intent intent = DriverActivity.start(HistoryActivity.this);
            intent.putExtra(EndGameActivity.DRIVER_RESTART_KEY, getString(R.string.driver_restart));
            startActivity(intent);
            finish();
        }
    }
}
