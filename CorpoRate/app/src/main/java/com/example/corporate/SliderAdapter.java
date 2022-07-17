package com.example.corporate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    // Arrays
    public int[] slide_images = {
        R.drawable.developers_icon,
        R.drawable.mission_icon,
        R.drawable.goals_icon
    };

    public String[] slide_headings = {
            "DEVELOPERS",
            "MISSION STATEMENT",
            "Sustainable Development Goals"
    };

    public String[] slide_descriptions = {
            "Sean Cesario:\nhttps://github.com/SeanAres\n\nPrince Paul:\nhttps://github.com/ppaul895\n\n" +
                    "Geetanjali Kanojia:\nhttps://github.com/gkanojia\n\nCameron Sweeney:\nhttps://github.com/cs05178n",
            "Our app aims to hold companies responsible and ensure that the public " +
                    "has a transparent view of how they operate. Users can anonymously rate companies " +
                    "based on various metrics (like environmental sustainability and working conditions) " +
                    "and give a brief synopsis of their experience with them. Users can then look at " +
                    "average ratings and reviews to gain insight into the culture of a company and decide " +
                    "if it's one they wish to work with or support.",
            "At the heart of the 2030 Agenda for Sustainable Development, adopted by all U.N. Member States " +
                    "in 2015, are the 17 Sustainable Development Goals (SDGs) which are an urgent call for " +
                    "action by all countries - developed and developing - in a global partnership. Our app " +
                    "focuses on the following:\n\nSDG 5: Gender Equality\nSDG 8: Decent Work and Economic " +
                    "Growth\nSDG 10: Reduced Inequalities\nSDG 13: Climate Action"
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater)  context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.about_slide_layout, container, false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.slide_image);
        TextView slideHeading = (TextView) view.findViewById(R.id.slide_heading);
        TextView slideDescription = (TextView) view.findViewById(R.id.slide_desc);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_descriptions[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout)object);
    }
}
