/**
 * Copyright 2013 Tom Renn
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
 * 
 */
package edu.rowan.app.fragments;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import rowan.application.quickaccess.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

import edu.rowan.app.util.JsonQueryManager;
import edu.rowan.app.util.SimpleUpFragment;

public class FoodCommentFragment extends SimpleUpFragment implements JsonQueryManager.Callback{
	private String foodEntryId;
	private String userId;
	private String userComment;
	public static final String FOOD_COMMENT_ADDR = "http://therowanuniversity.appspot.com/food/comment";
	private static final String COMMENT_PARAM = "comment";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		foodEntryId = args.getString(FoodRatingFragment.FOOD_ENTRY_ID);
		userId = args.getString(FoodRatingFragment.USER_ID);
	}
	
	public void onStart(){
		super.onStart();
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
	}
	
	/**
	 * Setup the view and set the button action when clicked
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.food_comment_layout, container, false);
		
		Button submit = (Button)view.findViewById(R.id.commentButton);
		final EditText commentField = (EditText)view.findViewById(R.id.commentField);

		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard(commentField);
				
				JsonQueryManager jsonManager = JsonQueryManager.getInstance(getActivity());
				Map<String, String> params = new HashMap<String, String>();
				String comment = commentField.getText().toString();
				comment = comment.replaceAll("<.*>", "");
				userComment = comment;
				try {
					comment = java.net.URLEncoder.encode(comment, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				params.put(FoodRatingFragment.FOOD_ENTRY_ID, foodEntryId);
				params.put(FoodRatingFragment.USER_ID, userId);
				params.put(COMMENT_PARAM, comment);
				jsonManager.requestJson(FOOD_COMMENT_ADDR, params, FoodCommentFragment.this);
			}
		});
		
		return view;
	}
	
	
	public void updateLocalComments(String userComment) {
		SharedPreferences prefs = getActivity().getSharedPreferences(FoodRatingFragment.PREFS, 0);
		String jsonEntry = prefs.getString(foodEntryId, null);
		if (jsonEntry != null) {
			try {
				JSONObject json = new JSONObject(jsonEntry);
				JSONObject commentDict = json.getJSONObject("comments");
				long timestamp = Calendar.getInstance().getTimeInMillis() / 1000;
				int numComments = json.getInt("numComments");
				JSONObject newComment = new JSONObject();
				newComment.put("date", timestamp);
				newComment.put("comment", userComment);
				commentDict.put(String.valueOf(numComments), newComment);
				numComments = numComments++;
//				json.put("numComments", numComments);
				Editor edit = prefs.edit();
				edit.putString(foodEntryId, json.toString());
				edit.commit();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	// When we receving the message back from the web service
	@Override
	public void receiveJson(JSONObject json, String origin) {
		Toast.makeText(getActivity(), "Comment successfully posted", Toast.LENGTH_SHORT).show();
		updateLocalComments(userComment);
		getActivity().onBackPressed();
	}
	
	public void hideKeyboard(EditText commentField) {
		// hide keyboard
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(commentField.getWindowToken(), 0);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			hideKeyboard( (EditText)getView().findViewById(R.id.commentField));
            getSherlockActivity().getSupportFragmentManager().popBackStackImmediate();
            return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
