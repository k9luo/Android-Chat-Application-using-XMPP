package com.mycompany.chat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mycompany.chat.R;
import com.mycompany.chat.xmpp.MyXMPP;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MyXMPP xmpp;
    private ListView listView;
    ArrayList<String> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        listView = (ListView) findViewById(R.id.list);
        xmpp=new MyXMPP();
        new GetUser().execute();
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(FriendsActivity.this, ChatActivity.class);
        String s = arrayList.get(i);
        intent.putExtra("USER", s);
        Log.i("user", s);
        startActivity(intent);
    }


    class GetUser extends AsyncTask<Void, Void, String> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(FriendsActivity.this);
            pd.setMessage("Loading contacts");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            // use the cursor to access the contacts
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                getRegisteredUser(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }


            return null;
        }

        @Override
        public void onPostExecute(String re) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(FriendsActivity.this, android.R.layout.simple_list_item_1, arrayList);
            listView.setAdapter(arrayAdapter);
        }
    }


    public void getRegisteredUser(String str) {
        //9799990168
        //+91 9799 990168
        //09799990168
        String username = str.replaceAll("\\s+", "");
        if (username.length() == 13) {
            username = username.substring(3);
        } else if (username.length() == 11) {
            username = username.substring(1);
        }
        UserSearchManager manager = new UserSearchManager(xmpp.getConnection());


        try {
            String searchFormString = "search." + xmpp.getConnection().getServiceName();
            Log.d("***", "SearchForm: " + searchFormString);
            Form searchForm = manager.getSearchForm(searchFormString);
            Form answerForm = searchForm.createAnswerForm();

            UserSearch userSearch = new UserSearch();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", username);

            ReportedData results = userSearch.sendSearchForm(xmpp.getConnection(), answerForm, searchFormString);
            if (results != null) {
                List<ReportedData.Row> rows = results.getRows();
                for (ReportedData.Row row : rows) {
                    if (username.equals(row.getValues("Username").toString()))
                        System.out.print(row.getValues("Username").toString());
                    String sbs = row.getValues("Username").toString();
                    sbs = sbs.replaceAll("\\p{P}", "");

                    arrayList.add(sbs);
                    ArrayList<String> arrayListContact = new ArrayList<>();
                    ArrayList<String> openfire = new ArrayList<>();

                    for (String str1 : arrayListContact) {
                        for (int i = 0; i < openfire.size(); i++) {
                            String opencontact = openfire.get(i);
                            if (str1 == opencontact) {

                            }
                            {

                            }
                        }

                    }
                    Log.i("MILA", sbs);
                }
            } else {
                Log.d("***", "No result found");
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }

    }

}
