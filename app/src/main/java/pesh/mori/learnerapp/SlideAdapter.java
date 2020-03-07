package pesh.mori.learnerapp;

import android.content.Context;
import android.graphics.Color;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlideAdapter extends PagerAdapter {
    Context context;
    LayoutInflater inflater;

    // list of images
    public int[] lst_images = {
            R.drawable.welcomee,
            R.drawable.learn,
            R.drawable.earn,
            R.drawable.welcomee
    };
    // list of titles
    public String[] lst_title = {
            "WELCOME",
            "LEARNING",
            "EARNING",
            "PROCEED..."
    }   ;
    // list of descriptions
    public String[] lst_description = {
            "Hi there! My name is Nelan, and I am Zawadi. Before you proceed allow us to ON-BOARD you on the L'earnerApp Experience!",
            "L'earnerApp gives me access to knowledge based content from across Africa, in document, video or audio format, on-the-go. If ever I am short of cash, L'earnerApp allows me to Bid for the content at a lower price, suitable for my budget. Salient features such as Note-Taking, make my learning experience worth-while.",
            "Using L'earnerApp I am able to trade off my most basic asset, my KNOWLEDGE, it may be a skill, a digital resource or a DIY. I can trade it off to Zawadi for a price, L'earnerApp gives me the freedom to; set content pricing, accept or decline bids on my content, transfer my tokens to other users or even redeem my earned tokens for cash straight to my mobile account hassle free. ",
            "To put it simply L'earnerApp allows you to not only Learn but also Earn at the palm of your hands. We cannot wait to see you on the other side!"
    };
    // list of background colors
    public int[]  lst_backgroundcolor = {
            Color.rgb(11,16,32),
            Color.rgb(51,11,20),
            Color.rgb(11,16,32),
            Color.rgb(51,11,20),
    };


    public SlideAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return lst_title.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide,container,false);
        LinearLayout layoutslide = (LinearLayout) view.findViewById(R.id.slidelinearlayout);
        ImageView imgslide = (ImageView)  view.findViewById(R.id.slideimg);
        TextView txttitle= (TextView) view.findViewById(R.id.txttitle);
        TextView description = (TextView) view.findViewById(R.id.txtdescription);
        layoutslide.setBackgroundColor(lst_backgroundcolor[position]);
        imgslide.setImageResource(lst_images[position]);
        txttitle.setText(lst_title[position]);
        description.setText(lst_description[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }
}
