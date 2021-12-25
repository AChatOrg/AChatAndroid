package com.hyapp.achat.ui.model;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hyapp.achat.R;

public class GroupAvatarView extends ConstraintLayout {

    private SimpleDraweeView[] draweeViews;
    private int cornerRadius;
    private int placeHolderCircleRes;
    private int placeHolderLeftRes;
    private int placeHolderRightRes;
    private int placeHolderTopLeftRes;
    private int placeHolderTopRightRes;
    private int placeHolderBottomRightRes;
    private int placeHolderBottomLeftRes;

    public GroupAvatarView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public GroupAvatarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public GroupAvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GroupAvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GroupAvatarView, defStyleAttr, defStyleRes);
        cornerRadius = typedArray.getDimensionPixelSize(R.styleable.GroupAvatarView_cornerRadius, 0);
        placeHolderCircleRes = typedArray.getResourceId(R.styleable.GroupAvatarView_placeholderCircle, 0);
        placeHolderLeftRes = R.drawable.placeholder_avatar_group_left;
        placeHolderRightRes = R.drawable.placeholder_avatar_group_right;
        placeHolderTopLeftRes = R.drawable.placeholder_avatar_group_top_left;
        placeHolderTopRightRes = R.drawable.placeholder_avatar_group_top_right;
        placeHolderBottomRightRes = R.drawable.placeholder_avatar_group_bottom_right;
        placeHolderBottomLeftRes = R.drawable.placeholder_avatar_group_bottom_left;
        typedArray.recycle();

        inflate(context, R.layout.group_avatar, this);

        draweeViews = new SimpleDraweeView[]{
                findViewById(R.id.one)
                , findViewById(R.id.two)
                , findViewById(R.id.three)
                , findViewById(R.id.four)
        };
    }


    private void setTo4part() {
        ((LayoutParams) draweeViews[3].getLayoutParams()).leftMargin = 0;

        RoundingParams[] roundingParams = new RoundingParams[]{
                new RoundingParams().setCornersRadii(cornerRadius, 0, 0, 0)
                , new RoundingParams().setCornersRadii(0, 0, 0, cornerRadius)
                , new RoundingParams().setCornersRadii(0, cornerRadius, 0, 0)
                , new RoundingParams().setCornersRadii(0, 0, cornerRadius, 0)
        };

        int[] placeHolders = new int[]{
                placeHolderTopLeftRes
                , placeHolderBottomLeftRes
                , placeHolderTopRightRes
                , placeHolderBottomRightRes
        };

        for (int i = 0; i < draweeViews.length; i++) {
            SimpleDraweeView draweeView = draweeViews[i];
            draweeView.setVisibility(VISIBLE);
            GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
            hierarchy.setPlaceholderImage(placeHolders[i]);
            hierarchy.setRoundingParams(roundingParams[i].setBorder(Color.WHITE, 1));
        }
    }

    private void setTo3part() {

        draweeViews[1].setVisibility(GONE);
        draweeViews[0].setVisibility(VISIBLE);
        draweeViews[2].setVisibility(VISIBLE);
        draweeViews[3].setVisibility(VISIBLE);
        ((LayoutParams) draweeViews[3].getLayoutParams()).leftMargin = cornerRadius;

        RoundingParams leftParams = new RoundingParams()
                .setCornersRadii(cornerRadius, 0, 0, cornerRadius)
                .setBorder(Color.WHITE, 1);
        RoundingParams topRightParams = new RoundingParams()
                .setCornersRadii(0, cornerRadius, 0, 0)
                .setBorder(Color.WHITE, 1);
        RoundingParams bottomRightParams = new RoundingParams()
                .setCornersRadii(0, 0, cornerRadius, 0)
                .setBorder(Color.WHITE, 1);

        GenericDraweeHierarchy leftHierarchy = draweeViews[0].getHierarchy();
        leftHierarchy.setPlaceholderImage(placeHolderLeftRes);
        leftHierarchy.setRoundingParams(leftParams);

        GenericDraweeHierarchy topRightHierarchy = draweeViews[2].getHierarchy();
        topRightHierarchy.setPlaceholderImage(placeHolderTopRightRes);
        topRightHierarchy.setRoundingParams(topRightParams);

        GenericDraweeHierarchy bottomRightHierarchy = draweeViews[3].getHierarchy();
        bottomRightHierarchy.setPlaceholderImage(placeHolderBottomRightRes);
        bottomRightHierarchy.setRoundingParams(bottomRightParams);
    }

    private void setTo2part() {

        draweeViews[1].setVisibility(GONE);
        draweeViews[3].setVisibility(GONE);
        draweeViews[0].setVisibility(VISIBLE);
        draweeViews[2].setVisibility(VISIBLE);

        RoundingParams leftParams = new RoundingParams()
                .setCornersRadii(cornerRadius, 0, 0, cornerRadius)
                .setBorder(Color.WHITE, 1);
        RoundingParams rightParams = new RoundingParams()
                .setCornersRadii(0, cornerRadius, cornerRadius, 0)
                .setBorder(Color.WHITE, 1);

        GenericDraweeHierarchy leftHierarchy = draweeViews[0].getHierarchy();
        leftHierarchy.setPlaceholderImage(placeHolderLeftRes);
        leftHierarchy.setRoundingParams(leftParams);

        GenericDraweeHierarchy rightHierarchy = draweeViews[2].getHierarchy();
        rightHierarchy.setPlaceholderImage(placeHolderRightRes);
        rightHierarchy.setRoundingParams(rightParams);
    }

    private void setToCircle() {
        draweeViews[1].setVisibility(GONE);
        draweeViews[2].setVisibility(GONE);
        draweeViews[3].setVisibility(GONE);
        draweeViews[0].setVisibility(VISIBLE);

        RoundingParams circleParams = new RoundingParams().setRoundAsCircle(true);

        GenericDraweeHierarchy hierarchy = draweeViews[0].getHierarchy();
        hierarchy.setPlaceholderImage(placeHolderCircleRes);
        hierarchy.setRoundingParams(circleParams);
    }

    public void setAvatars(String... avatars) {
        int length = Math.min(draweeViews.length, avatars.length);

        switch (length) {
            case 4:
                setTo4part();
                for (int i = 0; i < length; i++) {
                    draweeViews[i].setImageURI(avatars[i]);
                }
                return;
            case 3:
                setTo3part();
                draweeViews[0].setImageURI(avatars[0]);
                draweeViews[2].setImageURI(avatars[1]);
                draweeViews[3].setImageURI(avatars[2]);
                return;
            case 2:
                setTo2part();
                draweeViews[0].setImageURI(avatars[0]);
                draweeViews[2].setImageURI(avatars[1]);
                return;
            case 1:
                setToCircle();
                draweeViews[0].setImageURI(avatars[0]);
                return;
            case 0:
                setToCircle();
                draweeViews[0].setImageURI((Uri) null);
        }
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public int getPlaceHolderTopLeftRes() {
        return placeHolderTopLeftRes;
    }

    public int getPlaceHolderTopRightRes() {
        return placeHolderTopRightRes;
    }

    public int getPlaceHolderBottomRightRes() {
        return placeHolderBottomRightRes;
    }

    public int getPlaceHolderBottomLeftRes() {
        return placeHolderBottomLeftRes;
    }

    public SimpleDraweeView[] getDraweeViews() {
        return draweeViews;
    }

    public int getPlaceHolderCircleRes() {
        return placeHolderCircleRes;
    }

    public int getPlaceHolderLeftRes() {
        return placeHolderLeftRes;
    }

    public int getPlaceHolderRightRes() {
        return placeHolderRightRes;
    }
}
