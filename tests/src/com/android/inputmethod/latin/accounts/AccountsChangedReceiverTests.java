/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.inputmethod.latin.accounts;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import com.android.inputmethod.latin.settings.Settings;

/**
 * Tests for {@link AccountsChangedReceiver}.
 */
public class AccountsChangedReceiverTests extends AndroidTestCase {
    private static final String ACCOUNT_1 = "account1@example.com";
    private static final String ACCOUNT_2 = "account2@example.com";

    private SharedPreferences mPrefs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // Remove all preferences before the next test.
        mPrefs.edit().clear();
    }

    public void testUnknownIntent() {
        updateAccountName(ACCOUNT_1);
        AccountsChangedReceiver reciever = new AccountsChangedReceiver();
        reciever.onReceive(getContext(), new Intent("some-random-action"));
        // Account should *not* be removed from preferences.
        assertAccountName(ACCOUNT_1);
    }

    public void testAccountRemoved() {
        updateAccountName(ACCOUNT_1);
        AccountsChangedReceiver reciever = new AccountsChangedReceiver() {
            @Override
            protected String[] getAccountsForLogin(Context context) {
                return new String[] {ACCOUNT_2};
            }
        };
        reciever.onReceive(getContext(), new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION));
        // Account should be removed from preferences.
        assertAccountName(null);
    }

    public void testAccountRemoved_noAccounts() {
        updateAccountName(ACCOUNT_2);
        AccountsChangedReceiver reciever = new AccountsChangedReceiver() {
            @Override
            protected String[] getAccountsForLogin(Context context) {
                return new String[0];
            }
        };
        reciever.onReceive(getContext(), new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION));
        // Account should be removed from preferences.
        assertAccountName(null);
    }

    public void testAccountNotRemoved() {
        updateAccountName(ACCOUNT_2);
        AccountsChangedReceiver reciever = new AccountsChangedReceiver() {
            @Override
            protected String[] getAccountsForLogin(Context context) {
                return new String[] {ACCOUNT_1, ACCOUNT_2};
            }
        };
        reciever.onReceive(getContext(), new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION));
        // Account should *not* be removed from preferences.
        assertAccountName(ACCOUNT_2);
    }

    private void updateAccountName(String accountName) {
        mPrefs.edit()
                .putString(Settings.PREF_ACCOUNT_NAME, accountName)
                .commit();
    }

    private void assertAccountName(String expectedAccountName) {
        assertEquals(expectedAccountName, mPrefs.getString(Settings.PREF_ACCOUNT_NAME, null));
    }
}