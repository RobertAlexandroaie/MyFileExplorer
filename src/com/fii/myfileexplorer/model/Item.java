/**
 * 
 */
package com.fii.myfileexplorer.model;

/**
 * @author Robert
 * 
 */
public class Item implements Comparable<Item> {
    private String name;
    private String data;
    private String path;
    private String image;
    private String date;

    public Item(String name, String data, String date, String path, String image) {
	this.name = name;
	this.data = data;
	this.path = path;
	this.image = image;
	this.date = date;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the data
     */
    public String getData() {
	return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(String data) {
	this.data = data;
    }

    /**
     * @return the path
     */
    public String getPath() {
	return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
	this.path = path;
    }

    /**
     * @return the image
     */
    public String getImage() {
	return image;
    }

    /**
     * @param image
     *            the image to set
     */
    public void setImage(String image) {
	this.image = image;
    }

    /**
     * @return the date
     */
    public String getDate() {
	return date;
    }

    /**
     * @param date
     *            the date to set
     */
    public void setDate(String date) {
	this.date = date;
    }

    @Override
    public int compareTo(Item another) throws IllegalArgumentException {
	if (name != null) {
	    int ret = name.toLowerCase().compareTo(another.getName().toLowerCase());
	    if (ret == 0) {
		ret = data.toLowerCase().compareTo(another.getName().toLowerCase());
	    }
	    return ret;
	} else {
	    throw new IllegalArgumentException();
	}
    }

}
