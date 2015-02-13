package com.qicheng.business.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qicheng.R;
import com.qicheng.framework.ui.base.BaseFragment;

/**
 * @author yangyu
 *         功能描述：列表Fragment，用来显示列表视图
 */
public class TopMenuFragment extends BaseFragment {
    private View view;
    private LinearLayout linearLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initView(inflater);
        return view;
    }


    /**
     * 初始化View视图
     *
     * @param inflater
     */
    public void initView(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.menu_list, null);
        linearLayout = (LinearLayout) view.findViewById(R.id.label_scroll_root);
          /*个人资料menu*/
        initViewItem(inflater, R.string.personal, R.drawable.ic_personal);
        /*我的标签menu*/
        initViewItem(inflater, R.string.my_label, R.drawable.ic_personal);
        /*我的相册menu*/
        initViewItem(inflater, R.string.my_photo, R.drawable.ic_personal);
        /*我的动态menu*/
        initViewItem(inflater, R.string.my_activity, R.drawable.ic_personal);
        /*筛选设置menu*/
        initViewItem(inflater, R.string.select_setting, R.drawable.ic_fliter);
         /*账户设置menu*/
        initViewItem(inflater, R.string.account_setting, R.drawable.ic_account_setting);

    }

    /**
     * 创建menu元素并加入到布局文件中
     *
     * @param inflater
     * @param stringID 字符串id
     * @param resId    icon id
     */

    public void initViewItem(LayoutInflater inflater, final int stringID, int resId) {
        View view = inflater.inflate(R.layout.personal_information_tabel, null);
        TextView viewText = (TextView) view.findViewById(R.id.table_text);
        viewText.setText(stringID);
        ImageView personalImg = (ImageView) view.findViewById(R.id.table_img);
        personalImg.setImageResource(resId);
        /*绑定点击事件*/
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (stringID) {
                    case R.string.personal:
                        /*跳转到个人资料页面*/
                        break;
                    case R.string.my_label:
                        /*跳转到我的标签*/
                        Intent intent = new Intent(getActivity(),LabelModifyActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    case R.string.my_photo:
                       /*跳转到我的相册*/
                        break;
                    case R.string.my_activity:
                        /*跳转到我的动态*/
                        break;
                    case R.string.select_setting:
                         /*跳转到筛选设置*/
                        break;
                    case R.string.account_setting:
                       /*跳转到账户设置*/
                        break;
                    default:
                        break;
                }
            }
        });
        linearLayout.addView(view);
    }



}
