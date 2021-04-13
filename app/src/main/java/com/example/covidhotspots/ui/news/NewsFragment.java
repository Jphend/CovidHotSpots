package com.example.covidhotspots.ui.news;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.covidhotspots.R;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news, container, false);

        String urlString = "https://news.google.com/rss/search?cf=all&hl=en-US&pz=1&q=COVID-19&gl=US&ceid=US:en";
        Parser parser = new Parser();
        parser.execute(urlString);
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                
                //what to do when the parsing is done
                //the Array List contains all article's data. For example you can use it for your adapter.
                System.out.println(list.toString());
                Toast.makeText(requireActivity(),"completed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(requireActivity(),"error", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }
}