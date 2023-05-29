package com.example.autosms;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SIMCard_Pickers extends Activity {
    private ArrayList<SIMCard> simCards;
    private SIMCardAdapter simCardAdapter;
    private TextView cancelButton;
    private Button button_deselect_all;
    private static final int REQUEST_CODE_READ_SIM_CARDS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simcard_pickers);

        ListView simCardsListView = findViewById(R.id.list_view_simCards);
        Button confirmButton = findViewById(R.id.button_confirm);
        Button selectAllButton = findViewById(R.id.button_select_all);
        cancelButton = findViewById(R.id.cancelButton);
        button_deselect_all = findViewById(R.id.button_deselect_all);

        simCards = new ArrayList<>();
        simCardAdapter = new SIMCardAdapter(this, simCards);
        simCardsListView.setAdapter(simCardAdapter);

        // Check if the READ_CONTACTS permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_CODE_READ_SIM_CARDS);
        } else {
            // Permission is already granted, proceed with loading SIM cards
            loadSIMCards();
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder selectedSimCards = new StringBuilder();
                for (SIMCard simCard : simCards) {
                    if (simCard.isSelected()) {
                        selectedSimCards.append(simCard.getNumber()).append(", ");
                    }
                }
                if (selectedSimCards.length() > 0) {
                    selectedSimCards.setLength(selectedSimCards.length() - 2); // Remove last comma and space
                }

                Intent intent = new Intent();
                intent.putExtra("selectedSimCards", selectedSimCards.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        button_deselect_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (SIMCard simCard : simCards) {
                    simCard.setSelected(true);
                }
                simCardAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_SIM_CARDS) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with loading Cards
                loadSIMCards();
            } else {
                // Permission denied, handle accordingly (e.g., show an error message)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSIMCards() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> subscriptionInfoList = SubscriptionManager.from(this).getActiveSubscriptionInfoList();
                if (subscriptionInfoList != null) {
                    for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                        String number = subscriptionInfo.getNumber();
                        String displayName = subscriptionInfo.getDisplayName().toString();

                        SIMCard simCard = new SIMCard(displayName, number);
                        simCards.add(simCard);
                    }
                    simCardAdapter.notifyDataSetChanged();
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        REQUEST_CODE_READ_SIM_CARDS);
            }
        }
    }
}
