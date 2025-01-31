package com.wallet.cold.app.pawn;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.wallet.R;
import com.wallet.cold.app.main.IndexActivity;
import com.wallet.cold.utils.Data;
import com.wallet.cold.utils.LocalManageUtil;
import com.wallet.cold.utils.LogCook;
import com.wallet.cold.utils.MyListView;
import com.wallet.cold.utils.Utils;
import com.wallet.cold.utils.WeiboDialogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

public class ShopActivity extends Activity implements View.OnClickListener{
    private Handler handler;
    private TextView fhgx,tztype,name1;
    private String result="";
    private Dialog mWeiboDialog;
    private MyListView lv1;
    private MyAdapter adapter;
    List<String> name = new ArrayList<String>();
    List<String> id = new ArrayList<String>();
    List<String> balance = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invest);
        Data.settype("shopactivity");
        Data.setcontext(ShopActivity.this);
        lv1=(MyListView)findViewById(R.id.list_zx);
        name1=(TextView)findViewById(R.id.name);name1.setText(R.string.p4);
        tztype=(TextView)findViewById(R.id.tztype);tztype.setText(R.string.sc1);tztype.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        fhgx=(TextView) findViewById(R.id.fhgx);fhgx.setOnClickListener(this);tztype.setOnClickListener(this);
        mWeiboDialog = WeiboDialogUtils.createLoadingDialog(ShopActivity.this, this.getResources().getString(R.string.f4));
        Data.setdialog(mWeiboDialog);
        if(Utils.isNetworkConnected(Data.getcontext())) {
            send1();
        }else{
            Toast.makeText(Data.getcontext(), Data.getcontext().getResources().getString(R.string.cd2), Toast.LENGTH_SHORT).show();
            WeiboDialogUtils.closeDialog(mWeiboDialog);
        }
        lv1.setonRefreshListener(new MyListView.OnRefreshListener() {
            public void onRefresh() {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void result) {
                        name.clear();
                        balance.clear();
                        id.clear();
                        send1();
                        lv1.onRefreshComplete();
                    }
                }.execute();
            }
        });
    }
    public void send1(){
        new Thread(new Runnable() {
            public void run() {
                send();
                Message m = handler.obtainMessage(); // 获取一个Message
                handler.sendMessage(m); // 发送消息
            }
        }).start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    Thread.sleep(1000);
                    if (result != null&&result!="") {
                        JSONObject jsonObject =new JSONObject(result);
                        LogCook.d("获取商城列表返回数据",String.valueOf(result));
                        String result1 = jsonObject.getString("data");
                        List<Object> list1 = JSON.parseArray(result1);
                        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                        for (Object object : list1) {
                            Map<String, Object> ageMap = new HashMap<String, Object>();
                            Map<String, Object> ret = (Map<String, Object>) object;//取出list里面的值转为map
                            list.add(ret);
                        }
                        Map<String, Object> m = new HashMap<String, Object>();
                        for (int q = 0; q < list.size(); q++) {
                            LogCook.d("遍历返回信息", valueOf(list.get(q)));
                            m = (Map<String, Object>) list.get(q); //通过索引方式进行转换类型的强转
                            Set keySet = m.keySet(); // 读取map中的文件
                            Iterator<String> it = keySet.iterator();
                            while (it.hasNext()) {  //挨个遍历
                                Object k = it.next(); // key
                                if (valueOf(k).equals("name")) {
                                    Object v = m.get(k);
                                    name.add(valueOf(v));
                                } else if (valueOf(k).equals("price")) {
                                    Object v = m.get(k);
                                    balance.add(valueOf(v));
                                }else if (valueOf(k).equals("id")) {
                                    Object v = m.get(k);
                                    id.add(valueOf(v));
                                }
                            }
                        }
                        LogCook.d("name", valueOf(name));
                        LogCook.d("balance", valueOf(balance));
                        LogCook.d("id", valueOf(id));
                        adapter = new MyAdapter(ShopActivity.this, name);
                        lv1.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        };
    };

    /**
     * 商城商品（用于卡内消费）
     */
    public void send() {
        new Thread(new Runnable() {
            public void run() {
                result = "";
                try {
//                    String data = "{\"user\":\"" + Data.getauth0type() + "\"}";
//                    data = URLEncoder.encode(data, "UTF-8");
                    String urlName = Data.gethttp1()+"/hierstarQrCode/pawnshop/shopgoods";
                    LogCook.d("发送参数", urlName);
                    URL U = new URL(urlName);
                    URLConnection connection = U.openConnection();
                    connection.connect();
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result += line;
                    }
                    in.close();
                } catch (Exception e) {
                    Looper.prepare();
                    e.printStackTrace();
                    WeiboDialogUtils.closeDialog(Data.getdialog());
                    Toast.makeText(getApplicationContext(),Data.getcontext().getResources().getString(R.string.sc2) , Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start(); // 开启线程
    }
    Bundle bundle = new Bundle();
    class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<String> mList = new ArrayList<>();
        public MyAdapter(Context context, List<String> list) {
            mContext = context;
            mList = list;
        }
        @Override
        public int getCount() {
            return mList.size();
        }
        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(mContext).inflate(R.layout.item_touzi, null);
                viewHolder.name = (TextView) view.findViewById(R.id.name);
                viewHolder.dividend = (TextView) view.findViewById(R.id.dividend);
                viewHolder.dividend.setVisibility(View.INVISIBLE);
                viewHolder.img = (ImageView) view.findViewById(R.id.image);
                viewHolder.fxtoken = (TextView) view.findViewById(R.id.fxtoken);
                viewHolder.fxtoken1 = (TextView) view.findViewById(R.id.fxtoken1);
                viewHolder.tztoken = (TextView) view.findViewById(R.id.tztoken);
                viewHolder.tztoken1 = (TextView) view.findViewById(R.id.tztoken1);
                viewHolder.cdname = (TextView) view.findViewById(R.id.cdname);
                viewHolder.cdname1 = (TextView) view.findViewById(R.id.cdname1);
                viewHolder.tz = (RelativeLayout) view.findViewById(R.id.tz);
                viewHolder.fxtoken.setVisibility(View.INVISIBLE);
                viewHolder.fxtoken1.setVisibility(View.INVISIBLE);
                viewHolder.tztoken1.setText(R.string.sc3);
                viewHolder.cdname1.setText("Store:");
                viewHolder.cdname.setText("HierStar Group");
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            if(viewHolder!=null) {
                if (mList.get(i).contains("电视机")) {
                    viewHolder.img.setImageResource(R.drawable.tv);
                    viewHolder.name.setText(name.get(0));
                    viewHolder.tztoken.setText(balance.get(0));
                    Data.setobjectId(id.get(0));
                }else if (mList.get(i).contains("冰箱")) {
                    viewHolder.img.setImageResource(R.drawable.bx);
                    viewHolder.name.setText(name.get(1));
                    viewHolder.tztoken.setText(balance.get(1));
                    Data.setobjectId(id.get(1));
                }else if (mList.get(i).contains("电饭锅")) {
                    viewHolder.img.setImageResource(R.drawable.dg);
                    viewHolder.name.setText(name.get(2));
                    viewHolder.tztoken.setText(balance.get(2));
                    Data.setobjectId(id.get(2));
                }
                WeiboDialogUtils.closeDialog(mWeiboDialog);
                ViewHolder finalViewHolder = viewHolder;
                viewHolder.tz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bundle.putString("name", finalViewHolder.name.getText().toString());
                        bundle.putString("balance", finalViewHolder.tztoken.getText().toString());
                        startActivity(new Intent(ShopActivity.this, SpxqActivity.class).putExtras(bundle));
                    }
                });
            }
            return view;
        }
        class ViewHolder {
            ImageView img;
            TextView name,dividend,fxtoken,fxtoken1,tztoken,tztoken1,cdname,cdname1;
            RelativeLayout tz;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果返回键按下
            Data.setresult("2");
            Intent intent = new Intent(this, IndexActivity.class);
            startActivity(intent);
        }
        return false;
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fhgx) {
            Data.setresult("2");
            Intent intent = new Intent(this, IndexActivity.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.tztype) {
            Intent intent = new Intent(this, ShoplistActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase));
    }
}
