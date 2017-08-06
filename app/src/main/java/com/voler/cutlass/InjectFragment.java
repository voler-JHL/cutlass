package com.voler.cutlass;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.voler.cutlass.annotation.InjectField;

import java.util.ArrayList;

/**
 * InjectFragment Created by voler on 2017/6/30.
 * 说明：
 */

public class InjectFragment extends android.support.v4.app.Fragment{
    @InjectField
    public String inject;
    @InjectField
    public ArrayList<Parcelable> yui;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Cutlass.inject(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
