package main.tl.com.timelogger.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vipulmittal on 14/10/16.
 */
public class Destination {
    @SerializedName("city_name")
    @Expose
    private String cityName;
    @SerializedName("rtl")
    @Expose
    private Integer rtl;
    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("dest_id")
    @Expose
    private String destId;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("dest_type")
    @Expose
    private String destType;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("lc")
    @Expose
    private String lc;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("nr_hotels")
    @Expose
    private String nrHotels;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("city_ufi")
    @Expose
    private Object cityUfi;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("hotels")
    @Expose
    private String hotels;
    @SerializedName("cc1")
    @Expose
    private String cc1;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The cityName
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * @param cityName The city_name
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * @return The rtl
     */
    public Integer getRtl() {
        return rtl;
    }

    /**
     * @param rtl The rtl
     */
    public void setRtl(Integer rtl) {
        this.rtl = rtl;
    }

    /**
     * @return The region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region The region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return The destId
     */
    public String getDestId() {
        return destId;
    }

    /**
     * @param destId The dest_id
     */
    public void setDestId(String destId) {
        this.destId = destId;
    }

    /**
     * @return The country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country The country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return The destType
     */
    public String getDestType() {
        return destType;
    }

    /**
     * @param destType The dest_type
     */
    public void setDestType(String destType) {
        this.destType = destType;
    }

    /**
     * @return The latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * @param latitude The latitude
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * @return The lc
     */
    public String getLc() {
        return lc;
    }

    /**
     * @param lc The lc
     */
    public void setLc(String lc) {
        this.lc = lc;
    }

    /**
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return The longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * @param longitude The longitude
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * @return The nrHotels
     */
    public String getNrHotels() {
        return nrHotels;
    }

    /**
     * @param nrHotels The nr_hotels
     */
    public void setNrHotels(String nrHotels) {
        this.nrHotels = nrHotels;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The cityUfi
     */
    public Object getCityUfi() {
        return cityUfi;
    }

    /**
     * @param cityUfi The city_ufi
     */
    public void setCityUfi(Object cityUfi) {
        this.cityUfi = cityUfi;
    }

    /**
     * @return The label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return The hotels
     */
    public String getHotels() {
        return hotels;
    }

    /**
     * @param hotels The hotels
     */
    public void setHotels(String hotels) {
        this.hotels = hotels;
    }

    /**
     * @return The cc1
     */
    public String getCc1() {
        return cc1;
    }

    /**
     * @param cc1 The cc1
     */
    public void setCc1(String cc1) {
        this.cc1 = cc1;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
