package com.example.covidhotspots.ui.news;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.covidhotspots.R;
import com.example.covidhotspots.SharedViewModel;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

public class NewsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news, container, false);

        //rss url with covid 19 query, searching all news sources in the UK
        String urlString = "https://news.google.com/rss/search?cf=all&hl=en-GB&pz=1&q=COVID-19&gl=UK&ceid=GB:en";
        // Using Rss-parser to grab the three most popular articles
        Parser parser = new Parser();
        parser.execute(urlString);
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                //Article type has getters and setters for data such as the title, publication date etc...
                //Here i grab the article info and assign it to xml placeholders to present the top 3 newest news articles concerning covid
                Article article1 = list.get(0);
                String title1 = article1.getTitle();
                String date1 = article1.getPubDate().toString();
                String url1 = article1.getLink();
                Uri uri1 = Uri.parse(url1);
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri1);
                TextView newsTitle1 = root.findViewById(R.id.newsTitle1);
                newsTitle1.setText(title1);
                TextView newsDate1 = root.findViewById(R.id.date1);
                newsDate1.setText(date1);
                Button button1 = root.findViewById(R.id.details1);
                button1.setOnClickListener(v -> startActivity(intent1) );

                Article article2 = list.get(1);
                String title2 = article2.getTitle();
                String date2 = article2.getPubDate().toString();
                String url2 = article2.getLink();
                Uri uri2 = Uri.parse(url2);
                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                TextView newsTitle2 = root.findViewById(R.id.newsTitle2);
                newsTitle2.setText(title2);
                TextView newsDate2 = root.findViewById(R.id.date2);
                newsDate2.setText(date2);
                Button button2 = root.findViewById(R.id.details2);
                button2.setOnClickListener(v -> startActivity(intent2) );

                Article article3 = list.get(2);
                String title3 = article3.getTitle();
                String date3 = article3.getPubDate().toString();
                String url3 = article3.getLink();
                Uri uri3 = Uri.parse(url3);
                Intent intent3 = new Intent(Intent.ACTION_VIEW, uri3);
                TextView newsTitle3 = root.findViewById(R.id.newsTitle3);
                newsTitle3.setText(title3);
                TextView newsDate3 = root.findViewById(R.id.date3);
                newsDate3.setText(date3);
                Button button3 = root.findViewById(R.id.details3);
                button3.setOnClickListener(v -> startActivity(intent3) );
            }

            @Override
            public void onError() {
                Toast.makeText(requireActivity(),"error", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedViewModel sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
    }
}