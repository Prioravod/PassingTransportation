package ru.petrovpavel.passingtransportation.data;
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
import android.os.Parcel;
import android.os.Parcelable;

public class MapData implements Parcelable {
    public final Creator<MapData> CREATOR = new Creator<MapData>() {
        @Override
        public MapData createFromParcel(Parcel parcel) {
            return new MapData(parcel);
        }

        @Override
        public MapData[] newArray(int size) {
            return new MapData[size];
        }
    };

    private double start_lat;
    private double start_long;
    private double dest_lat;
    private double dest_long;
    private String mode;
    private int markerStart;
    private int markerDest;
    private int polyLine;

    public MapData(double start_lat, double start_long, double dest_lat, double dest_long, String mode, int markerStart, int markerDest, int polyLine) {
        this.start_lat = start_lat;
        this.start_long = start_long;
        this.dest_lat = dest_lat;
        this.dest_long = dest_long;
        this.mode = mode;
        this.markerStart = markerStart;
        this.markerDest = markerDest;
        this.polyLine = polyLine;
    }

    private MapData(Parcel in) {
        this.start_lat = in.readDouble();
        this.start_long = in.readDouble();
        this.dest_lat = in.readDouble();
        this.dest_long = in.readDouble();
        this.mode = in.readString();
        this.markerStart = in.readInt();
        this.markerDest = in.readInt();
        this.polyLine = in.readInt();
    }

    public int getMarkerDest() {
        return markerDest;
    }

    public Creator<MapData> getCREATOR() {
        return CREATOR;
    }

    public double getDest_lat() {
        return dest_lat;
    }

    public double getDest_long() {
        return dest_long;
    }

    public int getMarkerStart() {
        return markerStart;
    }

    public String getMode() {
        return mode;
    }

    public int getPolyLine() {
        return polyLine;
    }

    public double getStart_lat() {
        return start_lat;
    }

    public double getStart_long() {
        return start_long;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(start_lat);
        dest.writeDouble(start_long);
        dest.writeDouble(dest_lat);
        dest.writeDouble(dest_long);
        dest.writeString(mode);
        dest.writeInt(markerStart);
        dest.writeInt(markerDest);
        dest.writeInt(polyLine);
    }
}
