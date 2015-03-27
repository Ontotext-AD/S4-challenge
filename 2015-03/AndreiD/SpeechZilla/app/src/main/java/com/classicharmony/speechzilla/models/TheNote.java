package com.classicharmony.speechzilla.models;

/**
 * Created by admin on 3/26/2015.
 */
public class TheNote {


    public TheNote(){

    }

    public TheNote(String _full_text,String _location_list, String _organization_list, String _keywords, String _created_at){
        this.full_text = _full_text;
        this.location_list = _location_list;
        this.organization_list = _organization_list;
        this.keywords = _keywords;
        this.created_at = _created_at;
    }

    int id;

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    String created_at;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFull_text() {
        return full_text;
    }

    public void setFull_text(String full_text) {
        this.full_text = full_text;
    }

    public String getLocation_list() {
        return location_list;
    }

    public void setLocation_list(String location_list) {
        this.location_list = location_list;
    }

    public String getOrganization_list() {
        return organization_list;
    }

    public void setOrganization_list(String organization_list) {
        this.organization_list = organization_list;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    String full_text;
    String location_list;
    String organization_list;
    String keywords;

}
