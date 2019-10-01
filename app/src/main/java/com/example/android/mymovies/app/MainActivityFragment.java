package com.example.android.mymovies.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static ArrayAdapter<String> arrayAdapter;
    public MainActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_popular) {
            FetchMovieTask fmt = new FetchMovieTask();
            fmt.urlType = "https://api.themoviedb.org/3/movie/popular?api_key=4688014dcaa0bc942747d4ea6c9ec16b";
            fmt.execute();
            return true;
        }
        else if (id == R.id.action_rating) {
            FetchMovieTask fmt = new FetchMovieTask();
            fmt.urlType = "https://api.themoviedb.org/3/movie/top_rated?api_key=4688014dcaa0bc942747d4ea6c9ec16b";
            fmt.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMovieTask fmt = new FetchMovieTask();
        fmt.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //Need to get URLs of images in the ArrayList
        List<String> data = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_movie, R.id.list_item_movie_imageView, data);
        ListView listView = (ListView) rootView.findViewById(R.id.listView_Movies);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String movie = arrayAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, movie);
                startActivity(intent);
            }
        });
        return rootView;
    }

}

class FetchMovieTask extends AsyncTask<Void, Void, String []>{

    String urlType = "https://api.themoviedb.org/3/movie/popular?api_key=4688014dcaa0bc942747d4ea6c9ec16b";

    public static String[] getTitles(String moviesdb) throws JSONException{
        JSONObject movies = new JSONObject(moviesdb);
        JSONArray moviesArr = movies.getJSONArray("results");
        int length = moviesArr.length();
        String [] titles = new String [length];
        JSONObject movie = null;
        for(int i = 0; i < moviesArr.length(); i++) {
            movie = moviesArr.getJSONObject(i);
            titles[i] = movie.getString("original_title");
        }
        return titles;
    }

    protected String[] doInBackground(Void ... Params){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJsonStr = null;

        try {
            URL url = new URL(urlType);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            movieJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        try {
            return getTitles(movieJsonStr);
        }
        catch(Exception e){
            return null;
        }
    }

    protected void onPostExecute(String[]result){
        if(result != null){
            MainActivityFragment.arrayAdapter.clear();
            for(String title : result){
                MainActivityFragment.arrayAdapter.add(title);
            }
        }
    }
}
