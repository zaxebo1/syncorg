/*
 * Copyright 2012 two forty four a.m. LLC <http://www.twofortyfouram.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.coste.syncorg.plugin;

import android.os.Bundle;
import android.text.TextUtils;

/**
 * Class for managing the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} for this plug-in.
 */
public final class PluginBundleManager {
    /**
     * Type: {@code String}
     * <p>
     * String message to display in a Toast message.
     */
    public static final String BUNDLE_EXTRA_STRING_MESSAGE = "com.yourcompany.yourapp.extra.STRING_MESSAGE"; //$NON-NLS-1$
    /**
     * Type: {@code int}
     * <p>
     * versionCode of the plug-in that saved the Bundle.
     */
    /*
     * This extra is not strictly required, however it makes backward and forward compatibility significantly easier. For example,
     * suppose a bug is found in how some version of the plug-in stored its Bundle. By having the version, the plug-in can better
     * detect when such bugs occur.
     */
    public static final String BUNDLE_EXTRA_INT_VERSION_CODE = "com.yourcompany.yourcondition.extra.INT_VERSION_CODE"; //$NON-NLS-1$

    /**
     * Private constructor prevents instantiation
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PluginBundleManager() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }

    /**
     * Method to verify the content of the bundle are correct.
     * <p>
     * This method will not mutate {@code bundle}.
     *
     * @param bundle bundle to verify. May be null, which will always return false.
     * @return true if the Bundle is valid, false if the bundle is invalid.
     */
    public static boolean isBundleValid(final Bundle bundle) {
        if (null == bundle) {
            return false;
        }

        /*
         * Make sure the expected extras exist
         */
        if (!bundle.containsKey(BUNDLE_EXTRA_STRING_MESSAGE)) {
            return false;
        }
        if (!bundle.containsKey(BUNDLE_EXTRA_INT_VERSION_CODE)) {
            return false;
        }

        /*
         * Make sure the correct number of extras exist. Run this test after checking for specific Bundle extras above so that the
         * error message is more useful. (E.g. the caller will see what extras are missing, rather than just a message that there
         * is the wrong number).
         */
        if (2 != bundle.keySet().size()) {
            return false;
        }

        /*
         * Make sure the extra isn't null or empty
         */
        if (TextUtils.isEmpty(bundle.getString(BUNDLE_EXTRA_STRING_MESSAGE))) {
            return false;
        }
        
        /*
         * Make sure the extra is the correct type
         */
        if (bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 0) != bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 1)) {
            return false;
        }

        return true;
    }
}