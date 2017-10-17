package firechat.app.com.chatapp;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {

    protected static final String ROOT_NAME = "roomName"; // creating constant to accessing intent string extra

    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
    private ArrayList<String> chatRooms = new ArrayList<>();
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private Button addgroup,joingroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        addgroup = (Button) findViewById(R.id.addgroup);
        joingroup = (Button) findViewById(R.id.joingroup);

        listView = (ListView) findViewById(R.id.list);
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,chatRooms);
        listView.setAdapter(arrayAdapter); // display list of all created group in firebase database

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // sending inside selected group
                Intent intent = new Intent(ChatActivity.this,StartChatActivity.class);
                intent.putExtra(ROOT_NAME,chatRooms.get(position));
                intent.putExtra(GoogleSignInActivity.USER_NAME,getIntent().getExtras().getString(GoogleSignInActivity.USER_NAME));
                startActivity(intent);
            }
        });


        addgroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddGroup();
            }
        });

        joingroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowGroupList();
            }
        });


    }

    private void ShowGroupList() {

        joingroup.setVisibility(View.GONE);

            root.addValueEventListener(new ValueEventListener() { // method to get data form firebase
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Set<String> set = new HashSet<String>();
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()){ // traversing to the end and adding group name to the list
                        set.add(((DataSnapshot)iterator.next()).getKey());
                    }
                    chatRooms.clear();
                    chatRooms.addAll(set);
                    arrayAdapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }


    // showing Dialog to user for entering group name
    private void AddGroup() {

        addgroup.setVisibility(View.GONE);

        final Dialog dialog = new Dialog(ChatActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert);

        final EditText groupName = (EditText) dialog.findViewById(R.id.groupname);
        Button submit = (Button) dialog.findViewById(R.id.submitbtn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              String name = groupName.getText().toString();

                if(name.equals(""))
                { // if user have not enter any group name
                    groupName.setError("Required");
                }
                else
                {
                    Map<String,Object> map = new HashMap<>();
                    map.put(name,""); // we require only key as group name
                    root.updateChildren(map); // pushing group name to firebase database

                    dialog.dismiss();
                    ShowGroupList(); // showing group list to user
                }
            }
        });

        dialog.show();


    }
}
