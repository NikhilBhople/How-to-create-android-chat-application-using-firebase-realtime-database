package firechat.app.com.chatapp;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StartChatActivity extends AppCompatActivity {


    private DatabaseReference root;
    private TextView chathistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_chat);

        final String userName = getIntent().getExtras().getString(GoogleSignInActivity.USER_NAME);
        String chatRoom = getIntent().getExtras().getString(ChatActivity.ROOT_NAME);

        setTitle(chatRoom); // setting title of activity as group name

        // getting reference to the root node of firebase database from the group list
        root = FirebaseDatabase.getInstance().getReference().child(chatRoom);

        final EditText userChat = (EditText) findViewById(R.id.chatText);

        chathistory = (TextView) findViewById(R.id.chathistory);
        chathistory.setMovementMethod(new ScrollingMovementMethod()); // it is for scrollable textview


        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> map = new HashMap<String, Object>();
                String key = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference message_root = root.child(key);
                Map<String,Object> map1 = new HashMap<String, Object>();
                map1.put("name",userName);
                map1.put("msg",userChat.getText().toString());
                message_root.updateChildren(map1);  // pushing user enter text to firebase database

                userChat.setText(""); // clearing user typed test after submitting
            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                playNotificationSound(); // play sound for new child added to database
                appendChat(dataSnapshot); // displaying that clild name and message to user
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                playNotificationSound();
                appendChat(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // giveing alert to user for new chat
    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // code for displaying chat to textview in activity
    private void appendChat(DataSnapshot dataSnapshot) {
        String chatmessage,chatUserName;

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            chatmessage  = (String) ((DataSnapshot)(iterator.next())).getValue();
            chatUserName = (String) ((DataSnapshot) iterator.next()).getValue();

            chathistory.append(chatUserName+ " : "+chatmessage + "\n\n");
        }


    }
}
