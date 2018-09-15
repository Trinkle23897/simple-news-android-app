package com.java.wengjiayi.rss;
import java.io.*;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class RssFeed_SAXParser {

    public RssFeed getFeed(String urlStr) throws ParserConfigurationException, SAXException, IOException{
        URL url = new URL(urlStr);
        url.openStream();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance(); //构建SAX解析工厂
        SAXParser saxParser = saxParserFactory.newSAXParser(); //解析工厂生产解析器
        XMLReader xmlReader = saxParser.getXMLReader(); //通过saxParser构建xmlReader阅读器
        RssHandler rssHandler=new RssHandler();
        xmlReader.setContentHandler(rssHandler);
        //使用url打开流，并将流作为 xmlReader解析的输入源并解析
        InputSource inputSource = new InputSource(url.openStream());
        xmlReader.parse(inputSource);
        return rssHandler.getRssFeed();
    }
}