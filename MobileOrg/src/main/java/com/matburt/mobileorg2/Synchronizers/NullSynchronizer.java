package com.matburt.mobileorg2.Synchronizers;

import android.content.Context;

import com.matburt.mobileorg2.Gui.SynchronizerNotificationCompat;

import java.io.BufferedReader;
import java.util.HashSet;

public class NullSynchronizer extends SynchronizerManager{

    public NullSynchronizer(Context context) {
        super(context);
    }

    @Override
    public String getRelativeFilesDir() {
        return null;
    }

    public boolean isConfigured() {
        return true;
    }

    public void putRemoteFile(String filename, String contents) {
    }

    public BufferedReader getRemoteFile(String filename) {
        return null;
    }

    @Override
    public SyncResult synchronize() {

        return null;
    }


    @Override
	public void postSynchronize() {
    }

    @Override
    public void addFile(String filename) {

    }

    @Override
    public boolean isConnectable() {
		return true;
	}
}