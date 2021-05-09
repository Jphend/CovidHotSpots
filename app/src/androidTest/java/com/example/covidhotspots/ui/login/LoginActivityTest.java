package com.example.covidhotspots.ui.login;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> loginTestRule = new ActivityScenarioRule<>(LoginActivity.class);

    private final String name1 = "Phil@gmail.com";
    private final String name2 = "jon";


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRegisterScenario() {
        //Espresso.onView(withId(R.id.))
    }

    @After
    public void tearDown() throws Exception {
    }




}