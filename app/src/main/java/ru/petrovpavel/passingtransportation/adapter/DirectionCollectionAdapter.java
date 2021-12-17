package ru.petrovpavel.passingtransportation.adapter;
/*
 * Copyright (C)2021 Petrov Pavel
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
 */

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.Locale;

import ru.petrovpavel.passingtransportation.fragment.DriveCollectionFragment;
import ru.petrovpavel.passingtransportation.R;


public class DirectionCollectionAdapter extends CursorAdapter {
    private static final String LOG_TAG = DirectionCollectionAdapter.class.getSimpleName();

    public DirectionCollectionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_nav_collection_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
       This is where we fill-in the views with the contents of the cursor.
    */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String START_NAME = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_START_NAME);
        String START_LAT = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_START_LAT);
        String START_LONG = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_START_LONG);
        String DEST_NAME = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_DEST_NAME);
        String DEST_LAT = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_DEST_LAT);
        String DEST_LONG = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_DEST_LONG);
        String MODE = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_MODE);
        String ROUTE_ID = cursor.getString(DriveCollectionFragment.COL_WAYPOINTS_ROUTE_ID);
        double ROUTE_DURATION = cursor.getDouble(DriveCollectionFragment.COL_WAYPOINTS_ROUTE_DURATION);
        double ROUTE_DISTANCE = cursor.getDouble(DriveCollectionFragment.COL_WAYPOINTS_ROUTE_DISTANCE);

        ViewHolder viewHolder = (ViewHolder) view.getTag();


        viewHolder.start_name.setText(firstCapital(START_NAME));
        viewHolder.dest_name.setText(firstCapital(DEST_NAME));

        MODE = MODE.toLowerCase();
        viewHolder.mode.setImageDrawable(ContextCompat.getDrawable(context, MODE.equals(context.getString(R.string.cycling)) ? R.drawable.ic_directions_bike_black_24dp : (MODE.equals(context.getString(R.string.driving)) ? R.drawable.ic_drive_eta_black_24dp : R.drawable.ic_directions_walk_black_24dp)));

        if (ROUTE_DISTANCE >= 500) {
            ROUTE_DISTANCE /= 1000;
            viewHolder.distance.setText(String.format(Locale.US, "%.2f" + context.getString(R.string.km), ROUTE_DISTANCE));
        } else {
            viewHolder.distance.setText(String.format(Locale.US, "%.0f" + context.getString(R.string.m), ROUTE_DISTANCE));
        }
        if (ROUTE_DURATION >= 60) {
            int min = (int) Math.floor(ROUTE_DURATION / 60);
            int sec = (int) (ROUTE_DURATION - min * 60);
            viewHolder.duration.setText(String.format(Locale.US, "%d " + context.getString(R.string.min) + " %d" + context.getString(R.string.sec), min, sec));
        } else {
            viewHolder.duration.setText(String.format(Locale.US, "%d" + context.getString(R.string.sec), (int) ROUTE_DURATION));
        }

    }

    public String firstCapital(String str) {
        return str.length() > 0 ? str.substring(0, 1).toUpperCase() + (str.length() > 1 ? str.substring(1) : " ") : " ";
    }

    public static class ViewHolder {

        public final ImageView mode;
        public final TextView start_name;
        public final TextView dest_name;
        public final TextView distance;
        public final TextView duration;

        public ViewHolder(View view) {
            mode = (ImageView) view.findViewById(R.id.mode);
            start_name = (TextView) view.findViewById(R.id.start_name);
            dest_name = (TextView) view.findViewById(R.id.dest_name);
            distance = (TextView) view.findViewById(R.id.distance);
            duration = (TextView) view.findViewById(R.id.duration);
        }
    }
}