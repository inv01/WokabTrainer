package com.example.wokabstar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity  extends android.support.v7.app.ActionBarActivity {

    public final static String EXTRA_MESSAGE = "com.example.wokabstar.MESSAGE";
    private Button btnChangeOpt, btnStartTrnr, btnEditDict;
    private TextView txtHello; //CurLevel + NumWords, NumRepeat, NumDone
    private final String helloStart = "Please edit your dictionary!", 
                         helloEdit = "Bravo!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        
         // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // For the main activity, make sure the app icon in the action bar
            // does not behave as a button
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(false);
        }
        //doSmthDB();
    }
    private void init(){
        btnChangeOpt = (Button)findViewById(R.id.btnChangeOpt);
        btnStartTrnr = (Button)findViewById(R.id.btnStartTrnr);
        btnEditDict = (Button)findViewById(R.id.btnEditDict);
        txtHello = (TextView)findViewById(R.id.txtHello);
    }

    public void onClickEditDict(View v) {
        String hello_world = getResources().getString(R.string.hello_world);
        txtHello.setText(hello_world + "CurLevel + NumWords, NumRepeat, NumDone");
        Intent intent = new Intent(this, WorkOnDictActivity.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = hello_world;//editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void onClickChangeOpt(View v) {
        txtHello.setText(getResources().getString(R.string.hello_world));
    }

    public void onClickStartTrnr(View v) {
        txtHello.setText(getResources().getString(R.string.start));
    }

    public static Connection ConnecrDb(){
        try{
            //String dir = System.getProperty("user.dir");  
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:/Users/oskarpolak/sdk/SQL/trnrDB");///Users/oskarpolak/sdk/SQL/trnrDB
            return con;
        }catch(Exception e){
            //JOptionPane.showMessageDialog(null,"Problem with connection of database");
            System.out.println("Translation:0!!!");
            e.printStackTrace();
            return null;
        }
    }
    
    public void doSmthDB(){
        Connection connection = null;  
        ResultSet resultSet = null;  
        Statement statement = null;

        try{
            connection = ConnecrDb();
            System.out.println("Translation:1");
            if (connection != null){
                
                statement = connection.createStatement();
                resultSet = statement
                        .executeQuery("SELECT out_word FROM TDict");
                while (resultSet.next()){
                    System.out.println("Translation:"
                            + resultSet.getString("out_word"));
                }
            }
            System.out.println("Translation:2");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                resultSet.close();
                statement.close();
                connection.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
