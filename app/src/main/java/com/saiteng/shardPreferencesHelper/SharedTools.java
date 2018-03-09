package com.saiteng.shardPreferencesHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

import java.util.List;
import java.util.Set;

public class SharedTools {

	private SharedPreferences shared;

	private final String SHARE_KEY = "stpttshared";

	public SharedTools(Context context) {
		super();
		this.shared = context.getSharedPreferences(SHARE_KEY,
				Context.MODE_PRIVATE);
	}

	public boolean getShareBoolean(String key, boolean b) {
		return shared.getBoolean(key, b);
	}

	public int getShareInt(String key, int i) {
		return shared.getInt(key, i);
	}

	public String getShareString(String key, String s) {
		return shared.getString(key, s);
	}


	public String getShareObject(String key, String s) {
		return shared.getString(key, s);
	}

	public void setShareBoolean(String key, boolean value) {
		Editor editor = shared.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void setShareInt(String key, int value) {
		Editor editor = shared.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void setShareString(String key, String value) {
		Editor editor = shared.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void setShareObject(String key, List value) {
		Gson gson = new Gson();
		String json = gson.toJson(value);
		Editor editor = shared.edit();
		editor.putString(key, json);
		editor.commit();
	}

	public void cleanSharedTools(){
		Editor editor = shared.edit();
		editor.clear();
		editor.commit();
	}
}
