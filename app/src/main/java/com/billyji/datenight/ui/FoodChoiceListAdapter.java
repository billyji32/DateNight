package com.billyji.datenight.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.billyji.datenight.R;
import com.billyji.datenight.data.YelpDataModel;
import com.billyji.datenight.network.YelpDataGetter;
import com.squareup.picasso.Picasso;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.Category;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FoodChoiceListAdapter extends ArrayAdapter<String> {
    @BindView(R.id.stars)
    ImageView m_numStars;
    @BindView(R.id.food_picture)
    ImageView m_foodPicture;
    @BindView(R.id.reviews)
    TextView m_reviews;
    @BindView(R.id.restaurant_name)
    TextView m_restaurantName;
    @BindView(R.id.categories)
    TextView m_restaurantCategories;
    @BindView(R.id.dollar_signs)
    TextView m_dollarSigns;
    @BindView(R.id.distance)
    TextView m_distance;
    @BindView(R.id.phone_number)
    @Nullable
    TextView m_phoneNumber;
    @BindView(R.id.address)
    @Nullable
    TextView m_address;
    @BindView(R.id.website_link)
    @Nullable
    TextView m_website;

    private final Activity m_context;
    private final YelpDataModel m_businessModel = new YelpDataModel(YelpDataGetter.Companion.getM_listBusinesses());
    private Business m_curBusiness;
    private boolean m_onlyOneBusiness;

    FoodChoiceListAdapter(Activity context, List<String> listSizeReference)
    {
        super(context, R.layout.food_list, listSizeReference);

        this.m_context = context;
        m_businessModel.getFiveBusinesses();
    }

    @Override
    public @NonNull
    View getView(int position, View view, @NonNull ViewGroup parent)
    {
        View rowView = inflateRowView(parent);
        ButterKnife.bind(this, rowView);

        m_curBusiness = m_businessModel.getBusinesses().get(position);
        setUpView(position);

        if (m_address != null && m_phoneNumber != null && m_website != null)
        {
            setAdditionalRestaurantDetails();
        }

        return rowView;
    }

    private View inflateRowView(ViewGroup parent)
    {
        View rowView;
        if (m_onlyOneBusiness)
        {
            rowView = LayoutInflater.from(m_context).inflate(R.layout.selected_food_detail, parent, false);
        }
        else
        {
            rowView = LayoutInflater.from(m_context).inflate(R.layout.food_list, parent, false);
        }

        return rowView;
    }

    void removeBusiness(int position)
    {
        if(m_businessModel.removeBusiness(position))
        {
            ((FoodChoiceActivity) m_context).update(m_businessModel.getBusinessListSize());
            notifyDataSetChanged();
        }
    }

    void setOnlyOneBusiness()
    {
        m_onlyOneBusiness = true;
    }

    private void setUpView(int position)
    {
        setFoodPicture(position);
        setText();
        setStars();
    }

    private void setFoodPicture(int position)
    {
        Picasso
            .with(m_context)
            .load(m_curBusiness.getImageUrl())
            .resize(100, 100)
            .centerCrop()
            .into(m_foodPicture);

        setListenerForLargePicture(position);
    }

    private void setText()
    {
        setRestaurantName();
        setRestaurantCategories();
        setReviews();
        setDollarSigns();
        setDistance();
    }

    private void setStars()
    {
        String numStars = Double.toString(m_curBusiness.getRating());
        String processedNumStars = numStars.replace('.', '_');

        int resourceID = m_context.getResources().getIdentifier("stars_" + processedNumStars, "drawable", m_context.getPackageName());

        m_numStars.setImageResource(resourceID);
    }

    private void setReviews()
    {
        String numReview = m_curBusiness.getReviewCount() + " Reviews";
        m_reviews.setText(numReview);
    }

    private void setRestaurantName()
    {
        m_restaurantName.setText(m_curBusiness.getName());
    }

    private void setRestaurantCategories()
    {
        StringBuilder allCategories = new StringBuilder();
        for (Category category : m_curBusiness.getCategories())
        {
            allCategories.append(category.getTitle());
            allCategories.append(", ");
        }
        allCategories.deleteCharAt(allCategories.length() - 2);

        m_restaurantCategories.setText(allCategories);
    }

    private void setDollarSigns()
    {
        StringBuilder dollarSigns = new StringBuilder();
        for (int i = 0; i < m_curBusiness.getPrice().length(); i++)
        {
            dollarSigns.append('$');
        }

        m_dollarSigns.setText(dollarSigns);
    }

    private void setDistance()
    {
        m_distance.setText(m_businessModel.getDistance(m_curBusiness));
    }

    private void setAdditionalRestaurantDetails()
    {
        setPhoneNumber();
        setAddress();
        setWebsite();
    }

    private void setPhoneNumber()
    {
        if(m_phoneNumber != null)
        {
            String callNumber = "Call " + m_curBusiness.getDisplayPhone();
            m_phoneNumber.setText(callNumber);
            m_phoneNumber.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + m_curBusiness.getDisplayPhone()));
                    m_context.startActivity(intent);
                }
            });
        }
    }

    private void setAddress()
    {
        if(m_address != null)
        {
            m_address.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String uri = "geo:0,0?q=" + m_curBusiness.getName();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    m_context.startActivity(intent);
                }
            });
        }
    }

    private void setWebsite()
    {
        if(m_website != null)
        {
            m_website.setClickable(true);
            m_website.setMovementMethod(LinkMovementMethod.getInstance());
            String text = "<a href='" + m_curBusiness.getUrl() + "'>" + "Check out " + m_curBusiness.getName() + " on Yelp" + "</a>";
            m_website.setText(Html.fromHtml(text));
        }
    }

    private void setListenerForLargePicture(final int position)
    {
        final View customView = LayoutInflater.from(m_context).inflate(R.layout.popup_dialog, null);
        final ImageView popupDialog = customView.findViewById(R.id.tv);

        Picasso
                .with(m_context)
                .load(m_businessModel.getBusinesses().get(position).getImageUrl())
                .resize(400, 400)
                .centerCrop()
                .into(popupDialog);

        m_foodPicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final PopupWindow popupWindow = new PopupWindow(
                        customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
                );

                popupWindow.setFocusable(true);
                popupWindow.setTouchInterceptor(new OnTouchListener()
                {
                    public boolean onTouch(View v, MotionEvent event)
                    {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                        popupWindow.dismiss();
                        break;
                        case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                        default:
                        break;
                    }
                    return true;
                    }
                });

                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });
    }
}