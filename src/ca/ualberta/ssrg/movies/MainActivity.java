package ca.ualberta.ssrg.movies;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.apache.http.client.methods.HttpPost;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import ca.ualberta.ssrg.androidelasticsearch.R;
import ca.ualberta.ssrg.movies.es.ESMovieManager;
import ca.ualberta.ssrg.movies.es.Movie;
import ca.ualberta.ssrg.movies.es.Movies;
import ca.ualberta.ssrg.movies.es.MoviesController;
import ca.ualberta.ssrg.movies.es.data.SearchResponse;

public class MainActivity extends Activity {

	private ListView movieList;
	private Movies movies;
	private ArrayAdapter<Movie> moviesViewAdapter;
	private ESMovieManager movieManager;
	private MoviesController moviesController;

	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		movieList = (ListView) findViewById(R.id.movieList);
	}

	@Override
	protected void onStart() {
		super.onStart();

		movies = new Movies();
		moviesViewAdapter = new ArrayAdapter<Movie>(this, R.layout.list_item,movies);
		movieList.setAdapter(moviesViewAdapter);
		movieManager = new ESMovieManager("");

		// Show details when click on a movie
		movieList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,	long id) {
				int movieId = movies.get(pos).getId();
				startDetailsActivity(movieId);
			}

		});

		// Delete movie on long click
		movieList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Movie movie = movies.get(position);
				Toast.makeText(mContext, "Deleting " + movie.getTitle(), Toast.LENGTH_LONG).show();

				Thread thread = new DeleteThread(movie.getId());
				thread.start();

				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Refresh the list when visible
		// TODO: Search all
		setContentView(R.layout.activity_main);
		//movies = new Movies();
		//HttpPost searchRequest = new HttpPost(movies.getSearchUrl());
		//movies.getSearchUrl();
		
		SearchThread search = null; 
		search.search();
		
		moviesViewAdapter = new ArrayAdapter<Movie>(this, R.layout.list_item,movies);
		movieList.setAdapter(moviesViewAdapter);
	
		
	}
	
	/** 
	 * Called when the model changes
	 */
	public void notifyUpdated() {
		// Thread to update adapter after an operation
		Runnable doUpdateGUIList = new Runnable() {
			public void run() {
				moviesViewAdapter.notifyDataSetChanged();
			}
		};
		
		runOnUiThread(doUpdateGUIList);
	}

	/** 
	 * Search for movies with a given word(s) in the text view
	 * @param view
	 */
	public void search(View view) {
		movies.clear();

		// TODO: Extract search query from text view
		
		// TODO: Run the search thread
		
	}
	
	/**
	 * Starts activity with details for a movie
	 * @param movieId Movie id
	 */
	public void startDetailsActivity(int movieId) {
		Intent intent = new Intent(mContext, DetailsActivity.class);
		intent.putExtra(DetailsActivity.MOVIE_ID, movieId);

		startActivity(intent);
	}
	
	/**
	 * Starts activity to add a new movie
	 * @param view
	 */
	public void add(View view) {
		Intent intent = new Intent(mContext, AddActivity.class);
		startActivity(intent);
	}


	class SearchThread extends Thread {
		// TODO: Implement search thread
		private ESMovieManager movieManager;
		private Movies movies;
		
		public Movies search() {
			movieManager.searchMovies("", null);
			this.movies = movieManager.getMovies();
			return this.movies;
		}
		
	}

	
	class DeleteThread extends Thread {
		private int movieId;

		public DeleteThread(int movieId) {
			this.movieId = movieId;
		}

		@Override
		public void run() {
			moviesController.deleteMovie(movieId);

			// Remove movie from local list
			for (int i = 0; i < movies.size(); i++) {
				Movie m = movies.get(i);

				if (m.getId() == movieId) {
					movies.remove(m);
					break;
				}
			}
		}
	}
}