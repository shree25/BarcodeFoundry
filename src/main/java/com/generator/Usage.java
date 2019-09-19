package com.generator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "usage_log") // This tells Hibernate to make a table out of this class
public class Usage {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    @Column(name="user_id")
    private Integer userId;
    private String text;
    private String type;
    private String format;
    private String code;
    private String datetime;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUser_id() {
        return userId;
    }
    public void setUser_id(Integer user_id) { this.userId = user_id; }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getDatetime() {
        return datetime;
    }
    public void setDatetime(String datetime) {this.datetime = datetime;}
}