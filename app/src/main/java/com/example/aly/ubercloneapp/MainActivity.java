package com.example.aly.ubercloneapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
     enum State
    {
        SIGNUP, LOGIN
    }

    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private RadioButton driverRadioButton, passengerRadioButton;
    private EditText editUserName, editPassword, editDriverOrPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Uber Clone");

        if (ParseUser.getCurrentUser() != null)
        {
            //ParseUser.logOut();
            transitionToPassengerActivity();
        }

        editUserName = findViewById(R.id.editUserName);
        editPassword = findViewById(R.id.editPassword);
        editDriverOrPassenger = findViewById(R.id.editDorP);
        driverRadioButton = findViewById(R.id.rdbDriver);
        passengerRadioButton = findViewById(R.id.rdbPassenger);
        btnSignUpLogin = findViewById(R.id.btnSignUpLogin);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);

        btnOneTimeLogin.setOnClickListener(this);

        state = State.SIGNUP;

        btnSignUpLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (state == State.SIGNUP)
                {
                    if (driverRadioButton.isChecked() == false && passengerRadioButton.isChecked() == false)
                    {
                        Toast.makeText(MainActivity.this,
                                "Are you a driver or passenger", Toast.LENGTH_LONG).show();

                        return;
                    }

                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(editUserName.getText().toString());
                    appUser.setPassword(editPassword.getText().toString());
                    if (driverRadioButton.isChecked())
                    {
                        appUser.put("as", "Driver");
                    }

                    else if (passengerRadioButton.isChecked())
                    {
                        appUser.put("as", "Passenger");
                    }

                    appUser.signUpInBackground(new SignUpCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e == null)
                            {
                                Toast.makeText(MainActivity.this,
                                        ParseUser.getCurrentUser().getUsername()
                                                + " signed up successfully", Toast.LENGTH_SHORT).show();

                                transitionToPassengerActivity();
                            }
                        }
                    });
                }

                else if (state == State.LOGIN)
                {
                    ParseUser.logInInBackground(editUserName.getText().toString(),
                            editPassword.getText().toString(), new LogInCallback()
                            {
                                @Override
                                public void done(ParseUser user, ParseException e)
                                {
                                    if (user != null && e == null)
                                    {
                                        Toast.makeText(MainActivity.this,
                                                ParseUser.getCurrentUser().getUsername()
                                                        + " logged in successfully", Toast.LENGTH_SHORT).show();

                                        transitionToPassengerActivity();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.login_item:

                if (state == State.SIGNUP)
                {
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Login");
                }

                else if (state == State.LOGIN)
                {
                    state = State.LOGIN;
                    item.setTitle("Login");
                    btnSignUpLogin.setText("Sign Up");
                }

            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view)
    {
        if (editDriverOrPassenger.getText().toString().equals("Driver")
                || editDriverOrPassenger.getText().toString().equals("Passenger"))
        {
            if (ParseUser.getCurrentUser() == null)
            {
                ParseAnonymousUtils.logIn(new LogInCallback()
                {
                    @Override
                    public void done(ParseUser user, ParseException e)
                    {
                        if (user != null && e == null)
                        {
                            Toast.makeText(MainActivity.this,
                                    "Anonymous " + editDriverOrPassenger.getText().toString()
                                            + " logged in successfully", Toast.LENGTH_SHORT).show();

                            user.put("as", editDriverOrPassenger.getText().toString());

                            user.saveInBackground(new SaveCallback()
                            {
                                @Override
                                public void done(ParseException e)
                                {
                                    if (e == null)
                                    {
                                        transitionToPassengerActivity();
                                    }
                                }
                            });


                        }
                    }
                });
            }
        }

        else
        {
            Toast.makeText(MainActivity.this,
                    "Are you a driver or passenger", Toast.LENGTH_LONG).show();

            return;
        }
    }

    private void transitionToPassengerActivity()
    {
        if (ParseUser.getCurrentUser() != null)
        {
            if (ParseUser.getCurrentUser().get("as").equals("Passenger"))
            {
                Intent intent = new Intent (MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }
}
