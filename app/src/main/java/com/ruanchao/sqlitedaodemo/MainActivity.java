package com.ruanchao.sqlitedaodemo;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.sqlitedao.database.BaseDaoFactory;
import com.ruanchao.sqlitedao.database.IBaseDao;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PermissionsManager mPermissionsManager;
    public static final int PERMISSON_REQUESTCODE = 0;
    private IBaseDao<User> mUserDao;
    RecyclerView mRecycler;
    EditText mAge;
    EditText mName;
    EditText mSex;
    RecyclerAdapter mRecyclerAdapter;
    List<User> mUsers = new ArrayList<>();

    /**
     * 需要进行检测的权限数组
     */
    public final static String[] MAP_NEED_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionsManager = new PermissionsManager(this);
        if(mPermissionsManager.checkIsGrantedPermissions(PERMISSON_REQUESTCODE, MAP_NEED_PERMISSIONS)){
            init();
        }
    }

    private void init() {
        try {
            mUserDao = BaseDaoFactory.getInstance().getBaseDao("rc.db", User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAge = findViewById(R.id.age);
        mName = findViewById(R.id.name);
        mSex = findViewById(R.id.sex);
        mRecycler = findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerAdapter = new RecyclerAdapter();
        mRecycler.setAdapter(mRecyclerAdapter);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!mPermissionsManager.verifyPermissions(grantResults)) {
                //表示有权限没有授权，弹框提示，或者禁用该功能
                mPermissionsManager.showMissingPermissionDialog();
            }else{
                //进行具体的操作
                init();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void insertDb(View view) {
        User user = new User();
        user.setAge(Integer.valueOf(mAge.getText().toString()));
        user.setName(mName.getText().toString());
        user.setSex(mSex.getText().toString());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        user.setIcon(bitmapToByteArr(bitmap));
        Long insert = mUserDao.insert(user);
        if (insert >= 0){
            Toast.makeText(MainActivity.this,"插入成功", Toast.LENGTH_LONG).show();
            mUsers = mUserDao.queryAll(User.class);
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public byte[] bitmapToByteArr(Bitmap bitmap){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bos);
        return bos.toByteArray();
    }

    public void deleteDb(View view) throws Exception {

        User user = new User();
        user.setId(2l);
        long delete = mUserDao.delete(user);
        if (delete >0){
            Toast.makeText(MainActivity.this,"删除成功", Toast.LENGTH_LONG).show();
            mUsers = mUserDao.queryAll(User.class);
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public void update(View view) throws Exception {
        User user = new User();
        user.setId(1l);
        user.setSex("男");
        user.setAge(28);
        user.setName("ruanchao");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        user.setIcon(bitmapToByteArr(bitmap));
        long update = mUserDao.update(user);
        if (update >0){
            Toast.makeText(MainActivity.this,"更新成功", Toast.LENGTH_LONG).show();
            mUsers = mUserDao.queryAll(User.class);
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public void query(View view) {
        User user = mUserDao.queryById(User.class, 2L);
        if (user != null){
            Toast.makeText(MainActivity.this,user.getName() + " " + user.getId(), Toast.LENGTH_LONG).show();
        }
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public RecyclerAdapter(){
            mUsers = mUserDao.queryAll(User.class);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(MainActivity.this).inflate(R.layout.recycler_item_layout, null);
            ViewHolder viewHolder = new ViewHolder(inflate);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            if (mUsers.get(position).getId() != null){
                viewHolder.mItemId.setText(String.valueOf(mUsers.get(position).getId()));
            }
            if (mUsers.get(position).getName() != null){
                viewHolder.mItemName.setText(mUsers.get(position).getName());
            }
            if (mUsers.get(position).getAge() != 0){
                viewHolder.mItemAge.setText(String.valueOf(mUsers.get(position).getAge()));
            }
            if (mUsers.get(position).getSex() != null){
                viewHolder.mItemSex.setText(mUsers.get(position).getSex());
            }
            if (mUsers.get(position).getIcon() != null){
                byte[] icon = mUsers.get(position).getIcon();
                Bitmap bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
                viewHolder.mItemIcon.setImageBitmap(bitmap);
            }
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }
    class ViewHolder extends RecyclerView.ViewHolder{
        TextView mItemName;
        TextView mItemAge;
        TextView mItemSex;
        ImageView mItemIcon;
        TextView mItemId;
        public ViewHolder(View itemView) {
            super(itemView);
            mItemId = itemView.findViewById(R.id.tv_id);
            mItemName = itemView.findViewById(R.id.tv_name);
            mItemAge = itemView.findViewById(R.id.tv_age);
            mItemSex = itemView.findViewById(R.id.tv_sex);
            mItemIcon = itemView.findViewById(R.id.iv_icon);
        }
    }
}
