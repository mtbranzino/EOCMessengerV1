package com.huntloc.eocmessenger;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.icu.text.LocaleDisplayNames;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static final String PREFS_NAME = "EOMessengerPrefsFile";
    private SharedPreferences settings;
    private FirebaseAuth mAuth;
    private BroadcastReceiver receiver;
    private ExpandableListView groupsListView;
    private  ExpandableListAdapter expandableListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        settings = getSharedPreferences(PREFS_NAME, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupsListView = (ExpandableListView) findViewById(R.id.groups_list);
        List<String> listDataHeader = new ArrayList<String>();
        HashMap<String, List<Client>> listDataChild = new HashMap<String, List<Client>>();
        expandableListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        groupsListView.setAdapter(expandableListAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client client = getClient(settings.getString("id", ""));
                if (client != null && client.isAdmin()) {
                    showMessageDialog();
                } else {
                    Snackbar.make(view, "Only administrators can send messages.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        FirebaseDatabase.getInstance().getReference().child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                saveGroups(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        FirebaseDatabase.getInstance().getReference().child("clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                saveClients(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    displayMessageReceived(intent.getStringExtra("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
    public void displayMessageReceived(String message) {
        if (mAuth.getCurrentUser() != null) {
            FragmentManager manager = getFragmentManager();
            DisplayMessageDialogFragment dialog = new DisplayMessageDialogFragment();
            dialog.setParams(this, message);
            dialog.show(manager, "newMessageDialog");
        }
    }
    private void showMessageDialog() {
        if (mAuth.getCurrentUser() != null) {
            //String username = SignInActivity.usernameFromEmail(mAuth.getCurrentUser().getEmail());
            FragmentManager manager = getFragmentManager();
            MessageDialogFragment dialog = new MessageDialogFragment();
            dialog.setParams(this, getClient(settings.getString("id", "")).getName());
            dialog.show(manager, "messageDialog");
        }
    }
    private void showSettingsDialog(boolean firstTime) {
        if (mAuth.getCurrentUser() != null) {
            FragmentManager manager = getFragmentManager();
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setParams(this, firstTime);
            dialog.show(manager, "settingsDialog");
        }
    }
    private Client getClient(String id) {
        SQLiteHelper db = new SQLiteHelper(
                this);
        return db.getClient(id);
    }
    private void saveGroups(DataSnapshot dataSnapshot) {
        SQLiteHelper db = new SQLiteHelper(this);
        db.deleteGroups();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            Group group = new Group(childDataSnapshot.getKey(), childDataSnapshot.getValue().toString());
            db.addGroup(group);
        }
        Client client = getClient(settings.getString("id", ""));
        if (client == null) {
            showSettingsDialog(true);
        }
    }
    private void saveClients(DataSnapshot dataSnapshot) {
        SQLiteHelper db = new SQLiteHelper(this);
        db.deleteClients();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
            HashMap<String, Boolean> groups = new HashMap<String, Boolean>();
            for (DataSnapshot childDataSnapshot1 : childDataSnapshot.child("groups").getChildren()) {
                groups.put(childDataSnapshot1.getKey(), (Boolean) childDataSnapshot1.getValue());
            }
            Client client = new Client(childDataSnapshot.child("id").getValue().toString(),
                    childDataSnapshot.child("name").getValue().toString(),
                    childDataSnapshot.child("token").getValue().toString(),
                    (boolean) childDataSnapshot.child("admin").getValue(),
                    groups
            );
            Log.d("saveClientsFromCloud", groups.toString());
            db.addClient(client);
        }
        listGroups();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        listGroups();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MyFirebaseMessagingService.REQUEST_ACCEPT)
        );
        if (getIntent().getExtras() != null && getIntent().getExtras().getString("data") != null) {
            Log.d("MainActivity Intent", getIntent().getExtras().getString("data").toString());
            displayMessageReceived(getIntent().getExtras().getString("data"));
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().getString("data") != null) {
            displayMessageReceived(intent.getExtras().getString("data"));
        }
    }
    private void listGroups() {
        SQLiteHelper db = new SQLiteHelper(this);
        List<Group> groups = db.getGroups();
        List<String> listDataHeader = new ArrayList<String>();
        HashMap<String, List<Client>> listDataChild = new HashMap<String, List<Client>>();
        for (Group group : groups) {
            listDataHeader.add(group.getName());
            listDataChild.put(group.getName(), db.getClientsByGroup(group.getName()));
        }
        expandableListAdapter.setListDataHeader(listDataHeader);
        expandableListAdapter.setListDataChild(listDataChild);
        expandableListAdapter.notifyDataSetChanged();
        //groupsListView.setAdapter(new ExpandableListAdapter(this, listDataHeader, listDataChild));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showSettingsDialog(false);
            return true;
        }
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static class SettingsDialogFragment extends DialogFragment implements View.OnClickListener {
        Button saveButton, cancelButton;
        EditText nameEditText;
        CheckBox isAdminCheck;
        ListView groupsListView;
        SharedPreferences settings;
        MainActivity activity;
        boolean isFirstTime;

        public SettingsDialogFragment() {
        }

        public void setParams(MainActivity activity, boolean isFirstTime) {
            this.activity = activity;
            this.isFirstTime = isFirstTime;
            this.settings = this.activity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setCancelable(false);
            getDialog().setTitle("Settings");
            View view = inflater.inflate(R.layout.settings_popup_window, null, false);
            nameEditText = (EditText) view.findViewById(R.id.editText_Name);
            saveButton = (Button) view.findViewById(R.id.ib_save);
            cancelButton = (Button) view.findViewById(R.id.ib_cancel);
            saveButton.setOnClickListener(this);
            cancelButton.setOnClickListener(this);
            isAdminCheck = (CheckBox) view.findViewById(R.id.admin_check);
            groupsListView = (ListView) view.findViewById(R.id.settings_groups_list);

            cancelButton.setEnabled(!this.isFirstTime);
            SQLiteHelper db = new SQLiteHelper(getActivity());
            Client client = db.getClient(settings.getString("id", ""));
            HashMap<String, Boolean> selectedGroups = new HashMap<String, Boolean>();
            if (client != null) {
                Log.d("Settings", client.toString());
                nameEditText.setText(client.getName());
                isAdminCheck.setChecked(client.isAdmin());
                selectedGroups = client.getGroups();
            }

            listGroups(selectedGroups);
            return view;
        }

        private void listGroups(HashMap<String, Boolean> selectedGroups) {
            SQLiteHelper db = new SQLiteHelper(getActivity());
            List<Group> groups = db.getGroups();
            ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
            for (Group group : groups) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("name", group.getName());
                list.add(item);
            }
            String[] columns = new String[]{"name"};
            int[] renderTo = new int[]{R.id.group_name};
            //ListAdapter listAdapter = new SimpleAdapter(getActivity(), list, R.layout.settings_group_list_row, columns, renderTo);
            groupsListView.setAdapter(new CustomAdapter(activity, list, selectedGroups));
        }

        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ib_save:
                    save();
                    break;
                case R.id.ib_cancel:
                    dismiss();
                    break;
            }
        }

        private void save() {
            if (TextUtils.isEmpty(nameEditText.getText().toString())) {
                nameEditText.setError("Required");
                return;
            }
            Client client = new Client(settings.getString("id", ""),
                    nameEditText.getText().toString(),
                    settings.getString("token", ""),
                    isAdminCheck.isChecked(), getSelectedGroups()
            );
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child("clients").child(client.getId()).setValue(client);
            dismiss();
            Snackbar.make(this.activity.findViewById(android.R.id.content), "Settings saved!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

        private HashMap<String, Boolean> getSelectedGroups() {
            HashMap<String, Boolean> selected = new HashMap<String, Boolean>();
            CheckBox checkBox;
            TextView textView;
            for (int i = 0; i < groupsListView.getChildCount(); i++) {
                checkBox = (CheckBox) groupsListView.getChildAt(i).findViewById(R.id.group_checkBox);
                if (checkBox.isChecked()) {
                    textView = (TextView) groupsListView.getChildAt(i).findViewById(R.id.group_name);
                    selected.put(textView.getText().toString(), false);
                }
            }
            return selected;
        }
    }
    public static class MessageDialogFragment extends DialogFragment implements View.OnClickListener {
        Button sendButton, cancelButton;
        Spinner groupSpinner;
        EditText messageEditText;
        CheckBox isDrillCheck;
        SharedPreferences settings;
        MainActivity activity;
        String name;

        Group[] groups;
        HashMap<String, String> messages;

        public void setParams(MainActivity activity, String name) {
            this.activity = activity;
            this.name = name;

        }

        public MessageDialogFragment() {

        }

        private void setSpinnerError(Spinner spinner, String error) {
            View selectedView = spinner.getSelectedView();
            if (selectedView != null && selectedView instanceof TextView) {
                TextView selectedTextView = (TextView) selectedView;
                selectedTextView.setError(error);
            }
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            setCancelable(false);
            getDialog().setTitle("New Message");
            View view = inflater.inflate(R.layout.message_popup_window, null, false);

            SQLiteHelper db = new SQLiteHelper(getActivity());
            List<Group> _groups = db.getGroups();
            Group[] defaultOption = new Group[]{new Group("Select group","")};
            groups =  _groups.toArray(new Group[_groups.size()]);

            Group[] options = new Group[defaultOption.length+groups.length];
            System.arraycopy(defaultOption, 0, options,0,defaultOption.length);
            System.arraycopy(groups, 0, options, defaultOption.length, groups.length);
            groups = options;

            messages = new HashMap<String, String>();
            for (Group group : _groups) {
                messages.put(group.getName(), group.getMessage());
            }

            messageEditText = (EditText) view.findViewById(R.id.editText_Message);
            sendButton = (Button) view.findViewById(R.id.ib_send_message);
            cancelButton = (Button) view.findViewById(R.id.ib_cancel_message);
            sendButton.setOnClickListener(this);
            cancelButton.setOnClickListener(this);
            groupSpinner = (Spinner) view.findViewById(R.id.group_message_spinner);
            groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    showMessage();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });
            ArrayAdapter<Group> spinnerArrayAdapter = new ArrayAdapter<Group>(
                    getActivity(), android.R.layout.simple_spinner_item, groups);
            groupSpinner.setAdapter(spinnerArrayAdapter);
            isDrillCheck = (CheckBox) view.findViewById(R.id.drill_check);
            isDrillCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    showMessage();
                }
            });

            return view;
        }

        private void showMessage() {
            setSpinnerError(groupSpinner,null);
            int selectedGroup = groupSpinner.getSelectedItemPosition();
            if(selectedGroup==0){
                setSpinnerError(groupSpinner,"Required");
                messageEditText.setText("");
                return;
            }
            String group = groups[selectedGroup].getName();
            String messageText = "";
            if (isDrillCheck.isChecked()) {
                messageText = "DRILL - ";
            }
            messageText = messageText + messages.get(group) + " - Message sent by " + name;
            messageEditText.setText(messageText);
        }

        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ib_send_message:
                    send();
                    break;
                case R.id.ib_cancel_message:
                    dismiss();
                    break;
            }
        }

        private void send() {
            setSpinnerError(groupSpinner,null);
            int selectedGroup = groupSpinner.getSelectedItemPosition();
            if(selectedGroup==0){
                setSpinnerError(groupSpinner,"Required");
                messageEditText.setText("");
                return;
            }
            String group = groups[selectedGroup].getName();
            SQLiteHelper db = new SQLiteHelper(
                    activity.getApplicationContext());
            List<Client> clients = db.getClientsByGroup(group);
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            for (Client client : clients) {
                sendMessage(client.getToken(), messageEditText.getText().toString(),group);
                database.child("clients").child(client.getId()).child("groups").child(group).setValue(false);
            }
            Snackbar.make(this.activity.findViewById(android.R.id.content), "Message sent.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            dismiss();
        }

        private void sendMessage(String token, String message, String group) {
            JSONObject maindata = new JSONObject();
            try {
                JSONObject notification = new JSONObject();
                notification.put("title", "EOC Messenger");
                notification.put("text", "New message to "+group);
                notification.put("sound", "default");
                JSONObject data = new JSONObject();
                data.put("data", message+"#"+group);
                maindata.put("notification", notification);
                maindata.put("data", data);
                maindata.put("to", token);

            } catch (JSONException je) {
            }
            String serverURL = "https://fcm.googleapis.com/fcm/send";
            new SendMessageTask().execute(serverURL, maindata.toString());
        }

        private class SendMessageTask extends AsyncTask<String, String, String> {
            HttpURLConnection urlConnection;

            @SuppressWarnings("unchecked")
            protected String doInBackground(String... args) {
                StringBuilder result = new StringBuilder();
                try {
                    URL url = new URL(args[0]);
                    String data = args[1];
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Authorization", "key=AAAA7ITj9T8:APA91bF8w1_M5t0b91sqXVcOQ7drXZDaYDrHm-_nKv1ulBr2ftahH5grpqjxZ5aqSvrDixaxuT27p-JhjjJ7FCwdM13GeZTe-d644nN1TLXaYNsuC4pgeUpPczX47ZM6onlsSSL0aFq2");
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                } finally {
                    urlConnection.disconnect();
                }
                return result.toString();
            }

            protected void onPostExecute(String result) {
                try {
                    if (result != null && !result.equals("")) {
                        Log.d("Result", result);
                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.toString());
                }
            }
        }
    }
    public static class DisplayMessageDialogFragment extends DialogFragment implements View.OnClickListener {
        Button receivedButton;
        EditText messageEditText;
        SharedPreferences settings;
        MainActivity activity;
        String message;
        String group;
        public void setParams(MainActivity activity, String message) {
            this.activity = activity;
            this.group = message.substring(message.lastIndexOf("#")+1);
            this.message = message.substring(0, message.lastIndexOf("#")-1);
            this.settings = this.activity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
            Log.d("group",group);
            Log.d("message",message);
        }

        public DisplayMessageDialogFragment() {

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            setCancelable(false);
            getDialog().setTitle("New Message");
            View view = inflater.inflate(R.layout.new_message_popup_window, null, false);
            messageEditText = (EditText) view.findViewById(R.id.editText_New_Message);
            messageEditText.setText(message);
            receivedButton = (Button) view.findViewById(R.id.ib_received_message);
            receivedButton.setOnClickListener(this);
            return view;
        }

        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ib_received_message:
                    received();
                    break;
            }
        }

        private void received() {
            SQLiteHelper db = new SQLiteHelper(getActivity());
            Client client = db.getClient(settings.getString("id", ""));
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child("clients").child(client.getId()).child("groups").child(group).setValue(true);
            dismiss();
        }

    }
}
