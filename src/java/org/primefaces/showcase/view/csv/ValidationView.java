package org.primefaces.showcase.view.csv;
 
import javax.faces.bean.ManagedBean;
 
@ManagedBean
public class ValidationView {
     
    private String text;
    private Integer integer;
 
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
 
    public Integer getInteger() {
        return integer;
    }
    public void setInteger(Integer integer) {
        this.integer = integer;
    }
}