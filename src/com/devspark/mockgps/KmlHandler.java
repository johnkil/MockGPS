package com.devspark.mockgps;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * 
 * @author johnkil
 *
 */
public class KmlHandler  extends DefaultHandler {
	private static final String TAG = KmlHandler.class.getSimpleName();
	
	private static final String TAG_COORDINATES = "coordinates";
	
	private List<String> coordinates;
	private String currentTag;
	
	public List<String> getCoordinates() {
		return coordinates;
	}
	
	@Override
	public void startDocument() throws SAXException {
		Log.v(TAG, "startDocument() called");
		coordinates = new ArrayList<String>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String tagName = localName;
		if (tagName.equals("")) {
			tagName = qName;
		}
		currentTag = tagName;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String tagName = localName;
		if (tagName.equals("")) {
			tagName = qName;
		}
		currentTag = null;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String value = new String(ch, start, length);
		if (TAG_COORDINATES.equals(currentTag)) {
			coordinates.add(value);
		}
	}

}
