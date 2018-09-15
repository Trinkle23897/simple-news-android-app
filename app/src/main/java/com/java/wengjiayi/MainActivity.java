package com.java.wengjiayi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

//import com.handmark.pulltorefresh.library.PullToRefreshBase;
//import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.java.wengjiayi.rss.*;
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v4.app.NavUtils;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnItemClickListener{//, NavigationView.OnNavigationItemSelectedListener {

    // 从网络获取RSS地址
    public String RSS_URL = "http://news.qq.com/newssh/rss_newssh.xml";
    public String RSS_URL2 = "http://news.qq.com/newssh/rss_newssh.xml";
    public String prefix = "";
    public final String tag = "RSSReader";
    public String[] urls = {"http://www.people.com.cn/rss/politics.xml",
                            "http://www.people.com.cn/rss/world.xml",
                            "http://www.people.com.cn/rss/finance.xml",
                            "http://www.people.com.cn/rss/sports.xml",
                            "http://www.people.com.cn/rss/haixia.xml",
                            "http://www.people.com.cn/rss/edu.xml",
                            "http://www.people.com.cn/rss/culture.xml",
                            "http://www.people.com.cn/rss/game.xml"};
    private RssFeed feed = null, feed2 = null;
    private ListView newsList;
    private SimpleAdapter simpleAdapter;
    boolean[] choiceSets = {true,true,true,true,true,true,true,true};
    int[] count = {0,0,0,0,0,0,0,0};
    Context context;
    public void getBest() {
        int id = 0, id2 = 0, max = 0, max2 = 0;
        for (int i = 0; i < count.length; ++i) {
            if (max < count[i]) {
                id2 = id;
                max2 = max;
                id = i;
                max = count[i];
            } else if (count[i] <= max && max2 < count[i]) {
                id2 = i;
                max2 = count[i];
            }
        }
        RSS_URL = urls[id];
        RSS_URL2 = urls[id2];
        Log.i("best", RSS_URL+" "+RSS_URL2);
        Log.i("best", id + " " + id2);
        Log.i("best", " "+count[0]+" "+count[1]+" "+count[2]+" "+count[3]+" "+count[4]+" "+count[5]+" "+count[6]+" "+count[7]);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        // 获取控件
        RSS_URL = "http://www.people.com.cn/rss/world.xml";
//        TextView tv = (TextView)findViewById(R.id.title);
//        tv.setText("国际新闻");
        prefix = getFilesDir().getPath();
        Log.i("favorDir", prefix+"/favor");
        File favor = new File(prefix+"/favor");
        if (!favor.exists()) favor.mkdirs();
        File file = new File(prefix+"/9bf6d3581229");
        String state = "11111111";
        if (file.exists()) {
            try {
                FileInputStream inStream=this.openFileInput("9bf6d3581229");
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                byte[] buffer=new byte[8];
                int length=-1;
                if((length=inStream.read(buffer))!=-1)
                    stream.write(buffer,0,length);
                stream.close();
                inStream.close();
                state = stream.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        File rec = new File(prefix+"/rec.txt");
        if (rec.exists()) {
            try {
                Scanner scan = new Scanner(rec);
                for (int i = 0; i < 8; ++i)
                    count[i] = scan.nextInt();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        getBest();
        Log.i("init", state);
        setUpColumn(R.id.col_, "recommend");
        setUpColumn(R.id.col0, "favor");
        setUpColumn(R.id.col1, urls[0]);
        setUpColumn(R.id.col2, urls[1]);
        setUpColumn(R.id.col3, urls[2]);
        setUpColumn(R.id.col4, urls[3]);
        setUpColumn(R.id.col5, urls[4]);
        setUpColumn(R.id.col6, urls[5]);
        setUpColumn(R.id.col7, urls[6]);
        setUpColumn(R.id.col8, urls[7]);
        setState(state);
        setPullToRefesh();
        refreshMain();
    }
    private int findUrl(String s) {
        for (int i = 0; i < urls.length; ++i)
            if (urls[i].equals(s))
                return i;
        return -1;
    }
    private void setState(String state) {
        TextView tv;
        Log.i("state", state);
        for (int i = 0; i < urls.length; ++i) {
            int id;
            if (i == 0) id = R.id.col1;
            else if (i == 1) id = R.id.col2;
            else if (i == 2) id = R.id.col3;
            else if (i == 3) id = R.id.col4;
            else if (i == 4) id = R.id.col5;
            else if (i == 5) id = R.id.col6;
            else if (i == 6) id = R.id.col7;
            else id = R.id.col8;
            tv = (TextView) findViewById(id);
            if (state.charAt(i) == '1') {
                tv.setVisibility(View.VISIBLE);
                Log.i("state", "true");
                choiceSets[i] = true;
            }
            else {
                tv.setVisibility(View.GONE);
                Log.i("state", "false");
                choiceSets[i] = false;
            }
        }
    }
    private void refreshMain() {
        Log.i("rssurl:", RSS_URL);
        if (!NetworkAvail.check(this)) {
            ShowDescriptionActivity.show("没有网络qwq请检查", this, true);
            Log.i("network", "no");
            return;
        }
        if (RSS_URL.startsWith("http")) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        feed = new RssFeed_SAXParser().getFeed(RSS_URL);
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i("find", " "+findUrl(RSS_URL));
            Log.i("find", " "+RSS_URL);
            feed.modify(findUrl(RSS_URL));
        }
        else if (RSS_URL.equals("favor")){ // 收藏
            feed = new RssFeed();
            File[] favors = new File(prefix+"/favor").listFiles();
            for (int i = 0; i < favors.length; ++i) {
                System.out.println(favors[i].getPath());
                feed.addItem(new RssItem(readFile(favors[i].getPath())));
            }
        }
        else { // 推荐
            getBest();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        feed = new RssFeed_SAXParser().getFeed(RSS_URL);
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            feed.modify(findUrl(RSS_URL));
            //-----
            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        feed2 = new RssFeed_SAXParser().getFeed(RSS_URL2);
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            feed2.modify(findUrl(RSS_URL2));
            feed = merge(feed, feed2);
            RSS_URL = "recommend";
        }
        showListView();
    }
    private RssFeed merge(RssFeed f1, RssFeed f2) {
        feed = new RssFeed();
        f1.refresh();
        f2.refresh();
        f1.getAllItems(getFilesDir().toString());
        f2.getAllItems(getFilesDir().toString());
        // 7:3
        for (int i = 0; i < f1.getSize() && feed.getSize() < 7; ++i)
            if (!f1.getItem(i).getTitle().contains("[已读] "))
                feed.addItem(f1.getItem(i));
        for (int i = 0; i < f2.getSize() && feed.getSize() < 10; ++i)
            if (!f2.getItem(i).getTitle().contains("[已读] "))
                feed.addItem(f2.getItem(i));
        return feed;
    }
    private String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        String s = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while( (s = br.readLine()) != null) {
                sb.append(s + "\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public void showMultiChoiceDialog(final Context context) {
        final ArrayList<Integer> yourChoices = new ArrayList<>();
        final String[] items = { "国内新闻","国际新闻","经济新闻","体育新闻","台湾新闻","教育新闻","文化新闻","游戏新闻" };
        // 设置默认选中的选项，全为false默认均未选中
//        yourChoices.clear();
//        for (int i = 0; i < items.length; ++i)
//            if (choiceSets[i])
//                yourChoices.add(i);
//        System.out.println(yourChoices);
        android.support.v7.app.AlertDialog.Builder multiChoiceDialog =
                new android.support.v7.app.AlertDialog.Builder(context);
        multiChoiceDialog.setTitle("请选择要订阅的新闻类型");
        multiChoiceDialog.setMultiChoiceItems(items, choiceSets,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        Log.i("choice", " "+which);
                        if (isChecked) {
                            choiceSets[which] = true;
//                            yourChoices.add(which);
                        } else {
                            choiceSets[which] = false;
                        }
                    }
                });
        multiChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        int size = yourChoices.size();
//                        boolean[] choice = {false,false,false,false,false,false,false,false};
                        String str = "";
                        String file = "";
                        for (int i = 0; i < choiceSets.length; ++i) {
                            if (choiceSets[i]) {
                                str += items[i] + " ";
                                file += "1";
                            }
                            else
                                file += "0";
                        }
                        Toast.makeText(context,
                                "你选中了：" + str,
                                Toast.LENGTH_SHORT).show();
                        try {
                            FileOutputStream out = context.openFileOutput("9bf6d3581229", MODE_PRIVATE);
                            out.write(file.getBytes());
                            out.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setState(file);
                    }
                });
        multiChoiceDialog.show();
    }
    private void setPullToRefesh(){
        final PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout)findViewById(R.id.id_lv_up);
        pullToRefreshLayout.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String s = "没有更多新闻了哟～";
                        if (RSS_URL.equals("recommend"))
                            s = "又推荐了好多呢～";
                        refreshMain();
                        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
//                        feed.refresh();
                        // 结束刷新
                        pullToRefreshLayout.finishRefresh();
                    }
                }, 1000);

            }

            @Override
            public void loadMore() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (RSS_URL.equals("recommend")) {
                            refreshMain();
                            Toast.makeText(context, "又推荐了好多呢～", Toast.LENGTH_SHORT).show();
                            pullToRefreshLayout.finishLoadMore();
                        }
                        else {
                            feed.refresh();
                            simpleAdapter = new SimpleAdapter(context, feed.getAllItems(getFilesDir().toString()), android.R.layout.simple_list_item_2, new String[]{RssItem.TITLE, RssItem.PUBDATE,}, new int[]{android.R.id.text1, android.R.id.text2});
                            newsList.setAdapter(simpleAdapter);
                            newsList.setSelection(newsList.getHeaderViewsCount());
//                        noMoreFlag = true;
                            // 结束刷新
                            pullToRefreshLayout.finishLoadMore();
                            Toast.makeText(context, "又加载了好多呢～", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 1000);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            showMultiChoiceDialog(context);
//            Toast.makeText(context, "要分享哪个新闻呢？", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_search) {
            searchContent();
            return true;
        }
        if (id == R.id.action_nofavor) {
            File[] favors = new File(prefix+"/favor").listFiles();
            for (int i = 0; i < favors.length; ++i) {
                new File(favors[i].getPath()).delete();
            }
            Toast.makeText(context, "成功清空收藏！", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_clear) {
            Toast.makeText(context, "缓存清空成功！", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void searchContent() {
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("在该页面搜索：").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = editText.getText().toString();
                        feed.setSearch(s);
                        showListView();
                        Toast.makeText(context,"已为您搜索关键字\""+s+"\"\n上拉即可恢复", Toast.LENGTH_SHORT);
//                        feed.setSearch("");
                    }
                }).show();
    }
    /*
     * 把RSS内容绑定到ui界面进行显示
     */
    private void setUpColumn(int id, final String url) {
        TextView tv = (TextView) findViewById(id);
        final String s = tv.getText().toString();
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("click!", url);
                //                Toast.makeText(context,"获取"+s+"中", Toast.LENGTH_SHORT).show();
                RSS_URL = url;
                refreshMain();
                Toast.makeText(context, s + "切换成功!\n向右滑动返回", Toast.LENGTH_SHORT).show();
//                Toast.makeText(context, s + "切换成功!\n向右滑动返回", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showListView() {
        Log.i(tag,"success");
        newsList = (ListView) this.findViewById(R.id.list);
        if (!NetworkAvail.check(this)) {
            ShowDescriptionActivity.show("没有网络qwq请检查", this, true);
            Log.i("network", "no");
            return;
        }
        else
            Log.i("network", "yes ");
        if (feed == null) {
            ShowDescriptionActivity.show("访问的RSS无效", this, true);
            return;
        }

        simpleAdapter = new SimpleAdapter(this,
                feed.getAllItems(getFilesDir().toString()), android.R.layout.simple_list_item_2,
                new String[] { RssItem.TITLE, RssItem.PUBDATE,  }, new int[] {
                android.R.id.text1, android.R.id.text2 });
        newsList.setAdapter(simpleAdapter);
        newsList.setOnItemClickListener(this);
        newsList.setSelection(0);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        position = feed.getmap(position);
        String s = feed.getItem(position).getTitle();

        Intent intent = new Intent();
        intent.setClass(this, ItemNewsActivity.class);
        intent.putExtra("link", feed.getItem(position).getLink());
        intent.putExtra("file", feed.getItem(position).toFileName());
        intent.putExtra("info", feed.getItem(position).toFavor());
        Bundle bundle = new Bundle();
        bundle.putString("title", feed.getItem(position).getTitle());
        bundle.putString("description",feed.getItem(position).getDescription());
        bundle.putString("link", feed.getItem(position).getLink());
        bundle.putString("pubdate", feed.getItem(position).getPubdate());
        // 用android.intent.extra.INTENT的名字来传递参数
        intent.putExtra("android.intent.extra.rssItem", bundle);
        startActivity(intent);
        int first = newsList.getFirstVisiblePosition();
        if (!s.startsWith("[已读] ")) {
            feed.getItem(position).setTitle("[已读] " + s);
            ++count[Integer.parseInt(feed.getItem(position).getCategory())];
            logCount();
            simpleAdapter = new SimpleAdapter(this,
                    feed.getAllItems(getFilesDir().toString()), android.R.layout.simple_list_item_2,
                    new String[] { RssItem.TITLE, RssItem.PUBDATE,  }, new int[] {
                    android.R.id.text1, android.R.id.text2 });
//            newsList.getChildAt(position-first).setBackgroundColor(getResources().getColor(R.color.colorAccent));
            newsList.setAdapter(simpleAdapter);
            newsList.setSelection(first);
        }
    }
    private void logCount() {
        String s = "";
        for (int i = 0; i < count.length; ++i)
            s += count[i] + " ";
        try {
            FileWriter fw = new FileWriter(prefix+"/rec.txt");
            fw.write(s);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
