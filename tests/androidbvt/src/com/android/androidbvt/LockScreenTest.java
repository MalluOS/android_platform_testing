/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.androidbvt;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.EditText;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LockScreenTest extends TestCase {
    private static final int SHORT_TIMEOUT = 200;
    private static final int LONG_TIMEOUT = 2000;
    private static final int PIN = 1234;
    private static final String PASSWORD = "aaaa";
    private UiDevice mDevice = null;
    private Context mContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.freezeRotation();
        mContext = InstrumentationRegistry.getTargetContext();
        mDevice.wakeUp();
        mDevice.pressHome();
    }

    @Override
    public void tearDown() throws Exception {
        mDevice.pressHome();
        mDevice.unfreezeRotation();
        mDevice.waitForIdle();
        super.tearDown();
    }

    @LargeTest
    public void testLockScreenPIN() throws Exception {
        setScreenLock(Integer.toString(PIN), "PIN");
        sleepAndWakeUpDevice();
        unlockScreen(Integer.toString(PIN));
        removeScreenLock(Integer.toString(PIN));
        Thread.sleep(LONG_TIMEOUT);
        Assert.assertFalse("Lock Screen is still enabled", isLockScreenEnabled());
    }

    @LargeTest
    public void testLockScreenPwd() throws Exception {
        setScreenLock(PASSWORD, "Password");
        sleepAndWakeUpDevice();
        unlockScreen(PASSWORD);
        removeScreenLock(PASSWORD);
        Thread.sleep(LONG_TIMEOUT);
        Assert.assertFalse("Lock Screen is still enabled", isLockScreenEnabled());
    }

    /** Sets the screen lock pin or password
     * @param pwd text of Password or Pin for lockscreen
     * @param mode indicate if its password or PIN
     */
    private void setScreenLock(String pwd, String mode) throws Exception {
        navigateToScreenLock();
        mDevice.wait(Until.findObject(By.text(mode)), LONG_TIMEOUT).click();
        //set up Secure start-up page
        mDevice.wait(Until.findObject(By.text("No thanks")), LONG_TIMEOUT).click();
        UiObject pinField = new UiObject(
                new UiSelector().className(EditText.class.getName()));
        pinField.setText(pwd);
        //enter and verify password
        mDevice.pressEnter();
        pinField.setText(pwd);
        mDevice.pressEnter();
        mDevice.wait(Until.findObject(By.text("DONE")), LONG_TIMEOUT).click();
    }

    private void removeScreenLock(String pwd) throws Exception {
        navigateToScreenLock();
        UiObject pinField = new UiObject(new UiSelector().className(EditText.class.getName()));
        pinField.setText(pwd);
        mDevice.pressEnter();
        mDevice.wait(Until.findObject(By.text("Swipe")), LONG_TIMEOUT).click();
        mDevice.wait(Until.findObject(By.text("YES, REMOVE")), LONG_TIMEOUT).click();
    }

    private void unlockScreen(String pwd) throws Exception {
        swipeUp();
        Thread.sleep(SHORT_TIMEOUT);
        //enter password to unlock screen
        String command = String.format(" %s %s %s", "input", "text", pwd);
        mDevice.executeShellCommand(command);
        mDevice.waitForIdle();
        Thread.sleep(SHORT_TIMEOUT);
        mDevice.pressEnter();
    }

    private void navigateToScreenLock() throws Exception {
        launchSettingsPage(mContext, Settings.ACTION_SECURITY_SETTINGS);
        new UiObject(new UiSelector().text("Screen lock")).click();
    }

    private void launchSettingsPage(Context ctx, String pageName) throws Exception {
        Intent intent = new Intent(pageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
        Thread.sleep(LONG_TIMEOUT * 2);
    }

    private void sleepAndWakeUpDevice() throws RemoteException, InterruptedException {
        mDevice.sleep();
        Thread.sleep(LONG_TIMEOUT);
        mDevice.wakeUp();
    }

    private void swipeUp() throws Exception {
        mDevice.swipe(mDevice.getDisplayWidth() / 2, mDevice.getDisplayHeight(),
                mDevice.getDisplayWidth() / 2, 0, 30);
        Thread.sleep(SHORT_TIMEOUT);
    }

    private boolean isLockScreenEnabled(){
        KeyguardManager km = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
        return km.isKeyguardSecure();
    }
}
